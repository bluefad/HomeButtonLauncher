package com.dynamicg.homebuttonlauncher.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StatusLineHelper {

    // see http://stackoverflow.com/questions/6564365/android-development-show-battery-level
    private static String getBatteryPct(final Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (intent == null) {
            return null;
        }
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        if (level == 0 || scale == 0) {
            return null;
        }
        int pct = level * 100 / scale;
        return pct + "%";
    }

    public static void addStatus(Activity context, PrefSettings prefSettings) {
        Date now = Calendar.getInstance().getTime();
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
        java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        String datestr = dateFormat.format(now);
        String timestr = timeFormat.format(now);
        String dayname = new SimpleDateFormat("EEE", Locale.getDefault()).format(now);

        String tokenSeparator = "  |  ";
        StringBuilder sb = new StringBuilder()
                .append(timestr)
                .append(tokenSeparator)
                .append(dayname)
                .append(" ")
                .append(datestr);

        String battery = getBatteryPct(context);
        if (battery != null) {
            sb.append(tokenSeparator);
            sb.append(battery);
        }

        TextView status = (TextView) context.getLayoutInflater().inflate(R.layout.footer_status, null);
        status.setText(sb.toString());
        ViewGroup parent = (ViewGroup) context.findViewById(R.id.mainContainer);

        if (prefSettings.isTabAtBottom() && prefSettings.getNumTabs() > 1) {
            // footer status is second last before the tabs
            parent.addView(status, parent.getChildCount() - 1);
        } else {
            parent.addView(status);
        }
    }


}
