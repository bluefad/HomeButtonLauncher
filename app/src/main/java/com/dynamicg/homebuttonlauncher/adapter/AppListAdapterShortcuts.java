package com.dynamicg.homebuttonlauncher.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;

public class AppListAdapterShortcuts extends AppListAdapter {

    public AppListAdapterShortcuts(MainActivityHome activity, AppListContainer apps) {
        super(activity, apps, R.layout.app_entry_default);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View row = getOrCreateView(convertView);
        final AppEntry appEntry = applist.get(position);
        bindView(position, appEntry, row);
        return row;
    }

}
