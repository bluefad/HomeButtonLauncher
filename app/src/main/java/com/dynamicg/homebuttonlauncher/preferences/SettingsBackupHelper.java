package com.dynamicg.homebuttonlauncher.preferences;

import android.content.Context;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.HBLConstants;

import java.util.ArrayList;

public class SettingsBackupHelper {

    private static final Logger log = new Logger(SettingsBackupHelper.class);

    private static int getSettingNumTabs(Context context) {
        try {
            PrefSettings prefSettings = new PrefSettings(context);
            return prefSettings.getNumTabs();
        } catch (Throwable t) {
            log.debug("ERROR", t);
        }
        return 0;
    }

    public static ArrayList<String> getSharedPrefNames(Context context) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(HBLConstants.PREFS_SETTINGS);
        final int settingNumTabs = getSettingNumTabs(context);
        final int pagecount = settingNumTabs == 0 ? 1 : settingNumTabs;
        for (int i = 0; i < pagecount; i++) {
            // note "tab0" or 'none' (if tabs not enabled) is key "apps", all the others are key "app<tabindex>"
            list.add(PreferencesManager.getShortlistName(i));
        }
        log.debug("#### HomeLauncherBackupAgent ###", list);
        return list;
    }

}
