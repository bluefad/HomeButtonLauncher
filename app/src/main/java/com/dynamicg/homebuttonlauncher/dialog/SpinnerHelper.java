package com.dynamicg.homebuttonlauncher.dialog;

import android.app.Dialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class SpinnerHelper {

    private static final String PADDING = "   ";
    public static final String PADDED_DASH = PADDING + "\u2013" + PADDING;

    public final Spinner spinner;

    public SpinnerHelper(Dialog dialog, int id) {
        this.spinner = (Spinner) dialog.findViewById(id);
    }

    public SpinnerHelper(View spinner) {
        this.spinner = (Spinner) spinner;
    }

    public void bind(SpinnerEntries items, int selectedPosition) {
        final ArrayAdapter<SpinnerEntry> adapter = new ArrayAdapter<SpinnerEntry>(spinner.getContext(), android.R.layout.simple_spinner_item, items.list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selectedPosition);
    }

    public int getSelectedValue() {
        return ((SpinnerEntry) spinner.getSelectedItem()).value;
    }

    public static class SpinnerEntry {
        final int value;
        final String label;

        public SpinnerEntry(int value, String label) {
            super();
            this.value = value;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static class SpinnerEntries {
        public final ArrayList<SpinnerEntry> list = new ArrayList<SpinnerEntry>();

        public void add(int value, String label) {
            list.add(new SpinnerEntry(value, label));
        }

        public void addPadded(int value, int label) {
            list.add(new SpinnerEntry(value, PADDING + label + PADDING));
        }
    }

}
