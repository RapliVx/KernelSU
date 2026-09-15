package me.weishu.kernelsu

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import me.weishu.kernelsu.Natives.Profile.RootProfileFlag
import me.weishu.kernelsu.ui.util.rootAvailable

/**
 * @author weishu
 * @date 2022/12/8.
 */
object Natives {
    var superKey: String = "su"
    val isAPatchInstalled: Boolean
        get() = me.weishu.kernelsu.apatch.APatchNatives.nativeReady(superKey)

    // minimal supported kernel version
    // 10915: allowlist breaking change, add app profile
    // 10931: app profile struct add 'version' field
    // 10946: add capabilities
    // 10977: change groups_count and groups to avoid overflow write
    // 11071: Fix the issue of failing to set a custom SELinux type.
    // 12143: breaking: new supercall impl
    // 32310: new get_allow_list ioctl
    // 32336: new set_sepolicy ioctl
    // 32377: add set_init_pgrp ioctl
    // 32513: add uapi version
    const val MINIMAL_SUPPORTED_KERNEL = 32513 + 30 // KowSU commit around 30

    const val KERNEL_SU_DOMAIN = "u:r:ksu:s0"

    const val ROOT_UID = 0
    const val ROOT_GID = 0

    init {
        System.loadLibrary("kernelsu")
    }

    private external fun nativeGetVersion(): Int
    val version: Int
        get() {
            if (isAPatchInstalled) {
                return me.weishu.kernelsu.apatch.APatchNatives.kernelPatchVersion().toInt()
            }
            return nativeGetVersion()
        }

    val isSafeMode: Boolean
        external get

    val isLkmMode: Boolean
        external get

    val isLkmBundled: Boolean
        external get

    val lkmVariant: String?
        external get

    val isLateLoadMode: Boolean
        external get

    private external fun nativeIsManager(): Boolean
    val isManager: Boolean
        get() {
            if (isAPatchInstalled) return true
            return nativeIsManager()
        }
        
    val hookMode: String
        external get

    val isPrBuild: Boolean
        external get

    external fun uidShouldUmount(uid: Int): Boolean

    /**
     * Get the profile of the given package.
     * @param key usually the package name
     * @return return null if failed.
     */
    private external fun nativeGetAppProfile(key: String?, uid: Int): Profile
    fun getAppProfile(key: String?, uid: Int): Profile {
        if (isAPatchInstalled) {
            val isSu = me.weishu.kernelsu.apatch.APatchNatives.suUids().contains(uid)
            val apProfile = if (isSu) me.weishu.kernelsu.apatch.APatchNatives.suProfile(uid) else null
            return Profile(
                name = key ?: "",
                currentUid = uid,
                allowSu = isSu,
                context = apProfile?.scontext ?: KERNEL_SU_DOMAIN
            )
        }
        return nativeGetAppProfile(key, uid)
    }

    private external fun nativeSetAppProfile(profile: Profile?): Boolean
    fun setAppProfile(profile: Profile?): Boolean {
        if (isAPatchInstalled && profile != null) {
            if (profile.allowSu) {
                me.weishu.kernelsu.apatch.APatchNatives.grantSu(profile.currentUid, 0, profile.context)
                return true
            } else {
                me.weishu.kernelsu.apatch.APatchNatives.revokeSu(profile.currentUid)
                return true
            }
        }
        return nativeSetAppProfile(profile)
    }

    /**
     * `su` compat mode can be disabled temporarily.
     * 0: disabled
     * 1: enabled
     * negative : error
     */
    private external fun nativeIsSuEnabled(): Boolean
    fun isSuEnabled(): Boolean {
        if (isAPatchInstalled) return true // APatch SU is always enabled if ready
        return nativeIsSuEnabled()
    }

    private external fun nativeSetSuEnabled(enabled: Boolean): Boolean
    fun setSuEnabled(enabled: Boolean): Boolean {
        if (isAPatchInstalled) return true
        return nativeSetSuEnabled(enabled)
    }

    /**
     * Kernel module umount can be disabled temporarily.
     * 0: disabled
     * 1: enabled
     * negative : error
     */
    private external fun nativeIsKernelUmountEnabled(): Boolean
    fun isKernelUmountEnabled(): Boolean {
        if (isAPatchInstalled) return false // APatch uses magisk-like umount
        return nativeIsKernelUmountEnabled()
    }

    private external fun nativeSetKernelUmountEnabled(enabled: Boolean): Boolean
    fun setKernelUmountEnabled(enabled: Boolean): Boolean {
        if (isAPatchInstalled) return false
        return nativeSetKernelUmountEnabled(enabled)
    }

    /**
     * SELinux hide can be disabled temporarily.
     * 0: disabled
     * 1: enabled
     * negative : error
     */
    private external fun nativeIsSelinuxHideEnabled(): Boolean?
    fun isSelinuxHideEnabled(): Boolean? {
        if (isAPatchInstalled) return false // Fallback
        return nativeIsSelinuxHideEnabled()
    }

