package com.dynamicg.homebuttonlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterMain;
import com.dynamicg.homebuttonlauncher.adapter.AppListAdapterMainStatic;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderAbstract;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tab.TabHelperMain;
import com.dynamicg.homebuttonlauncher.tools.AppHelper;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.DirectDialWrapper;
import com.dynamicg.homebuttonlauncher.tools.HblPermissionRequest;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;
import com.dynamicg.homebuttonlauncher.tools.StatusLineHelper;
import com.dynamicg.homebuttonlauncher.tools.SwipeHelper;
import com.dynamicg.homebuttonlauncher.tools.drive.GoogleDriveGlobals;
import com.dynamicg.homebuttonlauncher.tools.drive.HBLBackupRestoreGoogleDrive;

import java.util.Arrays;

/*
 * Copyright 2012,2013 DynamicG (dynamicg.android@gmail.com)
 * Distributed under the terms of the GNU General Public License
 * http://www.gnu.org/licenses/gpl-3.0.txt
 */

/*
 * android 5 ref
 * http://www.google.com/design/spec/style/color.html#color-color-palette
 * http://developer.android.com/training/material/theme.html#ColorPalette
 */
public class MainActivityHome extends Activity {

    private static final Logger log = new Logger(MainActivityHome.class);

    private static final int MAX_STATIC_THRESHOLD = 64;

    private Context context;
    private PreferencesManager preferences;
    private TabHost tabhost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        context = this;

