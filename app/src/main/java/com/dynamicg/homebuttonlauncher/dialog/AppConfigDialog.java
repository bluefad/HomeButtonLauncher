package com.dynamicg.homebuttonlauncher.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.AppListContextMenu;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.OnAdapterItemClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.OnClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapter;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterAddRemove;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterShortcuts;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterSort;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderAbstract;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderAppSearch;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderAppSortReset;
import com.dynamicg.homebuttonlauncher.preferences.PrefShortlist;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tab.TabHelperAppAdd;
import com.dynamicg.homebuttonlauncher.tools.AppHelper;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("UseSparseArrays")
public class AppConfigDialog extends Dialog {

    private static final Logger log = new Logger(AppConfigDialog.class);

    private static final HashMap<Integer, Integer> recentTab = new HashMap<Integer, Integer>();

    private final MainActivityHome activity;
    private final PrefShortlist prefShortlist;
    private final boolean[] sortChanged = new boolean[]{false};

    private final int action;
    private final boolean actionAdd;
    private final boolean actionRemove;
    private final boolean actionSort;
    protected AppListAdapter adapter;
    private AppListContainer preparedList; // async loaded
    private AppListContainer appList;
    private HeaderAbstract header;

    public AppConfigDialog(MainActivityHome activity, PreferencesManager preferences, int action) {
        super(activity);
        setCanceledOnTouchOutside(false);
        this.activity = activity;
        this.prefShortlist = preferences.prefShortlist;
        this.action = action;
        this.actionAdd = action == HBLConstants.MENU_APPS_ADD;
        this.actionSort = action == HBLConstants.MENU_APPS_SORT;
        this.actionRemove = action == HBLConstants.MENU_APPS_REMOVE;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    public static void afterSave(MainActivityHome activity, AppConfigDialog optionalDialog) {
        GlobalContext.resetCache();
        activity.refreshList();

        // close dialog
        if (optionalDialog != null && optionalDialog.isShowing()) {
            try {
                optionalDialog.dismiss();
            } catch (IllegalArgumentException e) {
                SystemUtil.dumpError(e);
            }
        }
    }

    private static int getSelectedTab(int action) {
        return recentTab.containsKey(action) ? recentTab.get(action) : 0;
    }

    public static void showAddDialog(final MainActivityHome activity, final PreferencesManager preferences) {
        final int action = HBLConstants.MENU_APPS_ADD;
        final AppConfigDialog dialog = new AppConfigDialog(activity, preferences, action);
        final String progressLabel = activity.getString(R.string.menuAdd) + " \u2026";
        new SimpleProgressDialog(activity, progressLabel) {
            @Override
            public void backgroundWork() {
                if (getSelectedTab(action) > 0) {
                    dialog.preparedList = AppHelper.getShortcutApps();
                } else {
                    dialog.preparedList = AppHelper.getAllAppsList(preferences.prefShortlist);
                }
            }

            @Override
            public void done() {
                dialog.show();
            }
        };
    }

    private final void onButtonOk() {
        if (actionAdd) {
            log.debug("actionAdd");
            prefShortlist.add(getSelectedComponents());
        } else if (actionRemove) {
            log.debug("actionRemove");
            prefShortlist.remove(getSelectedComponents());
        } else if (actionSort && sortChanged[0]) {
            // only store the sorted list if we actually had changes
            log.debug("actionSort");
            prefShortlist.saveSortedList(appList.getApps());
        }
        afterSave(activity, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.configure_apps);

        this.header = actionSort ? new HeaderAppSortReset(this) : new HeaderAppSearch(this);
        final int titleResId = actionRemove ? R.string.menuRemove : actionSort ? R.string.menuSort : R.string.menuAdd;
        header.attach(titleResId);

        findViewById(R.id.buttonOk).setOnClickListener(new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                onButtonOk();
            }
        });

        findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });

        if (actionAdd) {
            new TabHelperAppAdd(activity, this, getSelectedTab()).bindTabs();
        }

        putBody();
    }

    private int getSelectedTab() {
        return getSelectedTab(action);
    }

    private void putBody() {
        final boolean addShortcut = actionAdd && getSelectedTab() > 0;

        if (preparedList != null) {
            appList = preparedList;
            preparedList = null;
        } else if (addShortcut) {
            this.appList = AppHelper.getShortcutApps();
        } else if (actionAdd) {
            this.appList = AppHelper.getAllAppsList(prefShortlist);
        } else {
            this.appList = AppHelper.getSelectedAppsList(prefShortlist, false);
        }

        if (addShortcut) {
            putBodyShortcutTab();
        } else {
            putBodyMainTab();
        }

        header.setBaseAppList(appList);
    }

    private void putBodyMainTab() {
        if (actionSort) {
            this.adapter = new AppListAdapterSort(activity, appList, sortChanged);
        } else {
            this.adapter = new AppListAdapterAddRemove(activity, appList);
        }

        final ListView listview = (ListView) findViewById(R.id.applist);
        listview.setAdapter(adapter);

        if (!actionSort) {

            listview.setOnItemClickListener(new OnAdapterItemClickListenerWrapper() {
                @Override
                public void onItemClickImpl(AdapterView<?> parent, View view, int position, long id) {
                    AppEntry entry = (AppEntry) appList.get(position);
                    entry.flipCheckedState();
                    entry.decorateSelection(view);
                }
            });

            new AppListContextMenu(activity, false).attach(listview, appList);
        }

        if (actionAdd) {
            listview.setFastScrollEnabled(true);
        }

    }

    private void putBodyShortcutTab() {
        this.adapter = new AppListAdapterShortcuts(activity, appList);
        final ListView listview = (ListView) findViewById(R.id.applist);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnAdapterItemClickListenerWrapper() {
            @Override
            public void onItemClickImpl(AdapterView<?> parent, View view, int position, long id) {
                startShortcutApp((AppEntry) appList.get(position));
            }
        });

        new AppListContextMenu(activity, false).attach(listview, appList);
    }

    private List<String> getSelectedComponents() {
        List<String> list = new ArrayList<String>();
        for (AppEntry entry : appList.getApps()) {
            if (entry.isChecked()) {
                list.add(entry.getComponent());
            }
        }
        return list;
    }

    public void doSortReset() {
        prefShortlist.resetSortList();
        afterSave(activity, this);
    }

    // when "search" is applied
    public void updateAppList(List<AppEntry> newList) {
        appList.updateList(newList);
        adapter.notifyDataSetChanged();
    }

    public void tabChanged(int target) {
        log.debug("tabChanged", target);
        recentTab.put(action, target);
        putBody();
    }

    private void startShortcutApp(AppEntry appEntry) {

        if (HBLConstants.SELF.equals(appEntry.getComponent())) {
            ShortcutHelper.startExitSelfShortcut(activity, this, appEntry);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        intent.setComponent(ComponentName.unflattenFromString(appEntry.getComponent()));
        ShortcutHelper.storeDialogRef(this);
        activity.startActivityForResult(intent, HBLConstants.SHORTCUT_RC);
    }

}
