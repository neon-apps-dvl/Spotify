package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;
import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.pixel.components.color.Hct;
import com.pixel.spotify.R;

public class AlbumButton extends ConstraintLayout {
    @LayoutRes
    private static final int LAYOUT = R.layout.layout_album_button;
    private static final String TAG = "AlbumButton";

    private static int mColor;

    private static int maxWidth;
    private static int textPadding;

    private String mTextStart;
    private CharSequence mText;

    private TextView mTextView;

    public AlbumButton (@NonNull Context context) {
        super (context);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        mTextView = findViewById (R.id.album_view);

        maxWidth = (int) getPx (context, 256);
        textPadding = (int) getPx (context, 16);

        mTextStart = "Album ";
        mText = mTextStart + "%";
    }

    public void update (String album) {
        mText = mTextStart + album;

        Paint p = mTextView.getPaint ();
        Rect b = new Rect ();
        p.getTextBounds (mText, 0, mText.length (), b);

        int rawWidth = b.width () + 2 * textPadding;
        int w = rawWidth <= maxWidth ? rawWidth : maxWidth;

        Log.d (TAG, "updating: " + album + " newWidth: " + w);

        updateText (mText);
        updatePos (w);
        updateWidth (w);
    }

    private void updatePos (int newWidth) {
        int dw = getLayoutParams ().width - newWidth;
        float x = getX () + dw / 2;

        animate ()
                .x (x)
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .start ();
    }

    private void updateWidth (int newWidth) {
        ValueAnimator animator = ValueAnimator.ofInt (getWidth (), newWidth);
        animator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        animator.addUpdateListener (animation -> {
            setLayoutParams (new CoordinatorLayout.LayoutParams ((int) animation.getAnimatedValue (),
                    getLayoutParams ().height));
        });
        animator.start ();
    }

    private void updateText (CharSequence text) {
        mTextView.setText (text);
        setColor (mColor);
    }

    public void setColor (int color) {
        mColor = color;

        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);

        int colorPrimary = hct.toInt ();

        hct.setTone (SECONDARY);
        int colorSecondary = hct.toInt ();

        GradientDrawable b = (GradientDrawable) getResources ().getDrawable (R.drawable.album_button_background, getContext ().getTheme ()).mutate ();
        b.setStroke ((int) getPx (getContext (), 1), colorPrimary);

        setBackground (b);

        Log.d ("text", "applying color to: " + mText);

        SpannableString s = new SpannableString (mText);
        s.setSpan (new ForegroundColorSpan (colorPrimary),
                0,
                mTextStart.length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        s.setSpan (new ForegroundColorSpan (colorSecondary),
                mTextStart.length (),
                mText.length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mTextView.setText (s);
    }
}
