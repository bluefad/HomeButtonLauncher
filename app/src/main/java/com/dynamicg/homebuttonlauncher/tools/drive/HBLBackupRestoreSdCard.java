package com.dynamicg.homebuttonlauncher.tools.drive;

import android.app.Dialog;
import android.os.Environment;
import android.widget.Toast;

import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.PreferencesDialog;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

import java.io.File;

public class HBLBackupRestoreSdCard extends HBLBackupRestore {

    public HBLBackupRestoreSdCard(MainActivityHome activity, PreferencesDialog dialog) {
        super(activity, dialog);
    }

    @Override
    public File getBackupFile() {
        return new File(Environment.getExternalStorageDirectory(), "homeButtonLauncher.xml.gz");
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void executeUpload(File file) {
        // nothing to upload
        Toast.makeText(context, "Done. File: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void triggerImport(Dialog dialog) {
        File file = getBackupFile();
        if (file.canRead()) {
            restoreImpl(activity, dialog, file, false);
        } else {
            DialogHelper.showError(context, "File not found", "File: " + file.getAbsolutePath());
        }
    }

    @Override
    public int getTitleResId(int action) {
        if (action == HBLConstants.MENU_SDCARD_BACKUP) {
            return R.string.prefsSdCardBackup;
        }
        if (action == HBLConstants.MENU_SDCARD_RESTORE) {
            return R.string.prefsSdCardRestore;
        }
        return 0;
    }

}
