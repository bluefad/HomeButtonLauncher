package com.dynamicg.homebuttonlauncher.dialog.header;

import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.dynamicg.common.Logger;
import com.dynamicg.homebuttonlauncher.AppEntry;
import com.dynamicg.homebuttonlauncher.AppListContainer;
import com.dynamicg.homebuttonlauncher.dialog.AppConfigDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HeaderAppSearch extends HeaderAbstract {

    private static final Logger log = new Logger(HeaderAppSearch.class);

    private final AppConfigDialog dialog;

    private List<AppEntry> baseAppList;
    private String[] baseSearchLabels = null; // lazy
    private SearchView searchview;

    public HeaderAppSearch(AppConfigDialog dialog) {
        super(dialog);
        this.dialog = dialog;
    }

    private void initSearchLabels() {
        baseSearchLabels = new String[baseAppList.size()];
        for (int i = 0; i < baseAppList.size(); i++) {
            baseSearchLabels[i] = baseAppList.get(i).label.toLowerCase(Locale.getDefault());
        }
    }

    @Override
    public void attach() {

        OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                log.debug("onQueryTextChange", newText);
                updateAppList(newText);
                return true;
            }
        };

        OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                log.debug("onFocusChange", queryTextFocused);
                if (queryTextFocused) {
                    switchTitle(View.GONE);
                }
            }
        };

        SearchView.OnCloseListener onCloseListener = new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                log.debug("onCloseListener");
                switchTitle(View.VISIBLE);
                return false;
            }
        };

        this.searchview = new SearchView(context);
        searchview.setOnQueryTextListener(onQueryTextListener);
        searchview.setOnQueryTextFocusChangeListener(onFocusChangeListener);
        searchview.setOnCloseListener(onCloseListener);

        iconNode.setVisibility(View.GONE);
        ViewGroup container = ((ViewGroup) iconNode.getParent());
        container.addView(searchview, container.indexOfChild(iconNode));
    }

    private void switchTitle(int what) {
        if (titleNode.getVisibility() != what) {
            titleNode.setVisibility(what);
        }
    }

    private void updateAppList(String query) {

        log.debug("updateAppList", query);

        if (baseAppList.size() == 0) {
            return;
        }

        if (query == null || query.length() == 0) {
            dialog.updateAppList(baseAppList);
            return;
        }

        if (baseSearchLabels == null) {
            initSearchLabels();
        }

        final String searchstr = query.toLowerCase(Locale.getDefault());
        List<AppEntry> matchingApps = new ArrayList<AppEntry>();
        for (int i = 0; i < baseSearchLabels.length; i++) {
            if (baseSearchLabels[i].contains(searchstr)) {
                log.debug("matching app", baseSearchLabels[i]);
                matchingApps.add(baseAppList.get(i));
            }
        }
        dialog.updateAppList(matchingApps);
    }

    @Override
    public void setBaseAppList(AppListContainer appList) {
        this.baseAppList = appList.getApps();
        this.baseSearchLabels = null;
        if (searchview.getQuery().length() > 0) {
            searchview.setQuery("", false);
            searchview.setIconified(true);
        }
    }

}
