package com.dynamicg.homebuttonlauncher.dialog;

import android.app.Dialog;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dynamicg.homebuttonlauncher.R;

public class SeekBarHelper {

    private static final int TAG_NEW_VALUE = R.id.buttonOk;

    public final SeekBar bar;
    public final int initialValue;
    private final Dialog dialog;
    private final int[] values;
    private ValueChangeListener onValueChangeListener;

    public SeekBarHelper(Dialog dialog, final int id, final int[] values, final int initialValue) {
        this.dialog = dialog;
        this.values = values;
        this.bar = (SeekBar) dialog.findViewById(id);
        SizePrefsHelper.setSeekBar(bar, initialValue, values);
        this.initialValue = initialValue;
        bar.setTag(TAG_NEW_VALUE, initialValue);
    }

    public void attachDefaultIndicator(final int indicatorId) {
        final TextView indicator = (TextView) dialog.findViewById(indicatorId);
        bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int selectedValue = SizePrefsHelper.getSelectedValue(bar, values);
                    indicator.setText("[" + selectedValue + "]");
                    bar.setTag(TAG_NEW_VALUE, selectedValue);
                    if (onValueChangeListener != null) {
                        onValueChangeListener.valueChanged(selectedValue);
                    }
                }
            }
        });
    }

    public int getNewValue() {
        return (Integer) bar.getTag(TAG_NEW_VALUE);
    }

    public void setOnValueChangeListener(ValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }

}
