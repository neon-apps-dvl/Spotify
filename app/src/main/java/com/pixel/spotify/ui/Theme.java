package com.pixel.spotify.ui;

import java.util.Arrays;
import java.util.List;

public class Theme {
    private Class <? extends Enum> mValuesProvider;
    private int [] mColors;
    private OnThemeChangedListener mListener;

    protected Theme () {

    }

    public static Theme from (Class <? extends Enum> values) {
        Theme theme = new Theme ();
        theme.mValuesProvider = values;

        theme.mColors = new int[values.getEnumConstants ().length];

        return theme;
    }

    public void setColor (Enum color, int value) {
        if (! mValuesProvider.equals (color.getClass ())) throw new IllegalArgumentException ();

        List <Enum> values = Arrays.asList (mValuesProvider.getEnumConstants ());
        Enum <?> c = Enum.valueOf (mValuesProvider, color.name ());
        int i = values.indexOf (c);

        mColors [i] = value;
    }

    public void setColors (Enum [] colors, int[] values) {

    }

    public int getColor (Enum color) {
        if (! mValuesProvider.equals (color.getClass ())) throw new IllegalArgumentException ();

        List <Enum> values = Arrays.asList (mValuesProvider.getEnumConstants ());
        int i = values.indexOf (color);

        return mColors [i];
    }

    public void getColors (int colors) {

    }

    public Class getProvider () {
        return mValuesProvider;
    }
}