        try {
            main();
        } catch (Throwable t) {
            DialogHelper.showCrashReport(context, t);
        }
    }

    private void setAppTheme() {
        int theme = preferences.prefSettings.getThemeId();
        if (theme == PrefSettings.THEME_TRANSPARENT) {
            setTheme(R.style.ThemeSemiTransparent);
            int alpha = preferences.prefSettings.getTransparencyAlpha();
            alpha--; // strange android 5.0 issue: background breaks and shows ghost images when setting solid color (i.e. alpha 0xff)
            Drawable drawable = new ColorDrawable(Color.argb(alpha, 0x00, 0x00, 0x00));
            getWindow().setBackgroundDrawable(drawable);
        } else if (theme == PrefSettings.THEME_LIGHT) {
            setTheme(R.style.ThemeLight);
        }
        // default app theme is "dark"
    }

    private void main() {

        if (forwardToGoogleNow()) {
            return;
        }

        preferences = new PreferencesManager(this);
        setAppTheme();
        setContentView(R.layout.activity_main);

        if (isAutoStartSingleSuccessful()) {
            return;
        }
        attachContextMenu();
        if (preferences.prefSettings.getNumTabs() > 0) {
            tabhost = new TabHelperMain(this, preferences).bindTabs();
        }
        setListAdapter();
        setMinWidth();

        if (preferences.prefSettings.isShowStatusLine()) {
            StatusLineHelper.addStatus(this, preferences.prefSettings);
        }

    }

    private boolean forwardToGoogleNow() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null
                && bundle.containsKey(HBLConstants.GOOGLE_NOW_EXTRA)
                && bundle.getInt(HBLConstants.GOOGLE_NOW_EXTRA, 0) == 1
                ) {
            try {
                Intent googlenow = AppHelper.getStartIntent(PreferencesManager.DFLT_GOOGLE_SEARCH[0]);
                googlenow.putExtras(bundle); // copy extras passed by the widget
                AppHelper.flagAsNewTask(googlenow);
                startActivity(googlenow);
                finish();
                return true;
            } catch (Throwable t) {
                DialogHelper.showCrashReport(context, t);
            }
        }
        return false;
    }

    private boolean isStartedFromLauncherApp() {
        return getIntent().getBooleanExtra(MainActivityOpen.KEY, false) == true;
    }

    private boolean isAutoStartSingleSuccessful() {
        if (!preferences.prefSettings.isAutoStartSingle()) {
            log.debug("autoStart", "disabled");
            return false;
        }

        if (isStartedFromLauncherApp()) {
            // called through "OpenActivity" (i.e. app drawer or homescreen icon), not through swipe
            // -> skip, otherwise we will lock ourselves out
            log.debug("autoStart", "from OpenActivity");
            return false;
        }

        if (preferences.prefShortlist.getComponentsMap().size() != 1) {
            // shortcut - this is faster than "getSelectedAppsList" just below
            log.debug("autoStart", "getComponentsMap", "size!=1");
            return false;
        }

        final AppListContainer appList = AppHelper.getSelectedAppsList(preferences.prefShortlist, true);
        if (appList.size() != 1) {
            log.debug("autoStart", "getSelectedAppsList", "size!=1");
            return false;
        }

        AppEntry entry = appList.get(0);
        log.debug("autoStart", entry.getComponent());
        boolean started = startAppAndClose(entry);
        return started;
    }

    public void refreshList() {
        try {
            setListAdapter();
            setMinWidth();
        } catch (Throwable t) {
            DialogHelper.showCrashReport(context, t);
        }
    }

    private void setMinWidth() {
        int minWidth = DialogHelper.getDimension(preferences.prefSettings.getMinWidthDimension());
        findViewById(R.id.headerContainer).setMinimumWidth(minWidth);
        if (preferences.prefSettings.isNoHeader() && !isStartedFromLauncherApp()) {
            findViewById(R.id.headerContainer).setVisibility(View.GONE);
        }
    }

    private AbsListView getListView() {
        final int[] layout = preferences.prefSettings.getMainLayout();
        int layoutResId = layout[0];
        int numGridColumns = layout[1];
        final View listview = findViewById(R.id.mainListView);

        if (listview instanceof ViewStub) {
            // first call
            ViewStub stub = (ViewStub) listview;
            stub.setLayoutResource(layoutResId);
            AbsListView absListView = (AbsListView) stub.inflate();
            if (numGridColumns > 0) {
                ((GridView) absListView).setNumColumns(numGridColumns);
            }
            return absListView;
        }

        // replace existing list on refresh
        ViewGroup parent = (ViewGroup) listview.getParent();
        AbsListView replacementListView = (AbsListView) getLayoutInflater().inflate(layoutResId, null);
        if (numGridColumns > 0) {
            ((GridView) replacementListView).setNumColumns(numGridColumns);
        }
        parent.addView(replacementListView, parent.indexOfChild(listview));
        parent.removeView(listview);
        return replacementListView;
    }

    private void setListAdapter() {
        final AbsListView listview = getListView();
        listview.setId(R.id.mainListView);

        final AppListContainer appList = AppHelper.getSelectedAppsList(preferences.prefShortlist, true);

        final BaseAdapter adapter;
        if (appList.size() <= MAX_STATIC_THRESHOLD) {
            // use "keep textviews" adapter when less then [max] rows - since we have lightweight views should make startup faster.
            // also, with a typical setup most selected apps will be visible all times anyway
            adapter = new AppListAdapterMainStatic(this, appList);
        } else {
            adapter = new AppListAdapterMain(this, appList);
        }

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnAdapterItemClickListenerWrapper() {
            @Override
            public void onItemClickImpl(AdapterView<?> parent, View view, int position, long id) {
                startAppAndClose(appList.get(position));
            }
        });
        new AppListContextMenu(this, true).attach(listview, appList);

        if (tabhost != null) {
            SwipeHelper.attach(this, preferences, listview);
        }
    }

    private boolean startAppAndClose(final AppEntry entry) {
        if (DirectDialWrapper.isPermissionRequired(entry)) {
            new HblPermissionRequest(this, HblPermissionRequest.PHONE_CALL) {
                @Override
                public void onPermissionResult(boolean permissionGranted) {
                    startAppAndCloseImpl(entry); // regardless of 'permissionGranted'. see DirectDialWrapper.onShortcutOpen
                }
            };
            return false;
        } else {
            return startAppAndCloseImpl(entry);
        }
    }

    private boolean startAppAndCloseImpl(AppEntry entry) {
        Intent intent = null;
        try {
            if (entry.isShortcut()) {
                intent = ShortcutHelper.getIntent(entry);
                DirectDialWrapper.onShortcutOpen(context, intent);
            } else {
                intent = AppHelper.getStartIntent(entry.getComponent());
            }

            if (intent.getComponent() != null && HBLConstants.SELF.equals(intent.getComponent().flattenToString())) {
                // for those that want to disable the swipe (i.e. set "auto start mode" and only select HBL)
                log.debug("close self", HBLConstants.SELF);
                finish();
                return true;
            }

            AppHelper.flagAsNewTask(intent);
            startActivity(intent);

            boolean keepHomeLauncherOpen = entry.isShortcut() && ShortcutHelper.isKeepOpen(preferences.prefSettings, entry);
            if (!keepHomeLauncherOpen) {
                finish();
            }
            return true;
        } catch (Throwable t) {
            SystemUtil.dumpError(t);

            String appinfo = entry.getComponent();
            if (entry.isShortcut() && intent != null && intent.getComponent() != null) {
                // "entry.getComponent()" for shortcuts is "sc-<nn>|#<label>" so we show the shortcut intent instead
                appinfo = intent.getComponent().flattenToString();
            }

            String title = "Error - cannot open";
            String details = "\u2022 Exception: " + t.getClass().getSimpleName()
                    + "\n\u2022 Component:\n{" + appinfo + "}";
            if (t instanceof SecurityException) {
                details += "\n\n\u2022 Details: " + t.getMessage();
            }
            DialogHelper.showError(context, title, details);
            return false;
        }
    }

    private void attachContextMenu() {
        final ImageView anchor = (ImageView) findViewById(R.id.headerIcon);
        anchor.setOnClickListener(new OnClickListenerWrapper() {
            private PopupMenuWrapper wrapper;

            @Override
            public void onClickImpl(View v) {
                // create the popup only if actually required
                if (wrapper == null) {
                    wrapper = bindMainMenu(anchor);
                }
                wrapper.showMenu();
            }
        });
        anchor.setImageResource(HeaderAbstract.getMenuIconId(preferences.prefSettings));
    }

    public PopupMenuWrapper bindMainMenu(final View anchor) {
        final PopupMenuItemListener listener = new PopupMenuItemListener() {
            @Override
            public void popupMenuItemSelected(PopupMenu popupMenu, int id) {
                MainActivityHome activity = MainActivityHome.this;
                switch (id) {
                    case HBLConstants.MENU_APPS_ADD:
                        AppConfigDialog.showAddDialog(activity, preferences);
                        break;
                    case HBLConstants.MENU_APPS_REMOVE:
                        new AppConfigDialog(activity, preferences, HBLConstants.MENU_APPS_REMOVE).show();
                        break;
                    case HBLConstants.MENU_APPS_SORT:
                        new AppConfigDialog(activity, preferences, HBLConstants.MENU_APPS_SORT).show();
                        break;
                    case HBLConstants.MENU_PREFERENCES:
                        new PreferencesDialog(activity, preferences).show();
                        break;
                }
            }
        };

        final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, anchor, listener);
        menuWrapper.addItem(HBLConstants.MENU_APPS_ADD, R.string.menuAdd, android.R.drawable.ic_menu_add);
        menuWrapper.addItem(HBLConstants.MENU_APPS_REMOVE, R.string.menuRemove, android.R.drawable.ic_menu_close_clear_cancel);
        menuWrapper.addItem(HBLConstants.MENU_APPS_SORT, R.string.menuSort, android.R.drawable.ic_menu_sort_by_size);
        menuWrapper.addItem(HBLConstants.MENU_PREFERENCES, R.string.preferences, android.R.drawable.ic_menu_preferences);
        return menuWrapper;
    }

    public void updateOnTabSwitch(int tabindex) {
        log.debug("updateOnTabSwitch", tabindex);
        preferences.updateCurrentTabIndex(tabindex);
        refreshList();
    }

    public void redrawTabContainer() {
        log.debug("redrawTabContainer");
        tabhost = new TabHelperMain(this, preferences).redraw();
    }

    public void forceTabSwitch(int tabindex) {
        updateOnTabSwitch(tabindex);
        redrawTabContainer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GoogleDriveGlobals.ACTION_CUSTOM_GET) {
            HBLBackupRestoreGoogleDrive.restoreFromFile(data);
        } else if (requestCode == HBLConstants.SHORTCUT_RC) {
            ShortcutHelper.shortcutSelected(this, data);
        }
    }

    public void saveShortcutComponent(String component) {
        preferences.prefShortlist.add(Arrays.asList(component));
    }

    public PreferencesManager getPreferences() {
        return preferences;
    }

    public TabHost getTabHost() {
        return tabhost;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        HblPermissionRequest.onRequestPermissionsResult(grantResults[0]);
    }
}
