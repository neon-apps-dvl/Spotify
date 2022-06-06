package com.pixel.spotify.ui;

import static neon.pixel.components.android.color.Color.TONE_LIGHT;
import static neon.pixel.components.android.color.Color.TONE_ON_CONTAINER_DARK;
import static neon.pixel.components.android.color.Color.TONE_ON_LIGHT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.google.android.material.button.MaterialButton;
import com.pixel.spotify.R;
import com.pixel.spotifyapi.Objects.Track;

import neon.pixel.components.color.Hct;

public class PlayerControlsView extends ViewGroup {
    @LayoutRes
    private static final int LAYOUT = R.layout.layout_player_contols;

    private Track mTrack;
    private int mColor;

    private boolean mIsPlaying;

    private Drawable playDrawable;
    private Drawable pauseDrawable;

    private TextView mTrackInfoView;
    private MaterialButton mPlayButton;
    private SeekBar mSeekBar;

    public PlayerControlsView (Context context) {
        super (context, null);
    }

    public PlayerControlsView (Context context, AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);



        playDrawable = getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ());
        pauseDrawable = getResources ().getDrawable (R.drawable.ic_pause_24, context.getTheme ());
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        if (! changed) return;

        if (true) return;

        mPlayButton.setOnClickListener (v -> {
            if (! mIsPlaying) {
                mPlayButton.setIcon (pauseDrawable);

                mIsPlaying = true;
            }
            else {
                mPlayButton.setIcon (playDrawable);

                mIsPlaying = false;
            }
        });

        mSeekBar.setOnSeekBarChangeListener (new android.widget.SeekBar.OnSeekBarChangeListener () {
            @Override
            public void onProgressChanged (android.widget.SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch (android.widget.SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch (android.widget.SeekBar seekBar) {

            }
        });
    }

    public void setColor (int color) {
        mColor = color;

        Hct hct = Hct.fromInt (color);
        hct.setTone (TONE_ON_LIGHT - 10);
        int c1 = hct.toInt ();

        hct = Hct.fromInt (color);
        hct.setTone (TONE_ON_CONTAINER_DARK);
        int c2 = hct.toInt ();

        hct = Hct.fromInt (color);
        hct.setTone (TONE_LIGHT);
        int c3 = hct.toInt ();

        mPlayButton.setIconTint (new ColorStateList (new int[][] {{}}, new int[] {c2}));
        mPlayButton.setRippleColor (new ColorStateList (new int[][] {{}}, new int[] {c1}));

        mSeekBar.setProgressTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
        mSeekBar.setProgressBackgroundTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
        mSeekBar.setThumbTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
        mSeekBar.setTickMarkTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));

        if (mTrack != null) {
            String title = mTrack.name;
            String artist = mTrack.artists.get (0).name;

            SpannableString s = new SpannableString (title + " " + artist);
            s.setSpan (new ForegroundColorSpan (c1), 0, title.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan (new ForegroundColorSpan (c2), title.length () + 1, s.toString ().length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTrackInfoView.setText (s);
        }
    }
}
