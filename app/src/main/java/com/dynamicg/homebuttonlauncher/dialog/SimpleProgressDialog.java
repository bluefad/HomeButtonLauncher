package com.dynamicg.homebuttonlauncher.dialog;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

@SuppressLint("HandlerLeak")
public abstract class SimpleProgressDialog {

    private static final int MSG_ERROR = -1;

    private final ProgressDialog progressDialog;
    private final Handler doneHandler;

    public SimpleProgressDialog(final Context context, final String title) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(title);
        progressDialog.setCancelable(false);
        progressDialog.show();

        doneHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                if (msg.what == MSG_ERROR) {
                    DialogHelper.showCrashReport(context, (Throwable) msg.obj);
                } else {
                    try {
                        done();
                    } catch (Throwable t) {
                        DialogHelper.showCrashReport(context, t);
                    }
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    backgroundWork();
                    doneHandler.sendEmptyMessage(0);
                } catch (Throwable e) {
                    Message msg = Message.obtain(doneHandler, MSG_ERROR, e);
                    doneHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    public abstract void backgroundWork();

    public abstract void done();

}
