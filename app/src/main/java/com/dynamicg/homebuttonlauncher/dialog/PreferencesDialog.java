package com.dynamicg.homebuttonlauncher.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SdkLevel;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.OnClickListenerWrapper;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderAbstract;
import com.dynamicg.homebuttonlauncher.dialog.header.HeaderPreferences;
import com.dynamicg.homebuttonlauncher.preferences.IntHolder;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;
import com.dynamicg.homebuttonlauncher.preferences.PreferencesManager;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

@SuppressLint("HandlerLeak")
public class PreferencesDialog extends Dialog {

    private static final Logger log = new Logger(PreferencesDialog.class);

    private final PreferencesManager preferences;
    private final PrefSettings prefSettings;
    private final MainActivityHome activity;

    private final IntHolder theme;
    private final IntHolder layout;

    private SeekBarHelper seekbarLabelSize;
    private SeekBarHelper seekbarIconSize;
    private SeekBarHelper seekbarNumTabs;
    private TransparencyAlphaHelper transparencyAlphaHelper;

    private CheckBox chkHighRes;
    private CheckBox chkAutoStartSingle;
    private CheckBox chkStatusLine;
    private CheckBox chkNoHeader;

    private SpinnerHelper homeTabHelper;
    private RadioGroup rgTabPosition;

