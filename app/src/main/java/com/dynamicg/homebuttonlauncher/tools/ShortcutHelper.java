package com.dynamicg.homebuttonlauncher.tools;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.widget.CheckBox;

import com.dynamicg.common.FileUtil;
import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.MainActivityHome;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;
import com.dynamicg.homebuttonlauncher.tools.drive.Hex;
import com.dynamicg.homebuttonlauncher.tools.icons.IconProvider;
import com.dynamicg.homebuttonlauncher.tools.icons.LargeIconLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;

/*
 * shortcut data:
 * "sc-<id>|<iconres-pkg>,<iconres-path>#<label>" is the component, used as key on the 'shortlist' settings
 * "sc-<id>" is the shortcut id, used as key on 'prefSettings' to save the intent and to write the icon to the disk
 * "<iconres>" is something like "com.android.settings:mipmap/ic_launcher_settings" according to shortcut_icon_resource
 */
public class ShortcutHelper {

    private static final Logger log = new Logger(ShortcutHelper.class);

    private static final String PREFIX_SHORTCUT = "sc-";
    private static final String EXTRA_PREFIX_KEEP_OPEN = "keepOpen.";
    private static final String SEPARATOR_RES = "|";
    private static final String SEPARATOR_PKG = ",";
    private static final String SEPARATOR_LABEL = "#";

    private static final String KEY_SC_MAXID = "sc-max";
    private static final String PNG = ".png";

    private static WeakReference<AppConfigDialog> dialogRef;

    private static File iconDir;

    public static boolean isShortcutComponent(String component) {
        return component.startsWith(PREFIX_SHORTCUT) && component.contains(SEPARATOR_RES);
    }

    public static String getShortcutId(String component) {
        return component.substring(0, component.indexOf(SEPARATOR_RES));
    }

    public static String getShortcutId(AppEntry entry) {
        return getShortcutId(entry.getComponent());
    }

    public static String getLabel(String component) {
        try {
            return component.substring(component.indexOf(SEPARATOR_LABEL) + 1, component.length());
        } catch (IndexOutOfBoundsException e) {
            return component; // corrupt prefs?
        }
    }

