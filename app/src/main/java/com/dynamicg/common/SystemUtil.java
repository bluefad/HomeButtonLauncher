package com.dynamicg.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

public class SystemUtil {

    public static final String PACKAGE = "com.dynamicg.homebuttonlauncher";

    private static final Logger log = new Logger(SystemUtil.class);

    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName()
                    , PackageManager.GET_ACTIVITIES + PackageManager.GET_META_DATA
            );
            return info.versionName;
        } catch (Exception e) {
            log.warn("get version/revision", e);
            return "0";
        }
    }

    public static String getExceptionText(Throwable exception) {
        final int limit = 1200;

        if (exception == null) {
            return "<no exception>";
        }

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String text = sw.getBuffer().toString();

        return text.length() > limit ? text.substring(0, limit) + "..." : text;

    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Throwable t) {
        }
    }

    public static void dumpError(Throwable e) {
        if (log.isDebugEnabled) {
            e.printStackTrace(System.err);
        }
    }

    public static String getFullStackTrace(Throwable exception) {
        if (exception == null) {
            return "<no exception>";
        }
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.getBuffer().toString();
    }

    private static boolean contains(String what, String... items) {
        for (String s : items) {
            if (s != null && s.toLowerCase(Locale.getDefault()).contains(what)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSony() {
        // http://gfxbench.com/device.jsp?benchmark=gfx27&D=Sony+C6603+Xperia+Z&testgroup=system
        return contains("sony", Build.MANUFACTURER, Build.BRAND, Build.FINGERPRINT);
    }

}
