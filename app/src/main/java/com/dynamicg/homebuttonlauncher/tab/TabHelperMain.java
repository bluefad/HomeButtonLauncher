package com.dynamicg.homebuttonlauncher.tab;

import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TabHost;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.OnLongClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.SpinnerHelper;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper.TextEditorListener;

public class TabHelperMain extends TabHelper {

    private static final Logger log = new Logger(TabHelperMain.class);
    private final PreferencesManager preferences;

    public TabHelperMain(MainActivityHome activity, PreferencesManager preferences) {
        super(activity
                , preferences.prefSettings.getNumTabs()
                , activity.findViewById(R.id.headerContainer)
                , preferences.prefSettings.isTabAtBottom()
        );
        this.preferences = preferences;
    }

    @Override
    public TabHost bindTabs() {
        final int selectedIndex = preferences.getTabIndex();

        TabHost.OnTabChangeListener onTabChangeListener = new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                int tabindex = Integer.parseInt(tabId);
                log.debug("onTabChanged", tabindex);
                if (preferences.getTabIndex() != tabindex) {
                    activity.updateOnTabSwitch(tabindex);
                }
                decorate(tabindex);
            }

            ;
        };

        View.OnLongClickListener longClickListener = new OnLongClickListenerWrapper() {
            @Override
            public boolean onLongClickImpl(View v) {
                int index = (Integer) v.getTag(R.id.tag_tab_index);
                editLabel(index);
                return true;
            }
        };

        String[] labels = new String[numTabs];
        for (int i = 0; i < numTabs; i++) {
            labels[i] = preferences.getTabTitle(i);
        }

        TabHost host = bindTabs(selectedIndex, labels, onTabChangeListener, longClickListener);
        decorate(selectedIndex);
        return host;
    }

    protected void editLabel(final int tabindex) {

		/*
         * switch tab position
		 */
        SpinnerHelper.SpinnerEntries items = new SpinnerHelper.SpinnerEntries();
        items.add(-1, SpinnerHelper.PADDED_DASH);
        for (int i = 0; i < preferences.prefSettings.getNumTabs(); i++) {
            if (i != tabindex) {
                items.addPadded(i, i + 1);
            }
        }

        ViewGroup panel = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.edit_tab, null);

        final SpinnerHelper switchTabSpinner = new SpinnerHelper(panel.findViewById(R.id.editTabNewPosition));
        switchTabSpinner.bind(items, 0);

        final SeekBar heightSeekBar = (SeekBar) panel.findViewById(R.id.editTabHeight);
        heightSeekBar.setMax(4);
        heightSeekBar.setProgress(preferences.getTabExtraHeight());

		/*
         * label editor
		 */
        final String currentLabel = preferences.getTabTitle(tabindex);
        TextEditorListener callback = new DialogHelper.TextEditorListener() {
            @Override
            public void onTextChanged(String text) {
                preferences.writeTabTitle(tabindex, text);
                preferences.saveTabExtraHeight(heightSeekBar.getProgress());
                applyMoveTab(tabindex, switchTabSpinner);
                activity.redrawTabContainer();
            }
        };

        DialogHelper.openLabelEditor(context, currentLabel, InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, callback, panel);
    }

    private void applyMoveTab(int tabindex, SpinnerHelper switchTabSpinner) {
        int newTabIndex = switchTabSpinner.getSelectedValue();
        if (newTabIndex >= 0) {
            try {
                preferences.exchangeTabData(tabindex, newTabIndex);
                activity.forceTabSwitch(newTabIndex);
            } catch (Throwable t) {
                DialogHelper.showCrashReport(context, t);
            }
        }
    }

    public TabHost redraw() {
        View tabContainer = activity.findViewById(android.R.id.tabhost);
        if (tabContainer != null) {
            ((ViewGroup) tabContainer.getParent()).removeView(tabContainer);
        }

        if (numTabs == 0) {
            return null;
        }

        return bindTabs();
    }

    // lollipop does not decorate TabHost tabs "touch event"
    protected void decorate(int currentTabIndex) {
        int themeId = preferences.prefSettings.getThemeId();
        if (preferences.prefSettings.getThemeId() != PrefSettings.THEME_TRANSPARENT) {
            for (int i = 0; i < tabviews.length; i++) {
                BackgroundHelper.setBackground(themeId, tabviews[i], i == currentTabIndex);
            }
        }
    }
}
