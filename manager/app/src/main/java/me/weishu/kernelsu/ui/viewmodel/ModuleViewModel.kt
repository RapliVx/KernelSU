package me.weishu.kernelsu.ui.viewmodel

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.weishu.kernelsu.ksuApp
import me.weishu.kernelsu.ui.util.HanziToPinyin
import me.weishu.kernelsu.ui.util.isNetworkAvailable
import me.weishu.kernelsu.ui.util.listModules
import me.weishu.kernelsu.ui.util.module.sanitizeVersionString
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.Collator
import java.util.Locale

class ModuleViewModel : ViewModel() {

    companion object {
        private const val TAG = "ModuleViewModel"
        private var modules by mutableStateOf<List<ModuleInfo>>(emptyList())
    }

    @Immutable
    class ModuleInfo(
        val id: String,
        val name: String,
        val author: String,
        val version: String,
        val versionCode: Int,
        val description: String,
        val enabled: Boolean,
        val update: Boolean,
        val remove: Boolean,
        val updateJson: String,
        val hasWebUi: Boolean,
        val hasActionScript: Boolean,
        val metamodule: Boolean,
        val banner: String? = null
    )

    @Immutable
    data class ModuleUpdateInfo(
        val downloadUrl: String,
        val version: String,
        val changelog: String
    ) {
        companion object {
            val Empty = ModuleUpdateInfo("", "", "")
        }
    }

    private data class ModuleUpdateSignature(
        val updateJson: String,
        val versionCode: Int,
        val enabled: Boolean,
        val update: Boolean,
        val remove: Boolean
    )

    private data class ModuleUpdateCache(
        val signature: ModuleUpdateSignature,
        val info: ModuleUpdateInfo
    )

    var isRefreshing by mutableStateOf(false)
        private set
    var search by mutableStateOf(TextFieldValue(""))

    var sortEnabledFirst by mutableStateOf(false)
    var sortActionFirst by mutableStateOf(false)
    var checkModuleUpdate by mutableStateOf(true)

    private val updateInfoMutex = Mutex()
    private var updateInfoCache: MutableMap<String, ModuleUpdateCache> = mutableMapOf()
    private val updateInfoInFlight = mutableSetOf<String>()
    private val _updateInfo = mutableStateMapOf<String, ModuleUpdateInfo>()
    val updateInfo: SnapshotStateMap<String, ModuleUpdateInfo> = _updateInfo

    val moduleList by derivedStateOf {
        val comparator = moduleComparator()
        val text = search.text
        modules.filter {
            it.id.contains(text, true) || it.name.contains(text, true) ||
                    it.description.contains(text, true) || it.author.contains(text, true) ||
                    HanziToPinyin.getInstance().toPinyinString(it.name).contains(text, true)
        }.sortedWith(comparator).also {
            isRefreshing = false
        }
    }

    var isNeedRefresh by mutableStateOf(false)
        private set

    fun markNeedRefresh() {
        isNeedRefresh = true
    }

    private fun moduleComparator(): Comparator<ModuleInfo> {
        return compareBy<ModuleInfo>(
            {
                val executable = it.hasWebUi || it.hasActionScript
                when {
                    it.metamodule -> 0
                    sortEnabledFirst && sortActionFirst -> when {
                        it.enabled && executable -> 1
                        it.enabled -> 2
                        executable -> 3
                        else -> 4
                    }

                    sortEnabledFirst && !sortActionFirst -> if (it.enabled) 1 else 2
                    !sortEnabledFirst && sortActionFirst -> if (executable) 1 else 2
                    else -> 1
                }
            },
            { if (sortEnabledFirst) !it.enabled else 0 },
            { if (sortActionFirst) !(it.hasWebUi || it.hasActionScript) else 0 },
        ).thenBy(Collator.getInstance(Locale.getDefault()), ModuleInfo::name)
    }

    fun fetchModuleList() {
        viewModelScope.launch {
            withContext(Dispatchers.Main) { isRefreshing = true }

            val oldModuleList = modules
            val start = SystemClock.elapsedRealtime()

            val parsedModules = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    val result = listModules()
                    Log.i(TAG, "result: $result")
                    val array = JSONArray(result)
                    (0 until array.length())
                        .asSequence()
                        .map { array.getJSONObject(it) }
                        .map { obj ->
                            val id = obj.getString("id")

                            // ✅ FIX: gunakan 'id' bukan 'moduleId'
                            val moduleRoot = File("/data/adb/modules", id)
                            val props = loadModuleProp(moduleRoot)
                            val bannerPath = resolveBannerPath(moduleRoot, props)

                            ModuleInfo(
                                id = id,
                                name = obj.optString("name"),
                                author = obj.optString("author", "Unknown"),
                                version = obj.optString("version", "Unknown"),
                                versionCode = obj.optInt("versionCode", 0),
                                description = obj.optString("description"),
                                enabled = obj.optBoolean("enabled"),
                                update = obj.optBoolean("update"),
                                remove = obj.optBoolean("remove"),
                                updateJson = obj.optString("updateJson"),
                                hasWebUi = obj.optBoolean("web"),
                                hasActionScript = obj.optBoolean("action"),
                                metamodule = (obj.optInt("metamodule") != 0) or obj.optBoolean("metamodule"),
                                banner = bannerPath // ← ini sekarang valid
                            )
                        }.toList()
                }.getOrElse {
                    Log.e(TAG, "fetchModuleList: ", it)
                    emptyList()
                }
            }

