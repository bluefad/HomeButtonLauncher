package com.dynamicg.homebuttonlauncher;

import android.view.View;
import android.widget.AdapterView;

import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class OnAdapterItemClickListenerWrapper implements AdapterView.OnItemClickListener {

    @Override
    public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            onItemClickImpl(parent, view, position, id);
        } catch (Throwable t) {
            DialogHelper.showCrashReport(view.getContext(), t);
        }
    }

    public abstract void onItemClickImpl(AdapterView<?> parent, View view, int position, long id);

}
