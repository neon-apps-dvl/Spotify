package com.pixel.spotify.ui;

import static neon.pixel.components.Components.getPx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class PlaylistView extends View {
    private static final String TAG = "AlbumView";

    private static final int SIZE_DP = 128;

    public PlaylistView (Context context) {
        this (context, null);
    }

    public PlaylistView (Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        setLayoutParams (new ViewGroup.LayoutParams (getPx (context, SIZE_DP), getPx (context, SIZE_DP)));
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);

        int minw = getPx (getContext (), SIZE_DP);
        int w = resolveSizeAndState(minw, widthMeasureSpec, 0);

        int minh = getPx (getContext (), SIZE_DP);
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    public void setPlaylist (Bitmap bitmap) {
        setBackground (new BitmapDrawable (getResources (), bitmap));
    }
}
