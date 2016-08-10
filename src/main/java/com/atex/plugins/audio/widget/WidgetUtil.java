package com.atex.plugins.audio.widget;

public final class WidgetUtil {

    private WidgetUtil() {
    }

    public static String abbreviate(final String str, final int maxWidth) {
        return str != null && str.length() > maxWidth
                ? str.substring(0, maxWidth - 3) + "..."
                : str;
    }
}
