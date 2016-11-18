package com.dynamicg.homebuttonlauncher.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.OnClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.OnLongClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.R;

public class AppListAdapterSort extends AppListAdapter {

    private static final Logger log = new Logger(AppListAdapterSort.class);

    private final boolean[] sortChanged;
    private final View.OnClickListener clickListener;
    private final View.OnLongClickListener longClickListener;

    public AppListAdapterSort(MainActivityHome activity, AppListContainer apps, boolean[] sortChanged) {
        super(activity, apps, R.layout.app_entry_sort);
        this.sortChanged = sortChanged;

        clickListener = new OnClickListenerWrapper() {
            @Override
            public synchronized void onClickImpl(View v) {
                int oldPosition = (Integer) v.getTag();
                int newPosition = v.getId() == R.id.sortDown ? oldPosition + 1 : oldPosition - 1;
                applyMove(oldPosition, newPosition);
            }
        };

        longClickListener = new OnLongClickListenerWrapper() {
            @Override
            public synchronized boolean onLongClickImpl(View v) {
                int oldPosition = (Integer) v.getTag();
                int newPosition = v.getId() == R.id.sortDown ? applist.size() - 1 : 0;
                applyMove(oldPosition, newPosition);
                return true;
            }
        };
    }

    private void applyMove(int oldPosition, int newPosition) {
        sortChanged[0] = true;
        AppEntry entry = applist.get(oldPosition);
        applist.moveTo(entry, newPosition);
        log.debug("applyMove done", entry, oldPosition, newPosition);
        notifyDataSetChanged();
    }

    private void prepareButton(Button button, int buttonId, int position) {
        button.setTag(position);

        button.setOnClickListener(clickListener);
        button.setOnLongClickListener(longClickListener);
        button.setLongClickable(true);

        boolean active = (buttonId == R.id.sortUp && position > 0)
                || (buttonId == R.id.sortDown && position < applist.size() - 1);
        if (active) {
            button.setText(buttonId == R.id.sortUp ? "\u2191" : "\u2193");
        } else {
            button.setText("");
        }
        button.setEnabled(active);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final AppEntry appEntry = applist.get(position);
        final LinearLayout layout;
        ViewHolder holder;
        if (convertView == null) {
            layout = (LinearLayout) inflater.inflate(appEntryLayoutId, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) layout.findViewById(R.id.sortIcon);
            holder.label = (TextView) layout.findViewById(R.id.sortLabel);
            holder.up = (Button) layout.findViewById(R.id.sortUp);
            holder.down = (Button) layout.findViewById(R.id.sortDown);
            layout.setTag(holder);
        } else {
            layout = (LinearLayout) convertView;
            holder = (ViewHolder) layout.getTag();
        }

        prepareButton(holder.up, R.id.sortUp, position);
        prepareButton(holder.down, R.id.sortDown, position);
        holder.icon.setImageDrawable(appEntry.getIcon(iconLoader));
        holder.label.setText(appEntry.getLabel());

        return layout;
    }

    static class ViewHolder {
        TextView label;
        ImageView icon;
        Button up;
        Button down;
    }


}
