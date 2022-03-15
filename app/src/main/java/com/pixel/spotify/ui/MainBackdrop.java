package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.pixel.components.backdrop.Backdrop;
import com.pixel.spotify.R;

public class MainBackdrop extends Backdrop {
    private static final int BACK_VIEW_PADDING_DP = 24;
    private float mMaxOffset;

    private CoordinatorLayout mView;

    private MaterialButton mMenuButton;
    private boolean mIsOpen = false;
    private OnStateChangedListener mListener;

    private static ValueAnimator sAnimator;


    public MainBackdrop (@NonNull Context context) {
        super (context);
    }

    public MainBackdrop (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        mView = new CoordinatorLayout (context);
        mView.setLayoutParams (new CoordinatorLayout.LayoutParams (-1, -1));
        mView.setBackground (null);

        mMenuButton = (MaterialButton) LayoutInflater.from (getContext ()).inflate (R.layout.menu_button, null);
//        mMenuButton.setY (200);
        mMenuButton.setOnClickListener (v -> {
            if (! mIsOpen) open ();
            else close ();

            if (mListener != null) mListener.onStateChanged (mIsOpen);
        });

        ViewCompat.setOnApplyWindowInsetsListener (mMenuButton, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets (WindowInsetsCompat.Type.systemBars ());

            MarginLayoutParams params = (MarginLayoutParams) mMenuButton.getLayoutParams ();
            params.leftMargin = (int) (insets.left + getPx (context, 24));
            params.rightMargin = insets.right;
            params.topMargin = (int) (insets.top + getPx (context, 12));
            params.bottomMargin = insets.bottom;

            return WindowInsetsCompat.CONSUMED;
        });

        mFrontView.addView (mView);
        mFrontView.addView (mMenuButton);
    }

    @Override
    public void setBackView (View v) {
        super.setBackView (v);

        mMaxOffset = (int) (v.getLayoutParams ().height + 2 * getPx (getContext (), BACK_VIEW_PADDING_DP));
    }

    @Override
    public void setFrontView (View v) {
        mView.removeAllViews ();
        mView.addView (v);
    }

    public void hideUi () {
        Log.e ("debug2", "hideUi");

        mMenuButton.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .alpha (0f)
                .start ();

    }

    public void showUi () {
        Log.e ("debug2", "showUi");

        mMenuButton.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .alpha (1f)
                .start ();

    }

    public void open () {
        if (sAnimator != null && sAnimator.isRunning ()) sAnimator.cancel ();

        mIsOpen = true;

        mMaxOffset = 500; // FIXME: temp

        sAnimator = ValueAnimator.ofFloat (getOffset (), mMaxOffset);
        sAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        sAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                Float offset = (Float) animation.getAnimatedValue ();

                Log.d ("offset", "offset: " + offset);

                setOffset (offset.intValue ());

                int b = (int) (offset / mMaxOffset * getWidth () / 8);

                if (b > 0) mView.setRenderEffect (RenderEffect.createBlurEffect (b, b, Shader.TileMode.CLAMP));
                else mView.setRenderEffect (null);
            }
        });
        sAnimator.start ();
    }

    public void close () {
        if (sAnimator != null && sAnimator.isRunning ()) sAnimator.cancel ();

        mIsOpen = false;

        sAnimator = ValueAnimator.ofFloat (getOffset (), 0);
        sAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        sAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                Float offset = (Float) animation.getAnimatedValue ();

                Log.e ("offset", "offset: " + offset);

                setOffset (offset.intValue ());

                int b = (int) (offset / mMaxOffset * getWidth () / 8);

                if (b > 0) mView.setRenderEffect (RenderEffect.createBlurEffect (b, b, Shader.TileMode.CLAMP));
                else mView.setRenderEffect (null);
            }
        });
        sAnimator.start ();
    }

    public void setOnStateChangedListener (OnStateChangedListener l) {
        mListener = l;
    }

    public interface OnStateChangedListener {
        void onStateChanged (boolean isOpen);
    }
}
