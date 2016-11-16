package com.dynamicg.homebuttonlauncher.tools;

import android.content.Context;
import android.content.Intent;

import com.dynamicg.homebuttonlauncher.AppEntry;

public class DirectDialWrapper {

    private static final String ACTION_CALL_STANDARD = Intent.ACTION_CALL; // "android.intent.action.CALL"
    private static final String ACTION_CALL_PRIVILEGED = "android.intent.action.CALL_PRIVILEGED";
    private static final String ACTION_DIAL = Intent.ACTION_DIAL;

    public static void onShortcutCreation(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_CALL_STANDARD.equals(action) || ACTION_CALL_PRIVILEGED.equals(action)) {
            intent.setAction(Intent.ACTION_DIAL);
        }
    }

    public static void onShortcutOpen(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent != null ? intent.getAction() : null;
        if (action == null) {
            return;
        }

        if (HblPermissionRequest.PRE_SDK23) {
            // action_call requires android.permission.CALL_PHONE so we change it to "dial" (which opens the dial pad with the given phone number)
            // => comment this out and enable "android.permission.CALL_PHONE" in the manifest for enabling "direct dial"
            if (ACTION_CALL_STANDARD.equals(action) || ACTION_CALL_PRIVILEGED.equals(action)) {
                intent.setAction(ACTION_DIAL);
            }
        } else {
            // M runtime permission
            if (ACTION_CALL_STANDARD.equals(action) || ACTION_CALL_PRIVILEGED.equals(action) || ACTION_DIAL.equals(action)) {
                String updatedAction = HblPermissionRequest.hasPermission(context, HblPermissionRequest.PHONE_CALL) ? ACTION_CALL_STANDARD : ACTION_DIAL;
                intent.setAction(updatedAction);
            }
        }
    }

    public static boolean isPermissionRequired(AppEntry entry) {
        if (HblPermissionRequest.PRE_SDK23 || !entry.isShortcut()) {
            return false;
        }
        try {
            Intent intent = ShortcutHelper.getIntent(entry);
            String action = intent != null ? intent.getAction() : null;
            return ACTION_CALL_STANDARD.equals(action) || ACTION_CALL_PRIVILEGED.equals(action) || ACTION_DIAL.equals(action);
        } catch (Throwable t) {
            return false;
        }
    }
}