    public PreferencesDialog(MainActivityHome activity, PreferencesManager preferences) {
        super(activity);
        setCanceledOnTouchOutside(false);
        this.activity = activity;
        this.preferences = preferences;
        this.prefSettings = preferences.prefSettings;
        this.theme = new IntHolder(prefSettings.getThemeId());
        this.layout = new IntHolder(prefSettings.getLayoutType());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    private static int getSelectedRadioButtonValue(RadioGroup grp) {
        View rb = grp.findViewById(grp.getCheckedRadioButtonId());
        return Integer.parseInt(rb.getTag().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTitle(R.string.preferences);

        DialogHelper.prepareCommonDialog(this, R.layout.preferences_body, R.layout.button_panel_2, true);

        HeaderAbstract header = new HeaderPreferences(this, activity);
        header.attach(R.string.preferences);

        seekbarLabelSize = new SeekBarHelper(this, R.id.prefsLabelSize, SizePrefsHelper.LABEL_SIZES, prefSettings.getLabelSize());
        seekbarLabelSize.attachDefaultIndicator(R.id.prefsLabelSizeIndicator);

        seekbarIconSize = new SeekBarHelper(this, R.id.prefsIconSize, SizePrefsHelper.ICON_SIZES, prefSettings.getIconSize());
        seekbarIconSize.attachDefaultIndicator(R.id.prefsIconSizeIndicator);

        seekbarNumTabs = new SeekBarHelper(this, R.id.prefsNumTabs, SizePrefsHelper.NUM_TABS, prefSettings.getNumTabs());
        seekbarNumTabs.attachDefaultIndicator(R.id.prefsNumTabsIndicator);

        rgTabPosition = attachRadioGroup(R.id.prefsTabPosition, prefSettings.getTabPosition());

        chkHighRes = attachCheckbox(R.id.prefsHighResIcon, prefSettings.isHighResIcons());
        chkAutoStartSingle = attachCheckbox(R.id.prefsAutoStartSingle, prefSettings.isAutoStartSingle());
        chkStatusLine = attachCheckbox(R.id.prefsStatusLine, prefSettings.isShowStatusLine());
        chkNoHeader = attachCheckbox(R.id.prefsNoHeader, prefSettings.isNoHeader());

        transparencyAlphaHelper = new TransparencyAlphaHelper();

        bindTogglePanel(R.id.prefThemeToggle, PrefSettings.NUM_THEMES, theme);
        bindTogglePanel(R.id.prefLayoutToggle, PrefSettings.NUM_LAYOUTS, layout);

        findViewById(R.id.buttonCancel).setOnClickListener(new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                dismiss();
            }
        });

        findViewById(R.id.buttonOk).setOnClickListener(new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                saveSettings();
            }
        });

        attachTabExtras();
    }

    private void attachTabExtras() {
        homeTabHelper = new SpinnerHelper(this, R.id.prefsHomeTab);
        final View containerHomeTab = findViewById(R.id.prefsHomeTabContainer);
        final View containerTabPosition = findViewById(R.id.prefsTabPositionContainer);

        final ValueChangeListener spinnerUpdateHandler = new ValueChangeListener() {
            @Override
            public void valueChanged(final int previousHomeTab) {
                final int maxTabs = seekbarNumTabs.getNewValue(); // this is 0,2,3,...
                final int newHomeTab = maxTabs >= previousHomeTab ? previousHomeTab : 0; // previousHomeTab is tabnum not tabindex.

                log.debug("tabs max/currentHome", maxTabs, newHomeTab);
                SpinnerHelper.SpinnerEntries items = new SpinnerHelper.SpinnerEntries();
                items.add(0, SpinnerHelper.PADDED_DASH); // pos 0 = none
                for (int idx = 0; idx < maxTabs; idx++) {
                    // pos 1 to n is "tabindex+1"
                    items.addPadded(idx + 1, idx + 1);
                }
                homeTabHelper.bind(items, newHomeTab);

                // apply visibility
                boolean hasTabs = maxTabs > 0;
                containerHomeTab.setVisibility(hasTabs ? View.VISIBLE : View.GONE);
                containerTabPosition.setVisibility(hasTabs ? View.VISIBLE : View.GONE);
            }
        };

        seekbarNumTabs.setOnValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChanged(int newValue) {
                // reuse current selection if applicable
                spinnerUpdateHandler.valueChanged(homeTabHelper.getSelectedValue());
            }
        });

        // initial setup
        spinnerUpdateHandler.valueChanged(prefSettings.getHomeTabNum());
    }

    private CheckBox attachCheckbox(int id, boolean checked) {
        CheckBox box = (CheckBox) findViewById(id);
        box.setChecked(checked);
        return box;
    }

    private RadioGroup attachRadioGroup(int id, int selectedIndex) {
        RadioGroup rg = (RadioGroup) findViewById(id);
        int checkedIndex = rg.getChildCount() > selectedIndex ? selectedIndex : 0;
        rg.check(rg.getChildAt(checkedIndex).getId());
        return rg;
    }

    private void onToggleChanged(IntHolder valueHolder, int value) {
        // this also fires on startup
        if (valueHolder == theme) {
            transparencyAlphaHelper.setVisibility(value == PrefSettings.THEME_TRANSPARENT);
        }
    }

    private void setToggleSelection(View parent, int numItems, IntHolder valueHolder, int value) {
        int highlightColorId;
        if (SdkLevel.BEFORE_LOLLIPOP) {
            highlightColorId = android.R.color.holo_blue_light;
        } else {
            highlightColorId = prefSettings.getThemeId() == PrefSettings.THEME_LIGHT ? R.color.l5AccentLight : R.color.l5AccentDark;
        }
        int highlightColorValue = getContext().getResources().getColor(highlightColorId);
        valueHolder.value = value;
        for (int i = 0; i < numItems; i++) {
            View toggle = parent.findViewWithTag("toggle_" + i);
            toggle.setBackgroundColor(value == i ? highlightColorValue : 0);
        }
        onToggleChanged(valueHolder, value);
    }

    private void bindTogglePanel(final int panelId, final int numItems, final IntHolder valueHolder) {
        final ViewGroup parent = (ViewGroup) findViewById(panelId);
        final View.OnClickListener clickListener = new OnClickListenerWrapper() {
            @Override
            public void onClickImpl(View v) {
                int which = Integer.parseInt(v.getTag().toString());
                setToggleSelection(parent, numItems, valueHolder, which);
            }
        };
        for (int i = 0; i < numItems; i++) {
            View image = parent.findViewWithTag(Integer.toString(i));
            image.setOnClickListener(clickListener);
        }
        setToggleSelection(parent, numItems, valueHolder, valueHolder.value);
    }

    private int getNewHomeTabNum() {
        return seekbarNumTabs.getNewValue() > 0 ? homeTabHelper.getSelectedValue() : 0;
    }

    private boolean isTabRefreshRequired() {
        final int currentTabIndex = preferences.getTabIndex();
        final int oldNumTabs = seekbarNumTabs.initialValue;
        final int newNumTabs = seekbarNumTabs.getNewValue();
        final int oldHomeTabNum = prefSettings.getHomeTabNum();
        final int newHomeTabNum = getNewHomeTabNum();

        if (currentTabIndex >= newNumTabs || (oldHomeTabNum > 0 && newHomeTabNum == 0)) {
            // reset to first tab
            preferences.updateCurrentTabIndex(0);
            return true;
        } else if (newHomeTabNum > 0 && newHomeTabNum != oldHomeTabNum) {
            // changed home tab
            preferences.updateCurrentTabIndex(newHomeTabNum - 1);
            return true;
        }

        return oldNumTabs != newNumTabs;
    }

    private void saveSettings() {
        final boolean appRestartRequired = isAppRestartRequired();
        final boolean tabRefreshRequired = isTabRefreshRequired();
        saveSharedPrefs();
        if (tabRefreshRequired) {
            activity.redrawTabContainer();
        }

        GlobalContext.resetCache();
        activity.refreshList();

        dismiss();
        if (appRestartRequired) {
            activity.recreate();
        }
    }

    private boolean isAppRestartRequired() {
        return (prefSettings.getThemeId() != theme.value)
                || (prefSettings.isShowStatusLine() != chkStatusLine.isChecked())
                || (prefSettings.getTabPosition() != getSelectedRadioButtonValue(rgTabPosition))
                || (prefSettings.isNoHeader() != chkNoHeader.isChecked())
                || transparencyAlphaHelper.isChanged()
                ;
    }

    private void saveSharedPrefs() {
        Editor edit = prefSettings.sharedPrefs.edit();

        edit.putInt(PrefSettings.KEY_THEME, theme.value);
        edit.putInt(PrefSettings.KEY_LAYOUT, layout.value);
        edit.putInt(PrefSettings.KEY_LABEL_SIZE, seekbarLabelSize.getNewValue());
        edit.putInt(PrefSettings.KEY_ICON_SIZE, seekbarIconSize.getNewValue());
        edit.putInt(PrefSettings.KEY_NUM_TABS, seekbarNumTabs.getNewValue());
        edit.putInt(PrefSettings.KEY_TRANS_ALPHA, transparencyAlphaHelper.getNewValue());
        edit.putInt(PrefSettings.KEY_HOME_TAB_NUM, getNewHomeTabNum());

        edit.putBoolean(PrefSettings.KEY_HIGH_RES, chkHighRes.isChecked());
        edit.putBoolean(PrefSettings.KEY_AUTO_START_SINGLE, chkAutoStartSingle.isChecked());
        edit.putBoolean(PrefSettings.KEY_STATUS_LINE, chkStatusLine.isChecked());
        edit.putBoolean(PrefSettings.KEY_NO_HEADER, chkNoHeader.isChecked());

        edit.putInt(PrefSettings.KEY_TAB_POSITION, getSelectedRadioButtonValue(rgTabPosition));

        edit.apply();
    }

    public class TransparencyAlphaHelper {
        final int offset = 155;
        final int max = 50;
        final int mod = 2;
        final int original = prefSettings.getTransparencyAlpha();
        final SeekBar bar = (SeekBar) findViewById(R.id.prefsTransparencyAlpha);

        TransparencyAlphaHelper() {
            bar.setProgress((original - offset) / mod);
            bar.setMax(max);
        }

        int getNewValue() {
            return bar.getProgress() * mod + offset;
        }

        void setVisibility(boolean visible) {
            bar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        boolean isChanged() {
            return original != getNewValue();
        }
    }

}
