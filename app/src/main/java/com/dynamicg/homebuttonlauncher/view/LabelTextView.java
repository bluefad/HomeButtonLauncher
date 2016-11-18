package com.dynamicg.homebuttonlauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class LabelTextView extends TextView {

    public LabelTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LabelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LabelTextView(Context context) {
        super(context);
    }

    public static CharSequence getLabel(CharSequence text) {
        if (text != null && !text.toString().endsWith(":")) {
            return text + ":";
        }
        return text;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(getLabel(text), type);
    }


}
