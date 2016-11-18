package com.dynamicg.common;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.lang.reflect.Field;

public class DeviceInfo {

    private static String getValue(Class<?> c, String key) {
        try {
            Field f = c.getDeclaredField(key);
            if (f != null) {
                Object value = f.get(null);
                return value.toString();
            }
        } catch (Throwable t) {
        }
        return "";
    }

    @SuppressLint("DefaultLocale") // toUpperCase,toLowerCase
    private static String getNameAndValue(Class<?> c, String key) {
        String name = key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
        return name + ": " + getValue(c, key);
    }

    public static String getDeviceInfo() {
        Class<?> build = Build.class;
        String NL = "\n";
        return getNameAndValue(build, "MODEL")
                + NL + getNameAndValue(build, "DEVICE")
                + NL + getNameAndValue(build, "BRAND")
                + NL + getNameAndValue(build, "MANUFACTURER")
                + NL + getNameAndValue(build, "DISPLAY")
                + NL + "SDK: " + Build.VERSION.SDK_INT + ", " + Build.VERSION.RELEASE
                + NL + "Free space MB: " + dumpStorage()
                ;
    }

    private static String dumpStorage(File path) {
        long oneMB = 1024l * 1024l;
        StatFs stat = new StatFs(path.getPath());
        long freeMB = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize()) / oneMB;
        long totalMB = ((long) stat.getBlockCount() * (long) stat.getBlockSize()) / oneMB;
        return freeMB + " of " + totalMB;
    }

    private static String dumpStorage() {
        try {
            return "Phone " + dumpStorage(Environment.getDataDirectory())
                    + ", Ext " + dumpStorage(Environment.getExternalStorageDirectory());
        } catch (Throwable t1) {
            try {
                return "Phone " + dumpStorage(Environment.getDataDirectory());
            } catch (Throwable t2) {
                return "?";
            }
        }
    }

}
