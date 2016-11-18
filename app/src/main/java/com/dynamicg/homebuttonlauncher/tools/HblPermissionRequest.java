package com.dynamicg.homebuttonlauncher.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.SoftReference;

public abstract class HblPermissionRequest {

    public static final boolean PRE_SDK23 = android.os.Build.VERSION.SDK_INT < 23;
    public static final String SD_CARD_READ = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String SD_CARD_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PHONE_CALL = Manifest.permission.CALL_PHONE;
    private static final int RC_ASK_PERMISSION = 31;
    private static SoftReference<HblPermissionRequest> currentRequest = null;

    public HblPermissionRequest(Activity activity, String permissionName) {
        if (PRE_SDK23) {
            throw new UnsupportedOperationException("do not call this with sdk<23");
        }
        if (hasPermission(activity, permissionName)) {
            onPermissionResult(true);
        } else {
            currentRequest = new SoftReference<HblPermissionRequest>(this);
            ActivityCompat.requestPermissions(activity, new String[]{permissionName}, RC_ASK_PERMISSION);
        }
    }

    public static boolean hasPermission(Context context, String permissionName) {
        return ContextCompat.checkSelfPermission(context, permissionName) == PackageManager.PERMISSION_GRANTED;
    }

    public static void onRequestPermissionsResult(int grantResult) {
        HblPermissionRequest request = currentRequest != null ? currentRequest.get() : null;
        if (request == null) {
            return;
        }
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            request.onPermissionResult(true);
        } else if (grantResult == PackageManager.PERMISSION_DENIED) {
            request.onPermissionResult(false);
        }
    }

    public abstract void onPermissionResult(boolean permissionGranted);
}
