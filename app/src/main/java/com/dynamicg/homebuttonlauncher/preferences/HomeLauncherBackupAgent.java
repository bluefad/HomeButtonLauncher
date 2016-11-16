package com.dynamicg.homebuttonlauncher.preferences;

import android.app.backup.BackupAgentHelper;

// removed as of V3.0 (we have Google Drive Backup/Restore in place instead)
public class HomeLauncherBackupAgent extends BackupAgentHelper {
    @Override
    public void onCreate() {
    }
}
