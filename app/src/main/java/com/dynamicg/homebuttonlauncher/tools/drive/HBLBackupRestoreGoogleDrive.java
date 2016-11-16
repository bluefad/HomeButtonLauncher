package com.dynamicg.homebuttonlauncher.tools.drive;

import android.app.Dialog;
import android.content.Intent;

import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class HBLBackupRestoreGoogleDrive extends HBLBackupRestore {

    public static final String GOOGLE_DRIVE_FOLDER_NAME = "HomeButtonLauncher";
    public static final String GOOGLE_DRIVE_FILE_NAME = "settings.xml.gz";

    private static WeakReference<MainActivityHome> refActivity;
    private static WeakReference<Dialog> refDialog;

    public HBLBackupRestoreGoogleDrive(MainActivityHome activity, PreferencesDialog dialog) {
        super(activity, dialog);
    }

    public static void restoreFromFile(Intent data) {
        String path = data != null ? data.getStringExtra(GoogleDriveGlobals.KEY_FNAME_ABS) : null;
        if (path == null || path.length() == 0) {
            return;
        }
        File file = new File(path);
        MainActivityHome activity = refActivity != null ? refActivity.get() : null;
        if (file != null && activity != null) {
            restoreImpl(activity, refDialog.get(), file, true);
        }
    }

    @Override
    public File getBackupFile() throws IOException {
        File file = new File(context.getFilesDir(), GOOGLE_DRIVE_FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        file.setReadable(true, false); // read=true, owner=false
        file.setWritable(true, false);
        return file;
    }

    @Override
    public boolean isReady() {
        if (!GoogleDriveUtil.isPluginAvailable(context)) {
            GoogleDriveUtil.alertMissingPlugin(context);
            return false;
        }
        return true;
    }

    @Override
    public void executeUpload(File file) {
        GoogleDriveUtil.upload(context, file);
    }

    @Override
    public void triggerImport(Dialog dialog) {
        try {
            refActivity = new WeakReference<MainActivityHome>(activity);
            refDialog = new WeakReference<Dialog>(dialog);
            GoogleDriveUtil.startDownload(activity, getBackupFile());
        } catch (Throwable t) {
            DialogHelper.showCrashReport(context, t);
        }
    }

    @Override
    public int getTitleResId(int action) {
        if (action == HBLConstants.MENU_DRIVE_BACKUP) {
            return R.string.prefsDriveBackup;
        }
        if (action == HBLConstants.MENU_DRIVE_RESTORE) {
            return R.string.prefsDriveRestore;
        }
        return 0;
    }

}
