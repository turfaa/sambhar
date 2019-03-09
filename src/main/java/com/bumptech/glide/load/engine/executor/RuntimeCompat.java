package com.bumptech.glide.load.engine.executor;

import android.os.Build.VERSION;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

final class RuntimeCompat {
    private static final String CPU_LOCATION = "/sys/devices/system/cpu/";
    private static final String CPU_NAME_REGEX = "cpu[0-9]+";
    private static final String TAG = "GlideRuntimeCompat";

    private RuntimeCompat() {
    }

    static int availableProcessors() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return VERSION.SDK_INT < 17 ? Math.max(getCoreCountPre17(), availableProcessors) : availableProcessors;
    }

    private static int getCoreCountPre17() {
        File[] listFiles;
        ThreadPolicy allowThreadDiskReads = StrictMode.allowThreadDiskReads();
        try {
            File file = new File(CPU_LOCATION);
            final Pattern compile = Pattern.compile(CPU_NAME_REGEX);
            listFiles = file.listFiles(new FilenameFilter() {
                public boolean accept(File file, String str) {
                    return compile.matcher(str).matches();
                }
            });
            StrictMode.setThreadPolicy(allowThreadDiskReads);
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(allowThreadDiskReads);
            throw th;
        }
        return Math.max(1, listFiles != null ? listFiles.length : 0);
    }
}
