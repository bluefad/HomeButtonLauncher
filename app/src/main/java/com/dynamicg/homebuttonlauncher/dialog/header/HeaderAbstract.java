package com.dynamicg.homebuttonlauncher.dialog.header;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dynamicg.common.SdkLevel;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.preferences.PrefSettings;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;

public abstract class HeaderAbstract {

    protected final Context context;
    protected final TextView titleNode;
    protected final ImageView iconNode;
    private final Dialog dialog;

    public HeaderAbstract(Dialog dialog) {
        this.dialog = dialog;
        this.context = dialog.getContext();
        this.titleNode = ((TextView) dialog.findViewById(R.id.headerTitle));
        this.iconNode = (ImageView) dialog.findViewById(R.id.headerIcon);
        int iconId = getMenuIconId(new PrefSettings(dialog.getContext()));
        iconNode.setImageResource(iconId);
    }

    public static int getMenuIconId(PrefSettings settings) {
        if (SdkLevel.BEFORE_LOLLIPOP) {
            return settings.getThemeId() == PrefSettings.THEME_LIGHT ? R.drawable.ic_menu_moreoverflow_normal_holo_light : R.drawable.ic_menu_moreoverflow_normal_holo_dark;
        }
        return R.drawable.ic_menu_moreoverflow_material;
    }

    protected abstract void attach();

    /**
     * @param appList
     */
    public void setBaseAppList(AppListContainer appList) {
    }

    public void attach(int titleResId) {
        attach();
        setTitleAndWidth(titleResId);
    }

    protected void setTitleAndWidth(int label) {
        titleNode.setText(label);
        int width = DialogHelper.getDimension(R.dimen.widthAppConfig);
        View container = dialog.findViewById(R.id.headerContainer);
        container.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
