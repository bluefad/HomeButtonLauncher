package com.dynamicg.homebuttonlauncher.dialog.header;

import android.content.DialogInterface;
import android.widget.PopupMenu;

import com.dynamicg.homebuttonlauncher.HBLConstants;
import com.dynamicg.homebuttonlauncher.OnClickListenerDialogWrapper;
import com.dynamicg.homebuttonlauncher.R;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;
import com.dynamicg.homebuttonlauncher.tools.DialogHelper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper;
import com.dynamicg.homebuttonlauncher.tools.PopupMenuWrapper.PopupMenuItemListener;

public class HeaderAppSortReset extends HeaderAbstract {

    private final AppConfigDialog dialog;

    public HeaderAppSortReset(AppConfigDialog dialog) {
        super(dialog);
        this.dialog = dialog;
    }

    @Override
    public void attach() {
        final PopupMenuItemListener listener = new PopupMenuItemListener() {
            @Override
            public void popupMenuItemSelected(PopupMenu popupMenu, int id) {
                if (id == HBLConstants.MENU_RESET) {
                    confirmSortReset();
                }
            }
        };
        final PopupMenuWrapper menuWrapper = new PopupMenuWrapper(context, iconNode, listener);
        menuWrapper.attachToAnchorClick();
        menuWrapper.addItem(HBLConstants.MENU_RESET, R.string.menuReset);
    }

    private void confirmSortReset() {
        OnClickListenerDialogWrapper okListener = new OnClickListenerDialogWrapper(context) {
            @Override
            public void onClickImpl(DialogInterface d, int which) {
                dialog.doSortReset();
            }
        };
        DialogHelper.confirm(context, R.string.menuReset, okListener);
    }

}
