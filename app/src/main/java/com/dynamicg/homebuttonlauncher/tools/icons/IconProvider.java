package com.dynamicg.homebuttonlauncher.tools.icons;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.R;

// see http://stackoverflow.com/questions/4609456/android-set-drawable-size-programatically
public class IconProvider {

    private static final Logger log = new Logger(IconProvider.class);

    private static final int ICON_SIZE_DP = 48;

    private static int getSizePX(int dp) {
        int px = (int) (dp * GlobalContext.density);
        log.debug("getSizePX", GlobalContext.density, dp, px);
        return px;
    }

    public static int getDefaultSizePX() {
        return getSizePX(ICON_SIZE_DP);
    }

    public static int getPreferredSizePX() {
        return getSizePX(GlobalContext.prefSettings.getIconSize());
    }

    private static Drawable scaleBitmap(int sizePX, Bitmap bitmap) {
        return new BitmapDrawable(GlobalContext.resources, Bitmap.createScaledBitmap(bitmap, sizePX, sizePX, true));
    }

    public static Drawable scale(Drawable icon, int iconSizePx) {

        if (icon == null) {
            return null;
        }

        if (icon.getIntrinsicHeight() == iconSizePx && icon.getIntrinsicWidth() == iconSizePx) {
            // icon is standard size, no scaling required
            //log.debug("scale() - no scaling required");
            return icon;
        }

        log.debug("scale() - change", icon.getIntrinsicHeight(), icon.getIntrinsicWidth(), iconSizePx);
        try {
            return scaleBitmap(iconSizePx, ((BitmapDrawable) icon).getBitmap());
        } catch (ClassCastException e1) {
            /*
             * error report
			 * java.lang.ClassCastException: android.graphics.drawable.StateListDrawable cannot be cast to android.graphics.drawable.BitmapDrawable
			 */
            if (icon instanceof StateListDrawable) {
                try {
                    StateListDrawable sd = (StateListDrawable) icon;
                    return scaleBitmap(iconSizePx, ((BitmapDrawable) sd.getCurrent()).getBitmap());
                } catch (Throwable e2) {
                    //ignore all
                    SystemUtil.dumpError(e2);
                }
            }
            // return default icon
            return getDefaultIcon(iconSizePx);
        }
    }

    public static Drawable getDefaultIcon() {
        return GlobalContext.resources.getDrawable(R.drawable.android);
    }

    public static Drawable getDefaultIcon(int iconSizePx) {
        return scaleBitmap(iconSizePx, ((BitmapDrawable) getDefaultIcon()).getBitmap());
    }

}
