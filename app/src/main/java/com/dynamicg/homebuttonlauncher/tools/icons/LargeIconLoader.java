package com.dynamicg.homebuttonlauncher.tools.icons;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.GlobalContext;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;

/*
 * see https://github.com/android/platform_frameworks_base/blob/master/core/java/com/android/internal/app/ResolverActivity.java
 * (mIconDpi)
 */
public class LargeIconLoader {

    private static final Logger log = new Logger(LargeIconLoader.class);

    private final int largeIconDensity;

    private LargeIconLoader(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
        this.largeIconDensity = activityManager.getLauncherLargeIconDensity();
    }

    public static LargeIconLoader createInstance(Context context, PrefSettings settings) {
        if (!settings.isHighResIcons()) {
            return null;
        }
        return new LargeIconLoader(context);
    }

    public Drawable getLargeIcon(AppEntry entry) {
        int id = entry.resolveInfo.getIconResource();
        if (id == 0) {
            return null;
        }
        try {
            Resources appRes = GlobalContext.packageManager.getResourcesForApplication(entry.getPackage());
            Drawable drawableForDensity = appRes.getDrawableForDensity(id, largeIconDensity);
            log.debug("getLargeIcon", drawableForDensity, entry.label);
            return drawableForDensity;
        } catch (Throwable e) {
            SystemUtil.dumpError(e);
            return null; // ignore
        }
    }

    public Drawable getLargeIcon(Resources appRes, int id) {
        return appRes.getDrawableForDensity(id, largeIconDensity);
    }

    //	public Drawable getLargeIcon(String respath) {
    //		try {
    //			String pkg = respath.substring(0, respath.indexOf("/"));
    //			String resname = respath.substring(respath.lastIndexOf("/")+1);
    //			Resources appRes = GlobalContext.packageManager.getResourcesForApplication(pkg);
    //			int id = appRes.getIdentifier(resname, "drawable" , pkg);
    //			if (id>0) {
    //				Drawable drawableForDensity = appRes.getDrawableForDensity(id, largeIconDensity);
    //				log.debug("getLargeIcon", drawableForDensity, respath, pkg, resname);
    //				return drawableForDensity;
    //			}
    //			else {
    //				log.debug("resource not found", respath, pkg, resname);
    //				//SystemUtil.recentError = new RuntimeException("res not found ["+respath+"]["+pkg+"]["+resname+"]");
    //			}
    //		}
    //		catch (Throwable e) {
    //			SystemUtil.dumpError(e);
    //		}
    //
    //		return null;
    //	}

}
