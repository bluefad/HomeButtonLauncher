package com.dynamicg.homebuttonlauncher.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.MainActivityHome;

public class AppListAdapterMain extends AppListAdapter {

    public AppListAdapterMain(MainActivityHome activity, AppListContainer apps) {
        super(activity, apps);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View row = getOrCreateView(convertView);
        bindView(position, applist.get(position), row);
        return row;
    }

}
