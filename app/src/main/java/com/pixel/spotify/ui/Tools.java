package com.pixel.spotify.ui;

import java.util.concurrent.TimeUnit;

public class Tools {
    public static String getTimeString (long ms) {
        return TimeUnit.MILLISECONDS.toMinutes (ms)
                + ":"
                + (TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
    }
}
