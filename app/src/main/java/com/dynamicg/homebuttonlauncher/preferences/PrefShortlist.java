package com.dynamicg.homebuttonlauncher.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;

import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.tools.AppHelper;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrefShortlist {

    private SharedPreferences sharedPrefs;

    /**
     * @param activity
     * @param appPrefs
     */
    public PrefShortlist(MainActivityHome activity, SharedPreferences appPrefs) {
        this.sharedPrefs = appPrefs;
    }

    public void switchSharedPrefs(SharedPreferences newPrefs) {
        this.sharedPrefs = newPrefs;
    }

    public int size() {
        return sharedPrefs.getAll().size();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getComponentsMap() {
        return (Map<String, Integer>) sharedPrefs.getAll();
    }

    public Collection<String> getComponentsSet() {
        Map<String, ?> all = sharedPrefs.getAll();
        Set<String> keySet = all.keySet();
        return keySet;
    }

    public synchronized void add(List<String> components) {
        Editor edit = sharedPrefs.edit();
        for (String comp : components) {
            edit.putInt(comp, 0);
        }
        edit.apply();
        validateAll();
    }

    public synchronized void remove(List<String> components) {
        removeImpl(components);
        validateAll();
    }

    public synchronized void remove(AppEntry entry) {
        removeImpl(Arrays.asList(entry.getComponent()));
    }

    private void validateAll() {
        final Collection<String> components = getComponentsSet();
        final ArrayList<String> zombies = new ArrayList<String>();
        for (String component : components) {
            if (ShortcutHelper.isShortcutComponent(component)) {
                continue;
            }
            ResolveInfo matchingApp = AppHelper.getMatchingApp(component);
            if (matchingApp == null || !AppHelper.getComponentName(matchingApp).equals(component)) {
                zombies.add(component);
            }
        }
        if (zombies.size() > 0) {
            removeImpl(zombies);
        }
    }

    private void removeImpl(List<String> components) {
        ArrayList<String> shortcutIds = new ArrayList<String>();

        Editor edit = sharedPrefs.edit();
        for (String component : components) {
            edit.remove(component);
            if (ShortcutHelper.isShortcutComponent(component)) {
                shortcutIds.add(ShortcutHelper.getShortcutId(component));
            }
        }
        edit.apply();

        if (shortcutIds.size() > 0) {
            ShortcutHelper.removeShortcuts(shortcutIds);
        }
    }

    public synchronized void saveSortedList(List<AppEntry> appList) {
        Editor edit = sharedPrefs.edit();
        for (int idx = 0; idx < appList.size(); idx++) {
            String component = appList.get(idx).getComponent();
            // sort pos is "index+1", pos "0" will be used for all apps added later
            // see com.dynamicg.homebuttonlauncher.tools.AppHelper.sort(List<AppEntry>)
            edit.putInt(component, idx + 1);
        }
        edit.apply();
    }

    public synchronized void resetSortList() {
        Editor edit = sharedPrefs.edit();
        for (String s : sharedPrefs.getAll().keySet()) {
            edit.putInt(s, 0);
        }
        edit.apply();
    }

}
