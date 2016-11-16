package com.dynamicg.homebuttonlauncher.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.OnClickListenerDialogWrapper;
import com.dynamicg.homebuttonlauncher.R;

public class DialogHelper {

    private static final Logger log = new Logger(DialogHelper.class);

    public static void showError(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        TextView body = new TextView(context);
        body.setText(message);
        int padding = DialogHelper.getDimension(R.dimen.appLinePadding);
        body.setPadding(padding, padding, padding, padding);
        builder.setView(body);

        builder.setPositiveButton(R.string.buttonOk, null);
        AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    public static void showCrashReport(Context context, Throwable t) {
        if (log.isDebugEnabled) {
            t.printStackTrace();
        }
    }

    public static void prepareCommonDialog(Dialog d, int bodyLayoutId, int buttonsLayoutId, boolean customHeader) {
        d.setContentView(R.layout.common_dialog);

        ViewStub body = (ViewStub) d.findViewById(R.id.commonDialogBody);
        body.setLayoutResource(bodyLayoutId);
        body.inflate();

        ViewStub buttons = (ViewStub) d.findViewById(R.id.commonDialogButtonPanel);
        buttons.setLayoutResource(buttonsLayoutId);
        buttons.inflate();

        if (!customHeader) {
            d.findViewById(R.id.headerContainer).setVisibility(View.GONE);
        }
    }

    public static void confirm(Context context, int labelId, OnClickListenerDialogWrapper okListener) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        String label = context.getString(labelId) + "?";
        b.setTitle(label);
        b.setPositiveButton(R.string.buttonOk, okListener);
        b.setNegativeButton(R.string.buttonCancel, null);
        AlertDialog dialog = b.show();
        dialog.setCanceledOnTouchOutside(false);
    }

    public static int getDimension(int dimensionId) {
        return (int) GlobalContext.resources.getDimension(dimensionId);
    }

    public static void underline(SpannableString str, int underlineFrom, int underlineTo) {
        str.setSpan(new UnderlineSpan(), underlineFrom, underlineTo, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void bold(SpannableString str, int underlineFrom, int underlineTo) {
        str.setSpan(new StyleSpan(Typeface.BOLD), underlineFrom, underlineTo, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void openLabelEditor(final Context context
            , final String defaultLabel
            , final int inputType // e.g. InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            , final TextEditorListener callback
            , final View extraActions
    ) {
        final String label = defaultLabel != null ? defaultLabel : "";
        final EditText editor = new EditText(context);
        editor.setText(label);
        editor.setSingleLine();
        if (label.length() > 0) {
            editor.setSelection(label.length());
        }
        editor.setInputType(inputType);

        final DialogInterface.OnClickListener okListener = new OnClickListenerDialogWrapper(context) {
            @Override
            public void onClickImpl(DialogInterface dialog, int which) {
                String newLabel = editor.getText().toString();
                newLabel = newLabel != null ? newLabel.trim() : "";
                callback.onTextChanged(newLabel);
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.buttonOk, okListener);
        builder.setNegativeButton(R.string.buttonCancel, null);

        LinearLayout body = new LinearLayout(context);
        body.setOrientation(LinearLayout.VERTICAL);
        body.addView(editor);
        if (extraActions != null) {
            body.addView(extraActions);
        }
        builder.setView(body);

        final AlertDialog dialog = builder.show();
        dialog.setCanceledOnTouchOutside(false);

        editor.setOnKeyListener(new EditText.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    okListener.onClick(dialog, Dialog.BUTTON_POSITIVE);
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });

        // auto open keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static interface TextEditorListener {
        public void onTextChanged(String text);
    }

}
