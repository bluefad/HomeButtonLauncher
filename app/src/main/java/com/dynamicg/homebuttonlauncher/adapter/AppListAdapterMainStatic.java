package com.dynamicg.homebuttonlauncher.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.MainActivityHome;

import java.util.Arrays;
import java.util.List;

public class AppListAdapterMainStatic extends AppListAdapter {

    private final List<View> list;

    public AppListAdapterMainStatic(MainActivityHome activity, AppListContainer apps) {
        super(activity, apps);
        list = Arrays.asList(new View[apps.size()]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (list.get(position) == null) {
            final View row = getOrCreateView(null); // make sure we don't recycle views
            bindView(position, applist.get(position), row);
            list.set(position, row);
        }
        return list.get(position);
    }

}
