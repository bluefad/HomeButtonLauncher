package com.dynamicg.homebuttonlauncher;

public class HBLConstants {

    public static final String SELF = "com.dynamicg.homebuttonlauncher/com.dynamicg.homebuttonlauncher.MainActivityOpen";

    public static final String PREFS_APPS = "apps";
    public static final String PREFS_SETTINGS = "settings";

    public static final int MENU_APPS_ADD = 1;
    public static final int MENU_APPS_REMOVE = 2;
    public static final int MENU_APPS_SORT = 3;
    public static final int MENU_PREFERENCES = 5;
    public static final int MENU_SHOW_APP_DETAILS = 6;
    public static final int SHOW_PLAY_STORE = 7;
    public static final int MENU_RESET = 8;
    public static final int MENU_DRIVE_BACKUP = 9;
    public static final int MENU_DRIVE_RESTORE = 10;
    public static final int MENU_SDCARD_BACKUP = 11;
    public static final int MENU_SDCARD_RESTORE = 12;
    public static final int MENU_BLANK = 99;

    public static final int SHORTCUT_RC = 31;

    /*
     * for whatever reason the "Google Now" widget opens the "assist" app when clicked, passing the following extras.
     * if we get those we forward to the Google Now app
     */
    public static final String GOOGLE_NOW_EXTRA = "assist_intent_source";
}
