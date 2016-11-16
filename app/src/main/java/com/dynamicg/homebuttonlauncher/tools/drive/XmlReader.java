package com.dynamicg.homebuttonlauncher.tools.drive;

import android.content.Context;
import android.util.Xml;

import com.dynamicg.common.Logger;
import com.dynamicg.common.SystemUtil;
import com.dynamicg.homebuttonlauncher.tools.ShortcutHelper;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class XmlReader {

    private static final Logger log = new Logger(XmlReader.class);

    private final XmlPullParser parser;
    private int evtype;

    public XmlReader(InputStream inputStream) throws Exception {
        parser = Xml.newPullParser();
        parser.setInput(inputStream, XmlGlobals.ENCODING);
    }

    private void nextItem() throws Exception {
        parser.next();
        evtype = parser.getEventType();
    }

    public List<Map<String, String>> getContent(Context context) throws Exception {
        final List<Map<String, String>> content = new ArrayList<Map<String, String>>();
        Map<String, String> map = null;
        while (true) {
            nextItem();
            if (evtype == XmlPullParser.END_DOCUMENT) {
                break;
            }
            if (evtype == XmlPullParser.START_TAG) {
                String tag = parser.getName();
                if (XmlGlobals.TAG_BODY.equals(tag)) {
                    // skip
                } else if (XmlGlobals.TAG_ENTRY.equals(tag)) {
                    map = new TreeMap<String, String>();
                    content.add(map);
                } else {
                    nextItem();
                    map.put(tag, parser.getText());
                }
            }

            boolean iconRecovery = evtype == XmlPullParser.END_TAG
                    && XmlGlobals.TAG_ENTRY.equals(parser.getName())
                    && map != null
                    && map.containsKey(XmlGlobals.ENTRY_ICON_DATA);
            if (iconRecovery) {
                // direct icon recovery. we don't pass this back as "content" entry
                try {
                    ShortcutHelper.initIconDir(context);
                    String entryKey = map.get(XmlGlobals.ENTRY_KEY);
                    String entryIconData = map.get(XmlGlobals.ENTRY_ICON_DATA);
                    ShortcutHelper.restoreIcon(entryKey, entryIconData);
                    boolean remove = content.remove(map); // NO NEED TO PASS THIS BACK
                    log.debug("icon recovery done", entryKey, remove);
                } catch (Throwable t) {
                    SystemUtil.dumpError(t);
                }
            }
        }
        return content;
    }

}
