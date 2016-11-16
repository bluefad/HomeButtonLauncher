package com.dynamicg.homebuttonlauncher;

import android.view.View;

import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class OnLongClickListenerWrapper implements View.OnLongClickListener {

    @Override
    public final boolean onLongClick(View view) {
        try {
            return onLongClickImpl(view);
        } catch (Throwable t) {
            DialogHelper.showCrashReport(view.getContext(), t);
            return false;
        }
    }

    public abstract boolean onLongClickImpl(View v);
}