            withContext(Dispatchers.Main) {
                modules = parsedModules
                isNeedRefresh = false
                if (oldModuleList === modules) {
                    isRefreshing = false
                }
            }

            if (parsedModules.isNotEmpty()) {
                syncModuleUpdateInfo(parsedModules)
            }

            withContext(Dispatchers.Main) {
                isRefreshing = false
            }

            Log.i(TAG, "load cost: ${SystemClock.elapsedRealtime() - start}, modules: $modules")
        }
    }

    private fun ModuleInfo.toSignature(): ModuleUpdateSignature {
        return ModuleUpdateSignature(
            updateJson = updateJson,
            versionCode = versionCode,
            enabled = enabled,
            update = update,
            remove = remove
        )
    }

    suspend fun syncModuleUpdateInfo(modules: List<ModuleInfo>) {
        if (!checkModuleUpdate) return

        val modulesToFetch = mutableListOf<Triple<String, ModuleInfo, ModuleUpdateSignature>>()
        val removedIds = mutableSetOf<String>()

        updateInfoMutex.withLock {
            val ids = modules.map { it.id }.toSet()
            updateInfoCache.keys.filter { it !in ids }.forEach { removedId ->
                removedIds += removedId
                updateInfoCache.remove(removedId)
                updateInfoInFlight.remove(removedId)
            }

            modules.forEach { module ->
                val signature = module.toSignature()
                val cached = updateInfoCache[module.id]
                if ((cached == null || cached.signature != signature) && updateInfoInFlight.add(module.id)) {
                    modulesToFetch += Triple(module.id, module, signature)
                }
            }
        }

        val fetchedEntries = coroutineScope {
            modulesToFetch.map { (id, module, signature) ->
                async(Dispatchers.IO) {
                    id to ModuleUpdateCache(signature, checkUpdate(module))
                }
            }.awaitAll()
        }

        val changedEntries = mutableListOf<Pair<String, ModuleUpdateInfo>>()
        updateInfoMutex.withLock {
            fetchedEntries.forEach { (id, entry) ->
                val existing = updateInfoCache[id]
                if (existing == null || existing.signature != entry.signature || existing.info != entry.info) {
                    updateInfoCache[id] = entry
                    changedEntries += id to entry.info
                }
                updateInfoInFlight.remove(id)
            }
        }

        if (removedIds.isEmpty() && changedEntries.isEmpty()) {
            return
        }

        withContext(Dispatchers.Main) {
            removedIds.forEach { _updateInfo.remove(it) }
            changedEntries.forEach { (id, info) ->
                _updateInfo[id] = info
            }
        }
    }

    fun checkUpdate(m: ModuleInfo): ModuleUpdateInfo {
        if (!isNetworkAvailable(ksuApp)) {
            return ModuleUpdateInfo.Empty
        }
        if (m.updateJson.isEmpty() || m.remove || !m.enabled) {
            return ModuleUpdateInfo.Empty
        }
        // download updateJson
        val result = kotlin.runCatching {
            val url = m.updateJson
            Log.i(TAG, "checkUpdate url: $url")
            val response = ksuApp.okhttpClient.newCall(
                okhttp3.Request.Builder().url(url).build()
            ).execute()
            Log.d(TAG, "checkUpdate code: ${response.code}")
            if (response.isSuccessful) {
                response.body?.string() ?: ""
            } else {
                ""
            }
        }.getOrDefault("")
        Log.i(TAG, "checkUpdate result: $result")

        if (result.isEmpty()) {
            return ModuleUpdateInfo.Empty
        }

        val updateJson = kotlin.runCatching {
            JSONObject(result)
        }.getOrNull() ?: return ModuleUpdateInfo.Empty

        var version = updateJson.optString("version", "")
        version = sanitizeVersionString(version)
        val versionCode = updateJson.optInt("versionCode", 0)
        val zipUrl = updateJson.optString("zipUrl", "")
        val changelog = updateJson.optString("changelog", "")
        if (versionCode <= m.versionCode || zipUrl.isEmpty()) {
            return ModuleUpdateInfo.Empty
        }

        return ModuleUpdateInfo(zipUrl, version, changelog)
    }
}

fun resolveBannerPath(moduleRoot: File, props: Map<String, String>): String? {
    val banner = props["banner"] ?: return null
    val file = File(moduleRoot, banner.trimStart('/'))
    Log.i("ModuleViewModel", "resolveBannerPath: checking ${file.absolutePath}, exists=${file.exists()}")
    return if (file.exists() && file.isFile) file.absolutePath else null
}

fun loadModuleProp(moduleRoot: File): Map<String, String> {
    val propFile = File(moduleRoot, "module.prop")
    if (!propFile.exists()) return emptyMap()
    return propFile.readLines().mapNotNull {
        val parts = it.split("=", limit = 2)
        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
    }.toMap()
}