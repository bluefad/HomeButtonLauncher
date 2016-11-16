package com.dynamicg.homebuttonlauncher;

import android.view.View;
import android.view.View.OnClickListener;

import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class OnClickListenerWrapper implements OnClickListener {

    @Override
    public final void onClick(View view) {
        try {
            onClickImpl(view);
        } catch (Throwable t) {
            DialogHelper.showCrashReport(view.getContext(), t);
        }
    }

    public abstract void onClickImpl(View view);

}
