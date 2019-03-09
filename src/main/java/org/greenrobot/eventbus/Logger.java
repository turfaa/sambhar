package org.greenrobot.eventbus;

import android.util.Log;
import java.io.PrintStream;
import java.util.logging.Level;

public interface Logger {

    public static class AndroidLogger implements Logger {
        static final boolean ANDROID_LOG_AVAILABLE;
        private final String tag;

        static {
            boolean z = false;
            try {
                if (Class.forName("android.util.Log") != null) {
                    z = true;
                }
            } catch (ClassNotFoundException unused) {
            }
            ANDROID_LOG_AVAILABLE = z;
        }

        public static boolean isAndroidLogAvailable() {
            return ANDROID_LOG_AVAILABLE;
        }

        public AndroidLogger(String str) {
            this.tag = str;
        }

        public void log(Level level, String str) {
            if (level != Level.OFF) {
                Log.println(mapLevel(level), this.tag, str);
            }
        }

        public void log(Level level, String str, Throwable th) {
            if (level != Level.OFF) {
                int mapLevel = mapLevel(level);
                String str2 = this.tag;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append("\n");
                stringBuilder.append(Log.getStackTraceString(th));
                Log.println(mapLevel, str2, stringBuilder.toString());
            }
        }

        /* Access modifiers changed, original: protected */
        public int mapLevel(Level level) {
            int intValue = level.intValue();
            if (intValue < 800) {
                return intValue < 500 ? 2 : 3;
            } else {
                if (intValue < 900) {
                    return 4;
                }
                return intValue < 1000 ? 5 : 6;
            }
        }
    }

    public static class JavaLogger implements Logger {
        protected final java.util.logging.Logger logger;

        public JavaLogger(String str) {
            this.logger = java.util.logging.Logger.getLogger(str);
        }

        public void log(Level level, String str) {
            this.logger.log(level, str);
        }

        public void log(Level level, String str, Throwable th) {
            this.logger.log(level, str, th);
        }
    }

    public static class SystemOutLogger implements Logger {
        public void log(Level level, String str) {
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            stringBuilder.append(level);
            stringBuilder.append("] ");
            stringBuilder.append(str);
            printStream.println(stringBuilder.toString());
        }

        public void log(Level level, String str, Throwable th) {
            PrintStream printStream = System.out;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            stringBuilder.append(level);
            stringBuilder.append("] ");
            stringBuilder.append(str);
            printStream.println(stringBuilder.toString());
            th.printStackTrace(System.out);
        }
    }

    void log(Level level, String str);

    void log(Level level, String str, Throwable th);
}
