package com.dynamicg.homebuttonlauncher.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.preferences.PrefShortlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AppHelper {

    private static final Logger log = new Logger(AppHelper.class);

    private static final int MAX_SORTNR = 999;

    public static String getComponentName(ResolveInfo resolveInfo) {
        return new StringBuilder()
                .append(resolveInfo.activityInfo.packageName)
                .append("/")
                .append(resolveInfo.activityInfo.name)
                .toString();
    }

    // see http://stackoverflow.com/questions/2780102/open-another-application-from-your-own-intent
    public static Intent getStartIntent(String componentName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(ComponentName.unflattenFromString(componentName));
        return intent;
    }

    public static ResolveInfo getMatchingApp(String component) {
        Intent intent = getStartIntent(component);
        final List<ResolveInfo> apps = GlobalContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (apps != null && apps.size() == 1) {
            // note "intent.setComponent" with only the activity name will match multiple entries (e.g. "com.dynamicg.bookmarkTree.Main")
            return apps.get(0);
        } else {
            log.debug("no match", component, (apps != null ? apps.size() : -1));
        }
        return null;
    }

    public static AppListContainer getAllAppsList(PrefShortlist settings) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        ArrayList<AppEntry> list = new ArrayList<AppEntry>();

        Collection<String> selectedComponents = new HashSet<String>(settings.getComponentsSet());
        selectedComponents.add(HBLConstants.SELF); // do not show my own app in the list

        final List<ResolveInfo> apps = GlobalContext.packageManager.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo resolveInfo : apps) {
            String component = getComponentName(resolveInfo);
            // System.err.println("COMPONENT:["+component+"]");
            if (!selectedComponents.contains(component)) {
                // skip apps already on the list
                list.add(new AppEntry(resolveInfo, 0, false));
            }
        }

        return new AppListContainer(list);
    }

    private static int getSortNr(Map<String, Integer> components, String component) {
        int sortnr = components.get(component);
        if (sortnr == 0) {
            // unsorted new entries get to the bottom
            return MAX_SORTNR;
        }
        return sortnr;
    }

    public static AppListContainer getSelectedAppsList(PrefShortlist settings, boolean forMainScreen) {
        final Map<String, Integer> components = settings.getComponentsMap();
        final ArrayList<AppEntry> list = new ArrayList<AppEntry>();
        for (String component : components.keySet()) {
            int sortnr = getSortNr(components, component);

            if (ShortcutHelper.isShortcutComponent(component)) {
                list.add(new AppEntry(component, sortnr, forMainScreen, AppEntry.TYPE_SHORTCUT));
                continue;
            }

            ResolveInfo matchingApp = getMatchingApp(component);
            if (matchingApp != null) {
                list.add(new AppEntry(matchingApp, sortnr, forMainScreen));
            }

        }
        return new AppListContainer(list);
    }

    public static AppListContainer getShortcutApps() {
        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        List<ResolveInfo> shortcutApps = GlobalContext.packageManager.queryIntentActivities(shortcutsIntent, 0);
        ArrayList<AppEntry> list = new ArrayList<AppEntry>();
        for (ResolveInfo resolveInfo : shortcutApps) {
            ActivityInfo ai = resolveInfo.activityInfo;
            log.trace("shortcut apps", resolveInfo, ai, ai.packageName, ai.name);
            list.add(new AppEntry(resolveInfo, 0, false));
        }

        // "Exit Self" shortcut - disabled with 4.0, there's other apps for that
        //		ResolveInfo self = getMatchingApp(HBLConstants.SELF);
        //		AppEntry appEntrySelf = new AppEntry(self, 0, false);
        //		list.add(appEntrySelf);

        return new AppListContainer(list);
    }

    public static void flagAsNewTask(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static void openAppDetails(Context context, String pkgname) {
        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        Uri data = Uri.parse("package:" + pkgname);
        i.setData(data);
        context.startActivity(i);
    }

}