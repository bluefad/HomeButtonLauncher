package com.dynamicg.homebuttonlauncher.tools;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.text.style.SuperscriptSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.OnClickListenerWrapper;

public class PopupMenuWrapper {

    @SuppressWarnings("unused")
    private static final Logger log = new Logger(PopupMenuWrapper.class);
    private final Context context;
    private final View anchor;
    private final PopupMenu popupMenu;
    private final Menu menu;

    public PopupMenuWrapper(final Context context, final View anchor, final PopupMenuItemListener listener) {
        this.context = context;
        this.anchor = anchor;
        this.popupMenu = new PopupMenu(context, anchor);
        this.menu = popupMenu.getMenu();

        popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    listener.popupMenuItemSelected(popupMenu, item.getItemId());
                } catch (Throwable t) {
                    DialogHelper.showCrashReport(context, t);
                }
                return true;
            }
        });
    }

    public void attachToAnchorClick() {
        anchor.setOnClickListener(new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                popupMenu.show();
            }
        });
    }

    public void showMenu() {
        popupMenu.show();
    }

    public void addItem(int id, int titleResId) {
        menu.add(id, id, 0, titleResId);
    }

    public void addItem(int id, String title) {
        menu.add(id, id, 0, title);
    }

    public void addItem(int id, int titleResId, int imageId) {
        //menu.add(id, id, 0, titleResId);
        final String label = "    " + context.getString(titleResId) + "        ";
        SpannableString spannable = new SpannableString(label);
        ImageSpan imagespan = new ImageSpan(context, imageId, ImageSpan.ALIGN_BASELINE);
        spannable.setSpan(imagespan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(new SuperscriptSpan(), 1, label.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        menu.add(id, id, 0, spannable);
    }

    public int size() {
        return menu.size();
    }

    public interface PopupMenuItemListener {
        public void popupMenuItemSelected(PopupMenu popupMenu, int id);
    }

}
