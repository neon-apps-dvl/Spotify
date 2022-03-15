package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pixel.components.color.Hct;
import com.pixel.spotify.R;

public class TrackInfoView extends ConstraintLayout {
    @LayoutRes
    private static final int LAYOUT = R.layout.track;

    private TextView trackInfoView;

    private String title;
    private String artist;
    private String album;

    private String info;

    private int themeColor;

    public TrackInfoView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        setBackground (null);

        trackInfoView = findViewById (R.id.track_info_view);

        trackInfoView.setBackground (null);
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout (changed, left, top, right, bottom);
    }

    public void setParams (String title, String artist, String album) {
        this.title = title;
        this.artist = artist;
        this.album = album;

        SpannableStringBuilder info = new SpannableStringBuilder (title + "&" + artist);
        info.setSpan (new ImageSpan (getContext (), Bitmap.createBitmap ((int) getPx (getContext (), 12), 1, Bitmap.Config.ARGB_8888)), title.length (), title.length () + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        trackInfoView.setText (info);
        trackInfoView.setAutoSizeTextTypeUniformWithConfiguration ((int) getPx (getContext (), 12), (int) getPx (getContext (), 24), 1, TypedValue.COMPLEX_UNIT_PX);

        setColor (themeColor);
    }

    ValueAnimator rAnimator;
    ValueAnimator gAnimator;
    ValueAnimator bAnimator;

    ValueAnimator animator;

    public void setColor (int color) {
        themeColor = color;

        Hct hct = Hct.fromInt (color);
        hct.setTone (70);

        int titleColor = hct.toInt ();

        hct.setTone (50);
        int artistColor = hct.toInt ();

        if (title != null) {
            SpannableString info = new SpannableString (trackInfoView.getText ());
            info.setSpan (new ForegroundColorSpan (titleColor), 0, title.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            info.setSpan (new ForegroundColorSpan (artistColor), title.length () + 1, info.toString ().length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            trackInfoView.setText (info);
            trackInfoView.setSelected (true);
        }
    }

    public void updateColor (int color) {
        if (animator != null && animator.isRunning ()) return;

        animator = ValueAnimator.ofObject (new ArgbEvaluator (), themeColor, color);
        animator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        animator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                themeColor = (int) animation.getAnimatedValue ();

                setColor (themeColor);
            }
        });

        animator.start ();
    }

    public void updateColor2 (int color1) {
        if (rAnimator != null && rAnimator.isRunning ()) return;

        int r = Color.red (themeColor);
        int g = Color.green (themeColor);
        int b = Color.blue (themeColor);

        int targetR = Color.red (color1);
        int targetG = Color.green (color1);
        int targetB = Color.blue (color1);

        int[] rgb = new int[3];

        rAnimator = ValueAnimator.ofInt (r, targetR);
        rAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        rAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                int temp = (Integer) animation.getAnimatedValue ();

                rgb[0] = temp;

                int r = temp;
                int g = rgb[1];
                int b = rgb[2];

                themeColor = Color.argb (255, r, g, b);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (themeColor), Color.green (themeColor), Color.blue (themeColor),
//                        targetR, targetG, targetB));

                setColor (themeColor);
            }
        });

        gAnimator = ValueAnimator.ofInt (g, targetG);
        gAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        gAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                int temp = (Integer) animation.getAnimatedValue ();

                rgb[1] = temp;

                int r = rgb[0];
                int g = temp;
                int b = rgb[2];

                themeColor = Color.argb (255, r, g, b);

//                Log.d ("debug", "g: " + temp + " targetG: " + targetG);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (themeColor), Color.green (themeColor), Color.blue (themeColor),
//                        targetR, targetG, targetB));

                setColor (themeColor);
            }
        });

        bAnimator = ValueAnimator.ofInt (b, targetB);
        bAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        bAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                int temp = (Integer) animation.getAnimatedValue ();

                rgb[2] = temp;

                int r = rgb[0];
                int g = rgb[1];
                int b = temp;

                themeColor = Color.argb (255, r, g, b);
//                Log.d ("debug", "b: " + temp + " targetB: " + targetB);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (themeColor), Color.green (themeColor), Color.blue (themeColor),
//                        targetR, targetG, targetB));

                setColor (themeColor);
            }
        });

        rAnimator.start ();
        gAnimator.start ();
        bAnimator.start ();
    }
}
