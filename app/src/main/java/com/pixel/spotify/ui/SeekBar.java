package com.pixel.spotify.ui;

import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static neon.pixel.components.Components.getPx;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.pixel.spotify.R;

import neon.pixel.components.color.Hct;

public class SeekBar extends CoordinatorLayout {
    private CoordinatorLayout mViewContainer;
    private View mBackground;
    private GradientDrawable mBackgroundDrawable;
    private View mForeground;

    private int mWidth;
    private int mHeight;

    private int mViewHeight;

    private boolean mRespondToTouch;
    private boolean mSnapToTouch;
    private int mThemeColor;
    private int mBackgroundColor;
    private int mForegroundColor;

    private float mProgress;

    public SeekBar (Context context) {
        super (context);
    }

    public SeekBar (Context context, @Nullable AttributeSet rawAttrs) {
        super (context, rawAttrs);

        mBackgroundDrawable = (GradientDrawable) context.getDrawable (R.drawable.seek_bar_background).mutate ();

        TypedArray seekBarAttrs = context.getTheme ().obtainStyledAttributes (
                rawAttrs,
                R.styleable.SeekBarAttrs,
                0, 0);

        TypedArray progressBarAttrs = context.getTheme ().obtainStyledAttributes (
                rawAttrs,
                R.styleable.ProgressBarAttrs,
                0, 0);

//        orientation = progressBarAttrs.getInteger (R.styleable.ComponentAttrs_orientation, 0);
        mViewHeight = (int) getPx (context, 64);//(int) progressBarAttrs.getDimension (R.styleable.ProgressBarAttrs_viewHeight, 0);

        LayoutInflater inflater = LayoutInflater.from (context);
        inflater.inflate (R.layout.layout_progress_bar, this, true);

        setBackground (null);

        mViewContainer = findViewById (R.id.view_container);
        mBackground = findViewById (R.id.background);
        mForeground = findViewById (R.id.progress_bar);

//        setBackground (mBackgroundDrawable);

        mRespondToTouch = true;//seekBarAttrs.getBoolean (R.styleable.SeekBarAttrs_respondToTouch, false);
        mSnapToTouch = false;//seekBarAttrs.getBoolean (R.styleable.SeekBarAttrs_snapToTouch, false);

        TypedValue tempHolder = new TypedValue ();

        context.getTheme ().resolveAttribute (R.attr.colorPrimaryContainer, tempHolder, true);
        mBackgroundColor = tempHolder.data;

        context.getTheme ().resolveAttribute (R.attr.colorPrimary, tempHolder, true);
        mForegroundColor = tempHolder.data;

        mBackground.setBackgroundColor (mBackgroundColor);
        mForeground.setBackgroundColor (mForegroundColor);

        if (mRespondToTouch) setOnTouchListener (onTouchListener);
    }

    public SeekBar (Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super (context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);

        int minw = getPaddingLeft () + getPaddingRight () + getSuggestedMinimumWidth ();
        int w = resolveSizeAndState (minw, widthMeasureSpec, 1);

        int minh = (int) (64 * getResources ().getDisplayMetrics ().density);
        int h = resolveSizeAndState (minh, heightMeasureSpec, 0);

        mWidth = w;
        mHeight = h;

        LayoutParams params = (LayoutParams) mViewContainer.getLayoutParams ();
        params.height = mViewHeight;
        mViewContainer.setLayoutParams (params);

        mBackgroundDrawable.setCornerRadius (h >= getPx (getContext (), 64) ? getPx (getContext (), 32) : mHeight / 2);
        mViewContainer.setBackground (mBackgroundDrawable);

        setMeasuredDimension (w, h);
    }

    public void seekTo (@FloatRange (from = 0, to = 1) float position) {
        mProgress = position;

        int newWidth = (int) (position * mWidth);

        LayoutParams params = new LayoutParams (newWidth, -1);
        mForeground.setLayoutParams (params);
    }

    public boolean getRespondToTouch () {
        return mRespondToTouch;
    }

    public boolean getSnapToTouch () {
        return mSnapToTouch;
    }

    public int getBackgroundColor () {
        return mBackgroundColor;
    }

    public int getColor () {
        return mForegroundColor;
    }

    public float getProgress () {
        return mProgress;
    }

    public void setRespondToTouch (boolean respondToTouch) {
        this.mRespondToTouch = respondToTouch;
    }

    public void setSnapToTouch (boolean snapToTouch) {
        this.mSnapToTouch = snapToTouch;
    }

    public void setNoTheme () {
        mBackgroundColor = 0;
        mForegroundColor = 0;

        mBackground.setBackgroundColor (mBackgroundColor);
        mForeground.setBackgroundColor (mForegroundColor);
    }

    public void setColor (int color) {
        mThemeColor = color;

        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);
        mForegroundColor = hct.toInt ();

        hct = Hct.fromInt (mThemeColor);
        hct.setTone (SECONDARY);
        mBackgroundColor = hct.toInt ();

        mBackground.setBackgroundColor (mBackgroundColor);
        mForeground.setBackgroundColor (mForegroundColor);
    }

    private ValueAnimator animator;

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

    private OnTouchListener onTouchListener = new OnTouchListener () {
        float startTouchX;
        float startX;
        float _;
        float rawTouchX;
        float touchX;

        float rawProgress;

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            rawTouchX = event.getX ();
            touchX = rawTouchX >= 0 ? (rawTouchX <= mWidth ? rawTouchX : mWidth) : 0;

            if (event.getAction () == MotionEvent.ACTION_DOWN) {
                startTouchX = rawTouchX;
                startX = mWidth * mProgress;

                _ = mSnapToTouch ? 0 : startX - startTouchX;
            } else if (event.getAction () == MotionEvent.ACTION_UP) {

            } else if (event.getAction () == MotionEvent.ACTION_MOVE) {

            }

            rawProgress = (rawTouchX + _) / mWidth;

            mProgress = rawProgress >= 0 ? (rawProgress <= 1 ? rawProgress : 1) : 0;

            seekTo (mProgress);

            return true;
        }
    };
}
