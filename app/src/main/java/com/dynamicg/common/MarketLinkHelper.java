package com.dynamicg.common;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dynamicg.homebuttonlauncher.tools.AppHelper;

public class MarketLinkHelper {

    private static void openUrlIntent(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        AppHelper.flagAsNewTask(i);
        context.startActivity(i);
    }

    public static void openMarketIntent(Context context, String pkg) {
        try {
            openUrlIntent(context, "market://details?id=" + pkg);
        } catch (ActivityNotFoundException e) {
            // Android Market not installed? try with HTTP url
            openUrlIntent(context, "http://market.android.com/details?id=" + pkg);
        }
    }

}
