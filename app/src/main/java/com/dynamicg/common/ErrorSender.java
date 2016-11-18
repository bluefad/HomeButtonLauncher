package com.dynamicg.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.dynamicg.homebuttonlauncher.R;

import java.util.Locale;

public class ErrorSender {

    private static void createIntent(Context context, String title, String body) {
        Intent msg = new Intent(Intent.ACTION_SEND);
        //msg.setType("text/plain");
        msg.setType("message/rfc822");
        msg.putExtra(Intent.EXTRA_SUBJECT, title);
        msg.putExtra(Intent.EXTRA_TEXT, body);
        if (AppSignature.isMatchingCertificate(context)) {
            msg.putExtra(Intent.EXTRA_EMAIL, new String[]{"dynamicg.info@gmail.com"});
        }
        context.startActivity(Intent.createChooser(msg, "Send error report"));
    }

    private static void emailError(Context context, String alertTitle, Throwable exception) {
        String version = SystemUtil.getVersion(context);
        String title = context.getString(R.string.app_name) + " " + version + " Error (" + Locale.getDefault().getLanguage() + ")";
        String body = alertTitle + "\n\n"
                + DeviceInfo.getDeviceInfo() + "\n\n"
                + SystemUtil.getExceptionText(exception);
        createIntent(context, title, body);
    }

    public static void notifyError(final Context context, final String title, final Throwable e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(e.toString());
        builder.setPositiveButton("Email Dev", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                emailError(context, title, e);
            }
        });
        builder.setNegativeButton("Close", null);
        AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);
    }

}
