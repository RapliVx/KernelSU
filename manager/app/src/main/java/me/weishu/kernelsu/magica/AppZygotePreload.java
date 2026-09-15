package me.weishu.kernelsu.magica;

import android.app.ZygotePreload;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

public class AppZygotePreload implements ZygotePreload {
    public static final String TAG = "KernelSUMagica";

    private static native void forkDontCareAndExecKsud(String ksudPath, String packageName);
    private static native void forkDontCareAndExecApd(String apdPath, String modulePath, String packageName);

    @Override
    public void doPreload(@NonNull ApplicationInfo appInfo) {
        File f = new File(appInfo.nativeLibraryDir, "libksud.so");
        if (f.exists()) {
            try {
                System.loadLibrary("kernelsu");
                Log.d(TAG, "executing magica for KSU ...");
                forkDontCareAndExecKsud(f.getAbsolutePath(), appInfo.packageName);
            } catch (Throwable t) {
                Log.e(TAG, "failed to late load KSU", t);
            }
        }

        File apd = new File(appInfo.nativeLibraryDir, "libapd.so");
        if (apd.exists()) {
            try {
                System.loadLibrary("apjni");
                File module = new File(appInfo.dataDir, "files/kernelpatch.ko");
                Log.d(TAG, "executing magica for APatch ...");
                forkDontCareAndExecApd(apd.getAbsolutePath(), module.getAbsolutePath(), appInfo.packageName);
            } catch (Throwable t) {
                Log.e(TAG, "failed to late load APatch", t);
            }
        }
    }
}
