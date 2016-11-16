package com.dynamicg.homebuttonlauncher.tab;

import android.widget.TabHost;

import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public class TabHelperAppAdd extends TabHelper {

    private final AppConfigDialog appConfigDialog;
    private final int selectedIndex;

    public TabHelperAppAdd(MainActivityHome activity, AppConfigDialog appConfigDialog, int selectedIndex) {
        super(activity, 2, appConfigDialog.findViewById(R.id.headerContainer), false);
        this.appConfigDialog = appConfigDialog;
        this.selectedIndex = selectedIndex;
    }

    @Override
    public TabHost bindTabs() {
        TabHost.OnTabChangeListener onTabChangeListener = new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                try {
                    appConfigDialog.tabChanged(Integer.parseInt(tabId));
                } catch (Throwable t) {
                    DialogHelper.showCrashReport(context, t);
                }
            }

            ;
        };
        String[] labels = new String[]{context.getString(R.string.tabApps), context.getString(R.string.tabShortcuts)};
        return bindTabs(selectedIndex, labels, onTabChangeListener, null);
    }

}
