package com.pixel.spotify.ui;

import static com.pixel.spotify.ui.color.ColorProfile.PRIMARY;
import static neon.pixel.components.Components.getPx;
import static neon.pixel.components.android.color.Color.TONE_LIGHT;
import static neon.pixel.components.android.color.Color.TONE_ON_LIGHT;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.pixel.spotify.R;

import neon.pixel.components.android.dynamictheme.OnThemeChangedListener;
import neon.pixel.components.android.theme.Theme;
import neon.pixel.components.backdrop.Backdrop;
import neon.pixel.components.color.Hct;

public class MainBackdrop extends Backdrop implements OnThemeChangedListener {
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
        mMenuButton.setOnClickListener (v -> {
            if (!mIsOpen) open ();
            else close ();

            if (mListener != null) mListener.onStateChanged (mIsOpen);
        });

        ViewCompat.setOnApplyWindowInsetsListener (mMenuButton, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets (WindowInsetsCompat.Type.systemBars ());

            MarginLayoutParams params = (MarginLayoutParams) mMenuButton.getLayoutParams ();
            params.leftMargin = insets.left + getPx (context, 24);
            params.rightMargin = insets.right;
            params.topMargin = insets.top + getPx (context, 12);
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
        mMenuButton.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .alpha (0f)
                .start ();

    }

    public void showUi () {
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

                setOffset (offset.intValue ());

                int b = (int) (offset / mMaxOffset * getWidth () / 8);

                if (b > 0)
                    mView.setRenderEffect (RenderEffect.createBlurEffect (b, b, Shader.TileMode.CLAMP));
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

                setOffset (offset.intValue ());

                int b = (int) (offset / mMaxOffset * getWidth () / 8);

                if (b > 0)
                    mView.setRenderEffect (RenderEffect.createBlurEffect (b, b, Shader.TileMode.CLAMP));
                else mView.setRenderEffect (null);
            }
        });
        sAnimator.start ();
    }

    public void setOnStateChangedListener (OnStateChangedListener l) {
        mListener = l;
    }

    @Override
    public void onThemeChangedListenerAdded (int id) {

    }

    @Override
    public void onThemeChangedListenerRemoved (int id) {

    }

    @Override
    public void onThemeChanged (int id, Theme theme) {
        int color = theme.getColor (PRIMARY);

        Hct hct = Hct.fromInt (color);
        hct.setTone (TONE_LIGHT);

        int c1 = hct.toInt ();

        hct = Hct.fromInt (color);
        hct.setTone (TONE_ON_LIGHT - 10);
        int c2 = hct.toInt ();

        Color holder = Color.valueOf (c1);

        int[][] mMenuButtonRippleStates = new int[][] {
                new int[] {android.R.attr.state_pressed}, // enabled
                new int[] {android.R.attr.state_focused | android.R.attr.state_hovered}, // disabled
                new int[] {android.R.attr.state_focused}, // disabled
                new int[] {android.R.attr.state_hovered}, // unchecked
                new int[] {}
        };

        int[] mMenuButtonRippleColors = new int[] {
                Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
                Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
                Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
                Color.argb (0.04f * 255, holder.red (), holder.green (), holder.blue ()),
                Color.argb (0.00f * 255, holder.red (), holder.green (), holder.blue ()),
        };

        int[][] mMenuButtonStates = new int[][] {
                new int[] {android.R.attr.checked}, // enabled
                new int[] {-android.R.attr.checked}, // unchecked
        };

        int[] mMenuButtonColors = new int[] {
                Color.argb (255, holder.red (), holder.green (), holder.blue ()),
                Color.argb (255, holder.red (), holder.green (), holder.blue ()),
        };

        mMenuButton.setRippleColor (new ColorStateList (mMenuButtonRippleStates, mMenuButtonRippleColors));
        mMenuButton.setBackgroundTintList (new ColorStateList (new int[][]{{}}, new int[] {c1}));
        mMenuButton.setIconTint (new ColorStateList (new int[][]{{}}, new int[] {c2}));
    }

    public interface OnStateChangedListener {
        void onStateChanged (boolean isOpen);
    }
}
