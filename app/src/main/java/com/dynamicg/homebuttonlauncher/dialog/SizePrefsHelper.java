package com.dynamicg.homebuttonlauncher.dialog;

import android.widget.SeekBar;

public class SizePrefsHelper {

    public static final int DEFAULT_LABEL_SIZE = 18;
    public static final int DEFAULT_ICON_SIZE = 48;

    public static final int[] LABEL_SIZES = new int[]{0, 12, 14, 16, 18, 20, 22, 24, 26, 28};
    public static final int[] ICON_SIZES = new int[]{32, 36, 40, 48, 56, 64, 72, 96}; // 48 is default so it should be "in the middle"
    public static final int[] NUM_TABS = new int[]{0, 2, 3, 4, 5, 6};

    public static int getSelectedValue(SeekBar bar, int[] values) {
        return values[bar.getProgress()];
    }

    public static void setSeekBar(SeekBar bar, int currentValue, int[] values) {
        int progress = indexOf(currentValue, values, values.length / 2 + 1);
        bar.setProgress(progress);
        bar.setMax(values.length - 1);
    }

    private static int indexOf(int currentValue, int[] values, int defaultIndex) {
        for (int i = 0; i < values.length; i++) {
            if (currentValue == values[i]) {
                return i;
            }
        }
        return defaultIndex; // if not found
    }

}