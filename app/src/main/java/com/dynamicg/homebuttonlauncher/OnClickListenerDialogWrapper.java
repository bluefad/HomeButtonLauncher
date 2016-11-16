package com.dynamicg.homebuttonlauncher;

import android.content.Context;
import android.content.DialogInterface;

import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class OnClickListenerDialogWrapper implements DialogInterface.OnClickListener {

    private final Context context;

    public OnClickListenerDialogWrapper(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        try {
            onClickImpl(dialog, which);
        } catch (Throwable t) {
            DialogHelper.showCrashReport(context, t);
        }
    }

    public abstract void onClickImpl(DialogInterface dialog, int which);

}
