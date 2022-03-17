package com.pixel.spotify.ui;

import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static neon.pixel.components.Components.getPx;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pixel.spotify.R;

import neon.pixel.components.color.Hct;

public class TrackInfoView extends ConstraintLayout {
    @LayoutRes
    private static final int LAYOUT = R.layout.track;

    private TextView mTrackInfoView;

    private String mTitle;
    private String mArtist;
    private String mAlbum;

    private int mThemeColor;

    public TrackInfoView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        setBackground (null);

        mTrackInfoView = findViewById (R.id.track_info_view);

        mTrackInfoView.setBackground (null);
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout (changed, left, top, right, bottom);
    }

    public void setInfo (String title, String artist, String album) {
        mTitle = title;
        mArtist = artist;
        mAlbum = album;

//        SpannableStringBuilder info = new SpannableStringBuilder (title + " " + artist);
//        info.setSpan (new ImageSpan (getContext (), Bitmap.createBitmap ((int) getPx (getContext (), 12), 1, Bitmap.Config.ARGB_8888)), title.length (), title.length () + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTrackInfoView.setText (title + " " + artist);
        mTrackInfoView.setAutoSizeTextTypeUniformWithConfiguration ((int) getPx (getContext (), 12), (int) getPx (getContext (), 24), 1, TypedValue.COMPLEX_UNIT_PX);

        setColor (mThemeColor);
    }

    ValueAnimator rAnimator;
    ValueAnimator gAnimator;
    ValueAnimator bAnimator;

    ValueAnimator animator;

    public void setColor (int color) {
        mThemeColor = color;

        Hct hct = Hct.fromInt (mThemeColor);
        hct.setTone (PRIMARY);
        int colorPrimary = hct.toInt ();

        hct = Hct.fromInt (mThemeColor);
        hct.setTone (SECONDARY);
        int colorSecondary = hct.toInt ();

        if (mTitle != null) {
            SpannableString s = new SpannableString (mTrackInfoView.getText ());
            s.setSpan (new ForegroundColorSpan (colorPrimary), 0, mTitle.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan (new ForegroundColorSpan (colorSecondary), mTitle.length () + 1, s.toString ().length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTrackInfoView.setText (s);
            mTrackInfoView.setSelected (true);
        }
    }

    public void updateColor (int color) {
        if (animator != null && animator.isRunning ()) return;

        animator = ValueAnimator.ofObject (new ArgbEvaluator (), mThemeColor, color);
        animator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        animator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mThemeColor = (int) animation.getAnimatedValue ();

                setColor (mThemeColor);
            }
        });

        animator.start ();
    }

    public void updateColor2 (int color1) {
        if (rAnimator != null && rAnimator.isRunning ()) return;

        int r = Color.red (mThemeColor);
        int g = Color.green (mThemeColor);
        int b = Color.blue (mThemeColor);

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

                mThemeColor = Color.argb (255, r, g, b);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (mThemeColor), Color.green (mThemeColor), Color.blue (mThemeColor),
//                        targetR, targetG, targetB));

                setColor (mThemeColor);
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

                mThemeColor = Color.argb (255, r, g, b);

//                Log.d ("debug", "g: " + temp + " targetG: " + targetG);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (mThemeColor), Color.green (mThemeColor), Color.blue (mThemeColor),
//                        targetR, targetG, targetB));

                setColor (mThemeColor);
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

                mThemeColor = Color.argb (255, r, g, b);
//                Log.d ("debug", "b: " + temp + " targetB: " + targetB);

//                Log.d ("debug", String.format ("r: %d g: %d b: %d  |  r: %d g: %d b: %d",
//                        Color.red (mThemeColor), Color.green (mThemeColor), Color.blue (mThemeColor),
//                        targetR, targetG, targetB));

                setColor (mThemeColor);
            }
        });

        rAnimator.start ();
        gAnimator.start ();
        bAnimator.start ();
    }
}
