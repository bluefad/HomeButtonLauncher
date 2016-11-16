package com.dynamicg.homebuttonlauncher.tab;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.View;

import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;

public class BackgroundHelper {

    private static final int COLOR_DARK_ACTIVE = Color.rgb(0x2f, 0x2f, 0x2f);
    private static final int COLOR_DARK_INACTIVE = Color.rgb(0x27, 0x27, 0x27);
    private static final int COLOR_LIGHT_ACTIVE = Color.rgb(0xec, 0xec, 0xec);
    private static final int COLOR_LIGHT_INACTIVE = Color.rgb(0xdd, 0xdd, 0xdd);
    private static final int COLOR_PRESSED = Color.argb(0x3f, 0x7f, 0x7f, 0x7f);

    private static ShapeDrawable shape(int color) {
        RoundRectShape shape = new RoundRectShape(null, null, null);
        ShapeDrawable drawable = new ShapeDrawable(shape);
        drawable.getPaint().setColor(color);
        return drawable;
    }

    private static StateListDrawable selector(int colorNormal, int colorSelected) {
        ShapeDrawable shapeSelected = shape(colorSelected);
        ShapeDrawable shapeNormal = shape(colorNormal);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, shapeSelected);
        states.addState(new int[]{android.R.attr.state_focused}, shapeSelected);
        states.addState(new int[]{}, shapeNormal);
        return states;
    }

    private static StateListDrawable getSelector(int themeId, boolean active) {
        if (themeId == PrefSettings.THEME_DARK) {
            return selector(active ? COLOR_DARK_ACTIVE : COLOR_DARK_INACTIVE, COLOR_PRESSED);
        }
        if (themeId == PrefSettings.THEME_LIGHT) {
            return selector(active ? COLOR_LIGHT_ACTIVE : COLOR_LIGHT_INACTIVE, COLOR_PRESSED);
        }
        return null;
    }

    public static void setBackground(int themeId, View view, boolean active) {
        if (view.getTag(R.id.tag_tab_selected) == Boolean.valueOf(active)) {
            return;
        }
        StateListDrawable drawable = getSelector(themeId, active);
        if (drawable != null) {
            view.setBackground(drawable);
            view.setTag(R.id.tag_tab_selected, active);
        }
    }
}
