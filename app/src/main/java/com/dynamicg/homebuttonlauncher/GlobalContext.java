package com.dynamicg.homebuttonlauncher;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;

import java.util.HashMap;

public class GlobalContext {

    public static final HashMap<String, String> labels = new HashMap<String, String>();
    public static final HashMap<String, Drawable> icons = new HashMap<String, Drawable>();
    public static PackageManager packageManager;
    public static PrefSettings prefSettings;
    public static Resources resources;
    public static float density;

    public static void init(Context context, PrefSettings prefSettings) {
        GlobalContext.packageManager = context.getPackageManager();
        GlobalContext.prefSettings = prefSettings;
        GlobalContext.resources = context.getResources();
        GlobalContext.density = resources.getDisplayMetrics().density;
    }

    public static void resetCache() {
        labels.clear();
        icons.clear();
    }

}
