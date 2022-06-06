package com.pixel.spotify.ui;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static neon.pixel.components.Components.getPxF;
import static neon.pixel.components.android.color.Color.TONE_LIGHT;
import static neon.pixel.components.android.color.Color.TONE_ON_CONTAINER_DARK;
import static neon.pixel.components.android.color.Color.TONE_ON_LIGHT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.pixel.spotify.R;
import com.pixel.spotifyapi.Objects.Track;

import neon.pixel.components.color.Hct;

public class MediaControlsView extends CoordinatorLayout {
    private static final String TAG = "MediaControlsView";

    @LayoutRes
    private static final int LAYOUT = R.layout.layout_media_controls;

    private static final int CORNERS_DP = 24;

    private Track mTrack;
    private int mColor;

    private boolean mIsPlaying = false;

    private CoordinatorLayout mContainer;
    private CoordinatorLayout mView;
    private ConstraintLayout mContent;
    private BottomSheetBehavior mBehavior;

    private Drawable playDrawable;
    private Drawable pauseDrawable;

//    private TextView mTrackInfoView;
//    private MaterialButton mPlayButton;
//    private SeekBar mSeekBar;


    public MediaControlsView (@NonNull Context context) {
        super (context, null);
    }

    public MediaControlsView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        setBackground (null);

        mContainer = findViewById (R.id.container);
        mView = findViewById (R.id.view);
        mContent = findViewById (R.id.content);
        mBehavior = BottomSheetBehavior.from (mView);
        mBehavior.addBottomSheetCallback (mCallback);

        playDrawable = getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ());
        pauseDrawable = getResources ().getDrawable (R.drawable.ic_pause_24, context.getTheme ());

//        mTrackInfoView = findViewById (R.id.track_info_view);
//        mPlayButton = findViewById (R.id.button_play);
//        mSeekBar = findViewById (R.id.seek_bar);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);

        if (! changed) return;

//        mPlayButton.setOnClickListener (v -> {
//            if (! mIsPlaying) {
//                mPlayButton.setIcon (pauseDrawable);
//
//                mIsPlaying = true;
//            }
//            else {
//                mPlayButton.setIcon (playDrawable);
//
//                mIsPlaying = false;
//            }
//        });
//
//        mSeekBar.setOnSeekBarChangeListener (new android.widget.SeekBar.OnSeekBarChangeListener () {
//            @Override
//            public void onProgressChanged (android.widget.SeekBar seekBar, int progress, boolean fromUser) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch (android.widget.SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch (android.widget.SeekBar seekBar) {
//
//            }
//        });
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged (w, h, oldw, oldh);

        mContainer.setOutlineProvider (new ViewOutlineProvider () {
            @Override
            public void getOutline (View view, Outline outline) {
                outline.setRoundRect (
                        view.getLeft (),
                        view.getTop (),
                        view.getRight (),
                        view.getBottom (),
                        getPxF (getContext (), CORNERS_DP)
                );
            }
        });
    }

    public void setTrackInfo (Track track) {
        mTrack = track;

        setColor (mColor);
//        mTrackInfoView.setAutoSizeTextTypeUniformWithConfiguration (getPx (getContext (), 12), getPx (getContext (), 24), 1, TypedValue.COMPLEX_UNIT_PX);
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

        mContent.setBackgroundTintList (new ColorStateList (new int[][] {{}}, new int[] {c3}));

//        mPlayButton.setIconTint (new ColorStateList (new int[][] {{}}, new int[] {c2}));
//        mPlayButton.setRippleColor (new ColorStateList (new int[][] {{}}, new int[] {c1}));

//        mSeekBar.setProgressTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
//        mSeekBar.setProgressBackgroundTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
//        mSeekBar.setThumbTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));
//        mSeekBar.setTickMarkTintList (new ColorStateList (new int[][] {{}}, new int[] {c2}));

//        if (mTrack != null) {
//            String title = mTrack.name;
//            String artist = mTrack.artists.get (0).name;
//
//            SpannableString s = new SpannableString (title + " " + artist);
//            s.setSpan (new ForegroundColorSpan (c1), 0, title.length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            s.setSpan (new ForegroundColorSpan (c2), title.length () + 1, s.toString ().length (), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            mTrackInfoView.setText (s);
//        }
    }

    public void addBottomSheetCallback (BottomSheetBehavior.BottomSheetCallback c) {
        mBehavior.addBottomSheetCallback (c);
    }

    private BottomSheetBehavior.BottomSheetCallback mCallback = new BottomSheetBehavior.BottomSheetCallback () {
        boolean isOpen;

        @Override
        public void onStateChanged (@NonNull View bottomSheet, int newState) {
            if (newState != STATE_COLLAPSED) {

            }

            if (newState == STATE_COLLAPSED) ;
            else if (newState == STATE_EXPANDED) ;
        }

        @Override
        public void onSlide (@NonNull View bottomSheet, float slideOffset) {

        }
    };
}
