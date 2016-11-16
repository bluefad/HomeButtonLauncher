package com.dynamicg.homebuttonlauncher.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.SizePrefsHelper;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.icons.IconLoader;
import com.dynamicg.homebuttonlauncher.tools.icons.IconProvider;

public abstract class AppListAdapter extends BaseAdapter {

    public final MainActivityHome activity;
    protected final AppListContainer applist;
    protected final LayoutInflater inflater;
    protected final int appEntryLayoutId;
    protected final int labelSize;
    protected final int iconSizePx;
    protected final int noLabelGridPadding;
    protected final IconLoader iconLoader;

    private final LocalViewBinder localViewBinder;

    private AppListAdapter(MainActivityHome activity, AppListContainer apps, int viewId, boolean forMainScreen, int iconSizePx, int labelSize) {
        this.activity = activity;
        this.applist = apps;
        this.inflater = activity.getLayoutInflater();
        this.appEntryLayoutId = viewId;
        this.iconSizePx = iconSizePx;
        this.labelSize = labelSize;
        this.iconLoader = new IconLoader(activity, iconSizePx, forMainScreen);

        if (labelSize == 0 && appEntryLayoutId == R.layout.app_entry_compact) {
            this.noLabelGridPadding = DialogHelper.getDimension(R.dimen.gridViewNoLabelIconPadding);
        } else {
            this.noLabelGridPadding = 0;
        }

        this.localViewBinder = new LocalViewBinderDefault();
    }

    /*
     * for main screen
     */
    public AppListAdapter(MainActivityHome activity, AppListContainer apps) {
        this(activity, apps, GlobalContext.prefSettings.getAppEntryLayoutId(), true, IconProvider.getPreferredSizePX(), GlobalContext.prefSettings.getLabelSize());
    }

    /*
     * for config screens
     */
    public AppListAdapter(MainActivityHome activity, AppListContainer apps, int viewId) {
        this(activity, apps, viewId, false, IconProvider.getDefaultSizePX(), SizePrefsHelper.DEFAULT_LABEL_SIZE);
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public int getCount() {
        return applist.size();
    }

    @Override
    public Object getItem(int position) {
        return applist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getOrCreateView(View convertView) {
        return localViewBinder.provideView(convertView);
    }

    protected void setLabel(TextView label, AppEntry appEntry) {
        if (this.labelSize == 0) {
            label.setText("");
        } else {
            label.setText(appEntry.getLabel());
        }
    }

    public void bindView(int position, AppEntry appEntry, View row) {
        localViewBinder.bindView(position, appEntry, row);
    }

    public abstract class LocalViewBinder {
        public abstract View provideView(View convertView);

        public abstract void bindView(int position, AppEntry appEntry, View row);
    }

    public class LocalViewBinderDefault extends LocalViewBinder {

        @Override
        public View provideView(View convertView) {
            if (convertView == null) {
                final TextView row = (TextView) inflater.inflate(appEntryLayoutId, null);
                row.setTextSize(labelSize);
                if (noLabelGridPadding > 0) {
                    row.setCompoundDrawablePadding(noLabelGridPadding);
                }
                return row;
            }
            return (TextView) convertView;
        }

        @Override
        public void bindView(int position, AppEntry appEntry, View row) {
            TextView label = (TextView) row;
            setLabel(label, appEntry);
            Drawable icon = appEntry.getIcon(iconLoader);
            if (appEntryLayoutId == R.layout.app_entry_compact) {
                // icon on top
                label.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            } else {
                // icon left
                label.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
            }
        }

    }
}