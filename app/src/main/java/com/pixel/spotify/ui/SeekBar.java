package com.pixel.spotify.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;

public class SeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private float mProgress;

    private OnSeekBarProgressChangedListener mOnSeekBarProgressChangedListener;

    public SeekBar (Context context) {
        super (context);
    }

    public SeekBar (Context context, @Nullable AttributeSet rawAttrs) {
        super (context, rawAttrs);

    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);

        int minw = getPaddingLeft () + getPaddingRight () + getSuggestedMinimumWidth ();
        int w = resolveSizeAndState (minw, widthMeasureSpec, 1);

        int minh = (int) (64 * getResources ().getDisplayMetrics ().density);
        int h = resolveSizeAndState (minh, heightMeasureSpec, 0);
    }

    public void seekTo (@FloatRange (from = 0, to = 1) float position) {
        mProgress = position;
    }

//    public void setOnProgressChangedListener (OnSeekBarProgressChangedListener l) {
//        mOnSeekBarProgressChangedListener = l;
//    }
}
