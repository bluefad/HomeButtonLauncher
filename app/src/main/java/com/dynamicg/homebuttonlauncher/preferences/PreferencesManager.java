package com.dynamicg.homebuttonlauncher.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.tools.AppHelper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PreferencesManager {

    public static final String[] DFLT_GOOGLE_SEARCH = new String[]{
            "com.google.android.googlequicksearchbox/com.google.android.googlequicksearchbox.SearchActivity"
            , "com.android.quicksearchbox/com.android.quicksearchbox.SearchActivity"
    };
    private static final Logger log = new Logger(PreferencesManager.class);
    private static final String KEY_TAB_INDEX = "tabIndex";
    private static final String KEY_TAB_EXTRA_HEIGHT = "tabExtraHeight";
    private static final String KEY_TAB_LABEL_PREFIX = "tabTitle.";
    public final PrefSettings prefSettings;
    public final PrefShortlist prefShortlist;
    private final Context context;
    private int currentTabIndex;

    public PreferencesManager(MainActivityHome activity) {
        this.context = activity;
        this.prefSettings = new PrefSettings(context);
        GlobalContext.init(context, prefSettings);

        currentTabIndex = getCurrentTabIndex(prefSettings);
        this.prefShortlist = new PrefShortlist(activity, getShortlistPrefs(currentTabIndex));
        if (currentTabIndex == 0) {
            // skip for all extra tabs
            checkOnStartup();
        }
    }

    protected static String getShortlistName(int tabindex) {
        return tabindex == 0 ? HBLConstants.PREFS_APPS : HBLConstants.PREFS_APPS + tabindex;
    }

    private static int getCurrentTabIndex(PrefSettings prefSettings) {
        final int numTabs = prefSettings.getNumTabs();
        if (numTabs == 0) {
            return 0;
        }
        final int homeTabIndex = prefSettings.getHomeTabNum() - 1;
        if (homeTabIndex >= 0 && homeTabIndex < numTabs) {
            return homeTabIndex;
        }
        final int recentTabIndex = prefSettings.getIntValue(KEY_TAB_INDEX);
        if (recentTabIndex >= 0 && recentTabIndex < numTabs) {
            return recentTabIndex;
        }
        return 0;
    }

    private static void replaceAll(SharedPreferences prefs, Map<String, ?> map) {
        Editor edit = prefs.edit();
        edit.clear();
        for (String key : map.keySet()) {
            Object o = map.get(key);
            // also see com.dynamicg.homebuttonlauncher.tools.drive.GoogleDriveBackupRestoreHelper.restoreSettings(Context, File)
            if (o instanceof Boolean) {
                edit.putBoolean(key, (Boolean) o);
            } else if (o instanceof Integer) {
                edit.putInt(key, (Integer) o);
            } else if (o instanceof String) {
                edit.putString(key, (String) o);
            } else if (log.isDebugEnabled) {
                throw new IllegalArgumentException("cannot copy [" + o + "]");
            }
        }
        edit.apply();
    }

    private SharedPreferences getShortlistPrefs(int tabindex) {
        return context.getSharedPreferences(getShortlistName(tabindex), Context.MODE_PRIVATE);
    }

    private void checkOnStartup() {
        if (prefShortlist.size() > 0) {
            return;
        }
        for (String defaultApp : DFLT_GOOGLE_SEARCH) {
            if (AppHelper.getMatchingApp(defaultApp) != null) {
                prefShortlist.add(Arrays.asList(defaultApp));
                return;
            }
        }
    }

    public void updateCurrentTabIndex(int tabindex) {
        currentTabIndex = tabindex;
        prefShortlist.switchSharedPrefs(getShortlistPrefs(tabindex));
        prefSettings.apply(KEY_TAB_INDEX, tabindex);
    }

    public int getTabIndex() {
        return currentTabIndex;
    }

    public String getTabTitle(int index) {
        // default tab name is "Tn"
        return prefSettings.getStringValue(KEY_TAB_LABEL_PREFIX + index, "T" + (index + 1));
    }

    public void writeTabTitle(int index, String label) {
        prefSettings.apply(KEY_TAB_LABEL_PREFIX + index, label);
    }

    public void exchangeTabData(int tabIndexA, int tabIndexB) {

        log.debug("### SWITCH TABS ###", tabIndexA, tabIndexB);

        // switch shortlist
        SharedPreferences shortlistA = getShortlistPrefs(tabIndexA);
        SharedPreferences shortlistB = getShortlistPrefs(tabIndexB);
        Map<String, ?> itemsA = new HashMap<String, Object>(shortlistA.getAll());
        Map<String, ?> itemsB = new HashMap<String, Object>(shortlistB.getAll());
        replaceAll(shortlistA, itemsB);
        replaceAll(shortlistB, itemsA);

        // switch tab title
        String labelA = prefSettings.getStringValue(KEY_TAB_LABEL_PREFIX + tabIndexA, "");
        String labelB = prefSettings.getStringValue(KEY_TAB_LABEL_PREFIX + tabIndexB, "");
        writeTabTitle(tabIndexA, labelB);
        writeTabTitle(tabIndexB, labelA);
    }

    public int getTabExtraHeight() {
        return prefSettings.getIntValue(KEY_TAB_EXTRA_HEIGHT);
    }

    public void saveTabExtraHeight(int value) {
        prefSettings.apply(KEY_TAB_EXTRA_HEIGHT, value);
    }

}