    public static void shortcutSelected(final MainActivityHome activity, final Intent data) {
        if (activity == null || data == null) {
            return;
        }

        Bundle bundle = data.getExtras();
        if (bundle == null) {
            return;
        }

        final Intent intent = (Intent) bundle.getParcelable(Intent.EXTRA_SHORTCUT_INTENT);
        final Bitmap icon = (Bitmap) bundle.getParcelable(Intent.EXTRA_SHORTCUT_ICON);
        final Intent.ShortcutIconResource iconResource = bundle.getParcelable(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
        final String name = bundle.getString(Intent.EXTRA_SHORTCUT_NAME);
        if (intent == null) {
            return;
        }

        log.debug("SHORTCUT", name, icon, iconResource, intent);

        DirectDialWrapper.onShortcutCreation(intent);

        final ExtraToggle extraToggle = new ExtraToggle(activity, intent);
        DialogHelper.TextEditorListener callback = new DialogHelper.TextEditorListener() {
            @Override
            public void onTextChanged(String text) {
                save(activity, icon, iconResource, intent, text, extraToggle);
            }
        };
        DialogHelper.openLabelEditor(activity, name, InputType.TYPE_TEXT_FLAG_CAP_WORDS, callback, extraToggle.box);
    }

    protected static int getAndIncrementNextId() {
        final int nextid = GlobalContext.prefSettings.getIntValue(KEY_SC_MAXID) + 1;
        GlobalContext.prefSettings.apply(KEY_SC_MAXID, nextid);
        return nextid;
    }

    private static void save(
            MainActivityHome activity
            , Bitmap bitmap
            , Intent.ShortcutIconResource iconResource
            , Intent intent
            , String label
            , ExtraToggle extraToggle
    ) {
        final String shortcutId = PREFIX_SHORTCUT + getAndIncrementNextId();
        final String intentString;
        if (extraToggle.isResolveContactId()) {
            String contactId = intent.getData().getLastPathSegment();
            Uri contactUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
            Intent contactIntent = new Intent(Intent.ACTION_VIEW, contactUri);
            intentString = contactIntent.toUri(0);
        } else {
            intentString = intent.toUri(0);
        }

        GlobalContext.prefSettings.apply(shortcutId, intentString);
        if (extraToggle.isKeepHomeLauncherOpen()) {
            GlobalContext.prefSettings.apply(EXTRA_PREFIX_KEEP_OPEN + shortcutId, 1);
        }

        String iconpath = iconResource != null ? iconResource.packageName + SEPARATOR_PKG + iconResource.resourceName : "";
        String componentToSave = shortcutId + SEPARATOR_RES + iconpath + SEPARATOR_LABEL + label;

        activity.saveShortcutComponent(componentToSave);
        AppConfigDialog.afterSave(activity, getDialogRef());

        if (bitmap != null) {
            saveIcon(activity, shortcutId, bitmap);
        }
    }

    public static Intent getIntent(AppEntry entry) throws URISyntaxException {
        SharedPreferences prefs = GlobalContext.prefSettings.sharedPrefs;
        String shortcutId = getShortcutId(entry);
        log.debug("shortcut/getIntent", shortcutId);
        String uri = prefs.getString(shortcutId, null);
        log.debug(". uri", uri);
        return Intent.parseUri(uri, 0);
    }

    public static void initIconDir(Context context) {
        if (iconDir == null) {
            iconDir = new File(context.getFilesDir(), "icons");
            if (!iconDir.exists()) {
                iconDir.mkdir();
            }
        }
    }

    private static void saveIcon(Context context, String shortcutId, Bitmap icon) {
        initIconDir(context);
        try {
            File file = new File(iconDir, shortcutId + PNG);
            FileOutputStream out = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Throwable t) {
            SystemUtil.dumpError(t);
        }
    }

    public static Drawable loadIcon(Context context, AppEntry appEntry, LargeIconLoader largeIconLoader, int iconSizePx) {
        Drawable icon = null;
        final String component = appEntry.getComponent();
        final String respath = component.substring(component.indexOf(SEPARATOR_RES) + 1, component.indexOf(SEPARATOR_LABEL));
        if (respath.length() > 0) {

            final String pkg = respath.substring(0, respath.indexOf(SEPARATOR_PKG));
            final String resname = respath.substring(respath.indexOf(SEPARATOR_PKG) + 1);

            //			if (largeIconLoader==null) {
            //				try {
            //					// try "quick load" using resource URI
            //					// note with email shortcuts we have a weird format so this might fail
            //					// (pkg=com.google.android.email, res=com.android.email:mipmap/ic_launcher_email)
            //					Uri uri = Uri.parse("android.resource://"+resname.replace(":", "/"));
            //					Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            //					icon = new BitmapDrawable(GlobalContext.resources, bitmap);
            //				}
            //				catch (Throwable t) {
            //					SystemUtil.dumpError(t);
            //				}
            //			}

            try {
                Resources appRes = GlobalContext.packageManager.getResourcesForApplication(pkg);
                int id = appRes.getIdentifier(resname, null, null);
                if (largeIconLoader != null && id > 0) {
                    icon = largeIconLoader.getLargeIcon(appRes, id);
                }
                if (icon == null && id > 0) {
                    icon = appRes.getDrawable(id);
                }
            } catch (Throwable t) {
                SystemUtil.dumpError(t);
            }

        } else {
            // png file on disk
            initIconDir(context);
            String shortcutId = getShortcutId(component);
            File file = new File(iconDir, shortcutId + PNG);
            icon = Drawable.createFromPath(file.getAbsolutePath());
        }

        if (icon == null) {
            // default icon if something fails
            icon = IconProvider.getDefaultIcon();
        }
        return IconProvider.scale(icon, iconSizePx);
    }

    public static void removeShortcuts(ArrayList<String> shortcutIds) {
        Editor edit = GlobalContext.prefSettings.sharedPrefs.edit();
        for (String shortcutId : shortcutIds) {
            edit.remove(shortcutId);
            edit.remove(EXTRA_PREFIX_KEEP_OPEN + shortcutId);
        }
        edit.apply();

        if (iconDir == null) {
            // if we get here the icondir should already have been initialised
            // (since initial display of the according item has already occurred)
            return;
        }
        for (String shortcutId : shortcutIds) {
            File file = new File(iconDir, shortcutId + PNG);
            boolean deleted = file.delete();
            log.debug("delete icon file", file, deleted);
        }
    }

    public static boolean isShortcutWithLocalIcon(String entryGroup, String entryKey) {
        return entryGroup.startsWith(HBLConstants.PREFS_APPS)
                && entryKey.startsWith(PREFIX_SHORTCUT)
                && entryKey.contains(SEPARATOR_RES + SEPARATOR_LABEL)
                ;
    }

    public static String encodeIcon(String shortcutId) throws Exception {
        File file = new File(iconDir, shortcutId + PNG);
        if (!file.exists()) {
            return "";
        }
        ByteArrayOutputStream os = FileUtil.getContent(file);
        return Hex.encodeHex(os.toByteArray(), false);
    }

    public static void restoreIcon(String shortcutId, String encodedIconData) throws Exception {
        if (encodedIconData == null || encodedIconData.length() == 0) {
            return;
        }
        File file = new File(iconDir, shortcutId + PNG);
        FileUtil.writeToFile(file, Hex.decodeHex(encodedIconData));
        log.debug("icon restore done", file, file.length());
    }

    public static void startExitSelfShortcut(MainActivityHome activity, AppConfigDialog dialog, AppEntry appEntry) {
        String label = "Exit";
        Bundle extras = new Bundle();
        extras.putParcelable(Intent.EXTRA_SHORTCUT_INTENT, AppHelper.getStartIntent(appEntry.getComponent()));
        extras.putString(Intent.EXTRA_SHORTCUT_NAME, label);
        Intent.ShortcutIconResource iconResource = new Intent.ShortcutIconResource();
        iconResource.packageName = appEntry.getPackage();
        iconResource.resourceName = appEntry.getPackage() + ":drawable/app_icon";
        extras.putParcelable(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        Intent data = new Intent();
        data.putExtras(extras);
        storeDialogRef(dialog);
        ShortcutHelper.shortcutSelected(activity, data);
    }

    public static void storeDialogRef(AppConfigDialog appConfigDialog) {
        dialogRef = new WeakReference<AppConfigDialog>(appConfigDialog);
    }

    private static AppConfigDialog getDialogRef() {
        return dialogRef != null ? dialogRef.get() : null;
    }

    public static boolean isKeepOpen(PrefSettings prefSettings, AppEntry entry) {
        String key = EXTRA_PREFIX_KEEP_OPEN + getShortcutId(entry);
        return prefSettings.getIntValue(key) == 1;
    }

    public static class ExtraToggle {

        private static final int CONTACT = 1;
        private static final int KEEP_OPEN = 2;

        private int what;
        private CheckBox box;

        public ExtraToggle(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            if ("com.android.contacts.action.QUICK_CONTACT".equals(intent.getAction())) {
                /*
                 * contact shortcut crashes the contact app on XPeria Z, so we show an extra "resolve contact id" option.
				 * see https://mail.google.com/mail/u/0/?ui=2&shva=1#inbox/140118c724029d62
				 */
                what = CONTACT;
                box = createBox(context, R.string.resolveContact, SystemUtil.isSony());
            } else if (intent.getComponent() != null && "com.dynamicg.settings".equals(intent.getComponent().getPackageName())) {
                what = KEEP_OPEN;
                box = createBox(context, R.string.hblKeepOpen, true);
            }
        }

        private static CheckBox createBox(Context context, int textId, boolean checked) {
            CheckBox box = new CheckBox(context);
            box.setText(textId);
            int pad = DialogHelper.getDimension(R.dimen.labelMargin);
            box.setPadding(box.getPaddingLeft(), pad, box.getPaddingRight(), pad);
            box.setChecked(checked);
            return box;
        }

        public boolean isResolveContactId() {
            return what == CONTACT && box.isChecked();
        }

        public boolean isKeepHomeLauncherOpen() {
            return what == KEEP_OPEN && box.isChecked();
        }
    }

}