    private external fun nativeSetSelinuxHideEnabled(enabled: Boolean): Int
    fun setSelinuxHideEnabled(enabled: Boolean): Int {
        if (isAPatchInstalled) return -1
        return nativeSetSelinuxHideEnabled(enabled)
    }

    /**
     * Avc spoof can be enabled/disabled.
     * 0: disabled
     * 1: enabled
     * negative : error
     */
    @JvmStatic
    private external fun nativeIsAvcSpoofEnabled(): Boolean
    fun isAvcSpoofEnabled(): Boolean {
        if (isAPatchInstalled) return false
        return nativeIsAvcSpoofEnabled()
    }

    @JvmStatic
    private external fun nativeSetAvcSpoofEnabled(enabled: Boolean): Boolean
    fun setAvcSpoofEnabled(enabled: Boolean): Boolean {
        if (isAPatchInstalled) return false
        return nativeSetAvcSpoofEnabled(enabled)
    }

    @JvmStatic
    private external fun nativeIsAdbRootEnabled(): Boolean?
    fun isAdbRootEnabled(): Boolean? {
        if (isAPatchInstalled) return false
        return nativeIsAdbRootEnabled()
    }

    @JvmStatic
    private external fun nativeSetAdbRootEnabled(enabled: Boolean): Boolean
    fun setAdbRootEnabled(enabled: Boolean): Boolean {
        if (isAPatchInstalled) return false
        return nativeSetAdbRootEnabled(enabled)
    }

    /**
     * Get the user name for the uid.
     */
    external fun getUserName(uid: Int): String?

    private external fun nativeGetSuperuserCount(): Int
    fun getSuperuserCount(): Int {
        if (isAPatchInstalled) {
            return me.weishu.kernelsu.apatch.APatchNatives.suUids().size
        }
        return nativeGetSuperuserCount()
    }

    private const val NON_ROOT_DEFAULT_PROFILE_KEY = "$"
    private const val NOBODY_UID = 9999

    fun setDefaultUmountModules(umountModules: Boolean): Boolean {
        Profile(
            NON_ROOT_DEFAULT_PROFILE_KEY,
            NOBODY_UID,
            false,
            umountModules = umountModules
        ).let {
            return setAppProfile(it)
        }
    }

    fun isDefaultUmountModules(): Boolean {
        getAppProfile(NON_ROOT_DEFAULT_PROFILE_KEY, NOBODY_UID).let {
            return it.umountModules
        }
    }

    private external fun nativeGetKernelUAPIVersion(): Int
    val kernelUAPIVersion: Int
        get() {
            if (isAPatchInstalled) return 1
            return nativeGetKernelUAPIVersion()
        }

    private external fun nativeGetManagerUAPIVersion(): Int
    val managerUAPIVersion: Int
        get() {
            if (isAPatchInstalled) return 1
            return nativeGetManagerUAPIVersion()
        }

    fun isFullFeatured(): Boolean {
        return isManager && kernelUAPIVersion == managerUAPIVersion && rootAvailable()
    }

    @Keep
    @Immutable
    @Parcelize
    @Serializable
    data class Profile(
        // and there is a default profile for root and non-root
        val name: String,
        // current uid for the package, this is convivent for kernel to check
        // if the package name doesn't match uid, then it should be invalidated.
        val currentUid: Int = 0,

        // if this is true, kernel will grant root permission to this package
        val allowSu: Boolean = false,

        // these are used for root profile
        val rootUseDefault: Boolean = true,
        val rootTemplate: String? = null,
        val uid: Int = ROOT_UID,
        val gid: Int = ROOT_GID,
        val groups: List<Int> = mutableListOf(),
        val capabilities: List<Int> = mutableListOf(),
        val context: String = KERNEL_SU_DOMAIN,
        val namespace: Int = Namespace.INHERITED.ordinal,

        val nonRootUseDefault: Boolean = true,
        val umountModules: Boolean = true,
        var rules: String = "", // this field is save in ksud!!

        val flags: Long = FLAG_KSU_NO_NEW_PRIVS,
    ) : Parcelable {
        @Keep
        enum class RootProfileFlag(val display: String, val desc: Int) {
            NO_NEW_PRIVS(
                "NO_NEW_PRIVS",
                R.string.profile_flags_desc_no_new_privs
            )
        }

        enum class Namespace {
            INHERITED,
            GLOBAL,
            INDIVIDUAL,
        }

        constructor() : this("")
    }

    const val FLAG_KSU_NO_NEW_PRIVS = 1L
}

fun List<RootProfileFlag>.toRawFlags(): Long =
    fold(0L) { acc, flag -> acc.or(1L.shl(flag.ordinal)) }

fun List<RootProfileFlag>.toOrdinalList(): List<Int> =
    map { it.ordinal }

fun Long.toRootProfileFlags(): List<RootProfileFlag> =
    RootProfileFlag.entries.filter { 1L.shl(it.ordinal).and(this) != 0L }.toList()