package com.dynamicg.homebuttonlauncher;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppListContainer {

    private List<AppEntry> list;

    public AppListContainer(List<AppEntry> list) {
        this.list = sort(list);
    }

    private static List<AppEntry> sort(List<AppEntry> list) {
        Collections.sort(list, new Comparator<AppEntry>() {
            @Override
            public int compare(AppEntry lhs, AppEntry rhs) {
                // note sortnr is zero when called through "getAllApps"
                if (lhs.sortnr != rhs.sortnr) {
                    return lhs.sortnr - rhs.sortnr;
                }
                return lhs.label.compareToIgnoreCase(rhs.label);
            }
        });
        return list;
    }

    public List<AppEntry> getApps() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public AppEntry get(int position) {
        return list.get(position);
    }

    public void moveTo(AppEntry entry, int newPosition) {
        list.remove(entry);
        list.add(newPosition, entry);
    }

    public void updateList(List<AppEntry> newList) {
        this.list = newList;
    }

}
