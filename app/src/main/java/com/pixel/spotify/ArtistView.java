package com.pixel.spotify;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class ArtistView extends CoordinatorLayout {
    private static final int LAYOUT = R.layout.fragment_album;
    public ArtistView (@NonNull Context context) {
        super (context);

        LayoutInflater.from (context).inflate (LAYOUT, this, true);
    }
}
