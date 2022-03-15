package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;

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

import com.pixel.components.color.Hct;
import com.pixel.spotify.R;

public class SeekBar extends CoordinatorLayout {
    CoordinatorLayout viewContainer;
    View background;
    GradientDrawable backgroundDrawable;
    View foreground;

    int width;
    int height;

    int viewHeight;

    int orientation;

    boolean respondToTouch;
    boolean snapToTouch;
    int themeColor;
    int backgroundColor;
    int foregroundColor;

    float progress;

    public SeekBar (Context context) {
        super (context);
    }

    public SeekBar (Context context, @Nullable AttributeSet rawAttrs) {
        super (context, rawAttrs);

        backgroundDrawable = (GradientDrawable) context.getDrawable (R.drawable.seek_bar_background).mutate ();

        TypedArray seekBarAttrs = context.getTheme ().obtainStyledAttributes (
                rawAttrs,
                R.styleable.SeekBarAttrs,
                0, 0);

        TypedArray progressBarAttrs = context.getTheme ().obtainStyledAttributes (
                rawAttrs,
                R.styleable.ProgressBarAttrs,
                0, 0);

//        orientation = progressBarAttrs.getInteger (R.styleable.ComponentAttrs_orientation, 0);
        viewHeight = (int) getPx (context, 64);//(int) progressBarAttrs.getDimension (R.styleable.ProgressBarAttrs_viewHeight, 0);

        LayoutInflater inflater = LayoutInflater.from (context);
        inflater.inflate (R.layout.layout_progress_bar, this, true);

        setBackground (null);

        viewContainer = findViewById (R.id.view_container);
        background = findViewById (R.id.background);
        foreground = findViewById (R.id.progress_bar);

//        setBackground (backgroundDrawable);

        respondToTouch = true;//seekBarAttrs.getBoolean (R.styleable.SeekBarAttrs_respondToTouch, false);
        snapToTouch = false;//seekBarAttrs.getBoolean (R.styleable.SeekBarAttrs_snapToTouch, false);

        TypedValue tempHolder = new TypedValue ();

        context.getTheme ().resolveAttribute (R.attr.colorPrimaryContainer, tempHolder, true);
        backgroundColor = tempHolder.data;

        context.getTheme ().resolveAttribute (R.attr.colorPrimary, tempHolder, true);
        foregroundColor = tempHolder.data;

        background.setBackgroundColor (backgroundColor);
        foreground.setBackgroundColor (foregroundColor);

        if (respondToTouch) setOnTouchListener (onTouchListener);
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

        width = w;
        height = h;

        LayoutParams params = (LayoutParams) viewContainer.getLayoutParams ();
        params.height = viewHeight;
        viewContainer.setLayoutParams (params);

        backgroundDrawable.setCornerRadius (h >= getPx (getContext (), 64) ? getPx (getContext (), 32) : height / 2);
        viewContainer.setBackground (backgroundDrawable);

        setMeasuredDimension (w, h);
    }

    public void seekTo (@FloatRange (from = 0, to = 1) float position) {
        progress = position;

        int newWidth = (int) (position * width);

        LayoutParams params = new LayoutParams (newWidth, -1);
        foreground.setLayoutParams (params);
    }

    public boolean getRespondToTouch () {
        return respondToTouch;
    }

    public boolean getSnapToTouch () {
        return snapToTouch;
    }

    public int getBackgroundColor () {
        return backgroundColor;
    }

    public int getColor () {
        return foregroundColor;
    }

    public float getProgress () {
        return progress;
    }

    public void setRespondToTouch (boolean respondToTouch) {
        this.respondToTouch = respondToTouch;
    }

    public void setSnapToTouch (boolean snapToTouch) {
        this.snapToTouch = snapToTouch;
    }

    public void setNoTheme () {
        backgroundColor = 0;
        foregroundColor = 0;

        background.setBackgroundColor (backgroundColor);
        foreground.setBackgroundColor (foregroundColor);
    }

    private void setColor (int color) {
        Hct backgroundHct = Hct.fromInt (color);
        Hct foregroundHct = Hct.fromInt (color);

        backgroundHct.setTone (20);
        foregroundHct.setTone (70);

        backgroundColor = backgroundHct.toInt ();
        foregroundColor = foregroundHct.toInt ();

        background.setBackgroundColor (backgroundHct.toInt ());
        foreground.setBackgroundColor (foregroundHct.toInt ());
    }

    private ValueAnimator animator;

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
            touchX = rawTouchX >= 0 ? (rawTouchX <= width ? rawTouchX : width) : 0;

            if (event.getAction () == MotionEvent.ACTION_DOWN) {
                startTouchX = rawTouchX;
                startX = width * progress;

                _ = snapToTouch ? 0 : startX - startTouchX;
            } else if (event.getAction () == MotionEvent.ACTION_UP) {

            } else if (event.getAction () == MotionEvent.ACTION_MOVE) {

            }

            rawProgress = (rawTouchX + _) / width;

            progress = rawProgress >= 0 ? (rawProgress <= 1 ? rawProgress : 1) : 0;

            seekTo (progress);

            return true;
        }
    };
}
