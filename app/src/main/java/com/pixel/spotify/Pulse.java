package com.pixel.spotify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.FloatRange;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

public class Pulse extends View {
    private Context context;
    private GradientDrawable pulseDrawable;
    private float minSize;
    private int maxSize;
    private int hue;

    private float minScale;
    private float maxScale;

    public Pulse (Context context, int minScale, int maxScale, boolean holdMin) {
        super (context);
        this.context = context;
        this.minScale = minScale;
        this.maxScale = maxScale;

        pulseDrawable = (GradientDrawable) context.getResources ().getDrawable (R.drawable.pulse, context.getTheme ()).mutate ();

        setBackground (pulseDrawable);

        post (new Runnable () {
            @Override
            public void run () {
                ConstraintLayout pulseContainer = (ConstraintLayout) getParent ();

//                setLayoutParams (new CoordinatorLayout.LayoutParams (ViewGroup.LayoutParams.MATCH_PARENT, -1));
                setSize (1);

                setScaleX (0f);
                setScaleY (0f);

//                setX (0);
                //setY (0);
            }
        });
    }

    public void setMinSize (int minSize) {
        this.minSize = minSize;
        this.minScale = minSize / maxSize;
    }

    public void setSize (int size) {
        setLayoutParams (new CoordinatorLayout.LayoutParams (size, size));
    }

    private void pulseOut (float pulseScale) {
        float targetWidth = pulseScale * getWidth ();

        ValueAnimator pulseOutAnimator = ValueAnimator.ofFloat (getScaleX (), pulseScale); // startPeriodic 0f
        pulseOutAnimator.setInterpolator (new LinearInterpolator ());
        pulseOutAnimator.setDuration (100);
        pulseOutAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                setScaleX ((Float) animation.getAnimatedValue ());
                setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        pulseOutAnimator.addListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd (Animator animation) {
                super.onAnimationEnd (animation);

                pulseIn (pulseScale);
            }

            @Override
            public void onAnimationStart (Animator animation) {
                super.onAnimationStart (animation);
            }
        });
        pulseOutAnimator.start ();
    }

    private void pulseIn (float pulseScale) {
        float targetWidth = pulseScale * getWidth ();

        ValueAnimator pulseOutAnimator = ValueAnimator.ofFloat (pulseScale, 0f);
        pulseOutAnimator.setInterpolator (new LinearInterpolator ());
        pulseOutAnimator.setDuration (200);
        pulseOutAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                setScaleX ((Float) animation.getAnimatedValue ());
                setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        pulseOutAnimator.addListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd (Animator animation) {
                super.onAnimationEnd (animation);
            }

            @Override
            public void onAnimationStart (Animator animation) {
                super.onAnimationStart (animation);
            }
        });
        pulseOutAnimator.start ();
    }

    public void pulse (@FloatRange (from = 0, to = 1) float pulseValue) {
        float rawScale = pulseValue * maxScale;
        float scale = rawScale >= minScale ? rawScale : minScale;

        Log.e ("Pulse", "pulse scale " + scale);
        Log.e ("Pulse", "max scale   " + maxScale);

        pulseOut (scale);
    }

    public void fadeHue (float targetHue, long anim) {
        ValueAnimator hueAnimator = ValueAnimator.ofFloat (hue, targetHue);
        hueAnimator.setInterpolator (new LinearInterpolator ());
        hueAnimator.setDuration (anim);
        hueAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                float hsv[] = {(float) animation.getAnimatedValue (), 1, 1};

                int start = Color.HSVToColor (128, hsv);
                int end = Color.HSVToColor (0, hsv);

                pulseDrawable.setColors (new int[] {start, end});
            }
        });

        hueAnimator.addListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd (Animator animation) {
                super.onAnimationEnd (animation);

                hue = (int) targetHue;
            }

            @Override
            public void onAnimationStart (Animator animation) {
                super.onAnimationStart (animation);
            }
        });
        hueAnimator.start ();
    }

}
