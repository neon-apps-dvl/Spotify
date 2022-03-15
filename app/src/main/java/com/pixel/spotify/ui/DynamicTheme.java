package com.pixel.spotify.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for dynamic application-wide themes
 */
public class DynamicTheme {
    private static Map <Integer, Theme> sThemes;
    private static Map <Integer, OnThemeChangedListener> sListeners;

    /**
     * Create a {@link Theme} instance
     * @param id The ID of this theme
     * @param themeProvider The {@code enum} used to provide the possible colors for this theme
     * @return An instance of {@link Theme}
     */
    public static Theme newInstance (int id, Class <? extends Enum> themeProvider) {
        if (sThemes == null) {
            sThemes = new HashMap <> ();
            sListeners = new HashMap <> ();
        }

        Theme theme = Theme.from (themeProvider);

        sThemes.put (id, theme);

        return theme;
    }

    /**
     * Create a {@link Theme} instance
     * @param id The ID of this theme
     * @param themeProvider The {@code enum} used to provide the possible colors for this theme
     * @param l An {@link OnThemeChangedListener} used to notify changes to the theme
     * @return An instance of {@link Theme}
     */
    public static Theme newInstance (int id, Class <? extends Enum> themeProvider, OnThemeChangedListener l) {
        setOnThemeChangedListener (id, l);

        return newInstance (id, themeProvider);
    }

    /**
     * Attach an {@link OnThemeChangedListener} to the {@link DynamicTheme} provider
     * to listen for changes to a {@link Theme}
     * @param id The id of the {@link Theme}
     * @param l The {@link OnThemeChangedListener}
     */
    private static void setOnThemeChangedListener (int id, OnThemeChangedListener l) {
        sListeners.put (id, l);
    }

    /**
     * Notify the {@link DynamicTheme} provider of a change to a {@link Theme}
     * @param id The id of the {@link Theme}
     */
    public void notifyThemeChanged (int id) {
        Theme theme = getTheme (id);

        OnThemeChangedListener l = sListeners.get (id);

        l.onThemeChanged (id, theme);
    }

    /**
     * Notify the {@link DynamicTheme} provider of a change to a {@link Theme}
     * @param theme The {@link Theme}
     */
    public void notifyThemeChanged (Theme theme) {
        Integer id = getId (theme);

        if (id == null) throw new NullPointerException ();

        OnThemeChangedListener l = sListeners.get (id);

        l.onThemeChanged (id, theme);
    }

    public static Theme getTheme (int id) {
        return sThemes.get (id);
    }

    private static Integer getId (Theme theme) {
        for (Map.Entry entry : sThemes.entrySet ()) {
            if (entry.getValue ().equals (theme)) return (int) entry.getKey ();
        }

        return null;
    }
}
