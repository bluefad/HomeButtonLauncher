package com.dynamicg.homebuttonlauncher.tab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class TabHelper {

    private static final Logger log = new Logger(TabHelper.class);

    protected final Context context;
    protected final MainActivityHome activity;
    protected final int numTabs;
    private final View header;
    private final boolean atBottom;

    protected TabSpec[] tabs;
    protected View[] tabviews;

    public TabHelper(MainActivityHome activity, int numTabs, View header, boolean atBottom) {
        this.activity = activity;
        this.context = activity;
        this.numTabs = numTabs;
        this.header = header;
        this.atBottom = atBottom;
    }

    public abstract TabHost bindTabs();

    private int getTabHeight() {
        int height = DialogHelper.getDimension(R.dimen.tabHeight);
        int extraCount = activity.getPreferences().getTabExtraHeight();
        if (extraCount > 0) {
            int extraHeight = DialogHelper.getDimension(R.dimen.tabExtraHeight);
            height += extraHeight * extraCount;
        }
        return height;
    }

    protected final TabHost bindTabs(int selectedIndex, String[] labels, TabHost.OnTabChangeListener onTabChangeListener, View.OnLongClickListener longClickListener) {
        final LayoutInflater inflater = activity.getLayoutInflater();
        final TabHost tabhost = (TabHost) inflater.inflate(R.layout.tabs_container, null);
        tabhost.setup();
        createTabs(tabhost, labels);
        log.debug("bindTabs", selectedIndex);
        tabhost.setCurrentTab(selectedIndex);
        tabhost.setOnTabChangedListener(onTabChangeListener);
        int tabHeight = getTabHeight();

        for (int i = 0; i < numTabs; i++) {
            View tab = tabviews[i];
            tab.setTag(R.id.tag_tab_index, i);

            if (longClickListener != null) {
                tab.setLongClickable(true);
                tab.setOnLongClickListener(longClickListener);
            }

            // layout options is same as time recording, see com.dynamicg.timerecording.util.ui.TabHostUtil.prepareTabs(Dialog, int, int[], int[])
            tab.getLayoutParams().height = tabHeight;
            tab.setPadding(0, tab.getPaddingTop(), 0, tab.getPaddingBottom());
        }

        ViewGroup main = (ViewGroup) header.getParent();
        int targetpos = main.indexOfChild(header) + (atBottom ? 2 : 1);
        main.addView(tabhost, targetpos);

        return tabhost;
    }

    private void createTabs(TabHost tabhost, String[] labels) {
        final TabHost.TabContentFactory factory = new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                return new View(context);
            }
        };

        this.tabs = new TabSpec[numTabs];
        this.tabviews = new View[numTabs];

        TabWidget tabWidget = tabhost.getTabWidget();
        if (log.isDebugEnabled) {
            log.debug("tabWidget", tabWidget.getChildCount(), tabWidget.getChildAt(0));
        }

        for (int i = 0; i < numTabs; i++) {
            TabSpec spec = tabhost.newTabSpec(Integer.toString(i));
            spec.setIndicator(labels[i]);
            spec.setContent(factory);
            tabhost.addTab(spec);
            tabs[i] = spec;
            tabviews[i] = tabWidget.getChildAt(i);
            //(View)(tabviews[i].findViewById(android.R.id.title)).getParent();
        }
    }

}
