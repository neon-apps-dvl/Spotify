package com.pixel.spotify.ui.mainfragment;

import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static neon.pixel.components.Components.getPx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.palette.graphics.Palette;

import com.google.android.material.button.MaterialButton;
import com.pixel.spotify.R;
import com.pixel.spotify.spotify.models.Playlists;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.ui.AlbumButton;
import com.pixel.spotify.ui.PlaylistSelector;
import com.pixel.spotify.ui.SeekBar;
import com.pixel.spotify.ui.TrackInfoView;
import com.pixel.spotify.ui.TrackView;
import com.pixel.spotify.ui.color.ColorProfile;

import neon.pixel.components.android.dynamictheme.OnThemeChangedListener;
import neon.pixel.components.android.theme.Theme;
import neon.pixel.components.color.Hct;

public class DragView extends CoordinatorLayout implements OnThemeChangedListener {
    private static final String TAG = "DragView";

    @LayoutRes
    private static final int LAYOUT = R.layout.layout_drag_view;

    public MainFragment mMainFragment;

    private CoordinatorLayout mMediaControlsContainer;
    private CoordinatorLayout mTrackViewContainer;

    private TrackView mTrackView;
    private int mSelectedTrack;
    private Playlists mPlaylists;

    private InteractionListener mMainFragmentInteractionListener;

    private float mTrackViewX;
    private float mTrackViewY;

    private float mTrackInfoViewX;
    private float mTrackInfoViewY;

    private float mPlayButtonX;
    private float mPlayButtonY;

    private float mSeekBarX;
    private float mSeekBarY;

    private int mDismissBound;
    private int mPeekBound;
    private int mShowBound;

    private Object scaleLock = null;

    private TrackModel mTrack;

    private PlaylistSelector mPlaylistSelector;
    private MaterialButton mSelectPlaylistButton;

    private AlbumButton mAlbumButton;

    private MaterialButton mPlayButton;
    private SeekBar mSeekBar;
    private TrackInfoView mTrackInfoView;

    private boolean mIsPlaying = false;

    private int themeColor;

    private boolean mEnabled = true;

    @SuppressLint ({"ClickableViewAccessibility", "ResourceType"})
    public DragView (@NonNull Context context, MainFragment mainFragment) {
        super (context);
        mMainFragment = mainFragment;

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);
        setLayoutParams (new ViewGroup.LayoutParams (-1, -1));
        setBackgroundColor (context.getColor (android.R.color.transparent));

        mPlaylistSelector = new PlaylistSelector (context);
        mPlaylistSelector.setLayoutParams (new ViewGroup.LayoutParams (-1, -1));
        mPlaylistSelector.setOnSelectionChangedListener ((selectedPlaylist, pinned) -> {
            if (pinned) {
                Log.e (TAG, "pinned: " + selectedPlaylist.name);

                mSelectedTrack = mPlaylists.items.indexOf (selectedPlaylist);
                mSelectPlaylistButton.setText ("Selected " + selectedPlaylist.name);
                setColor (themeColor);

                mMainFragment.setSelected (selectedPlaylist);
            }
            else {
//                int i = mPlaylists.indexOf (selectedPlaylist);
                selectedPlaylist.trackCount += 1;
//                mPlaylists.set (i, selectedPlaylist);
                mPlaylistSelector.setPlaylists (mSelectedTrack, mPlaylists);

                mMainFragment.push (selectedPlaylist);
                mPlaylistSelector.close ();
            }
        });
        mPlaylistSelector.setOnStateChangedListener (state -> {
            switch (state) {
                case OPEN:
                    mMainFragment.requestHideUi ();
                    break;

                case CLOSED:
                    mMainFragment.requestShowUi ();
                    break;
            }
        });

        mSelectPlaylistButton = findViewById (R.id.select_playlist_button);
        mSelectPlaylistButton.setText ("Add to playlist");
        mSelectPlaylistButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                mPlaylistSelector.open ();
            }
        });

        mAlbumButton = new AlbumButton (context);
        mAlbumButton.setLayoutParams (new CoordinatorLayout.LayoutParams (0, (int) getPx (context, 32)));
        mAlbumButton.setOnClickListener (v -> {
            mMainFragment.open (mTrack.album.uri);
        });

        mPlayButton = (MaterialButton) LayoutInflater.from (context).inflate (R.layout.play_button, null);//new MaterialButton (new ContextThemeWrapper (context, R.style.Widget_Material3_Button_OutlinedButton));
        mPlayButton.setIcon (getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ()));
        mPlayButton.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 64), (int) getPx (context, 64)));
        mPlayButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                if (!mIsPlaying) {
                    mIsPlaying = true;
                    mPlayButton.setIcon (getResources ().getDrawable (R.drawable.ic_pause_24, context.getTheme ()));
                } else if (mIsPlaying) {
                    mIsPlaying = false;
                    mPlayButton.setIcon (getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ()));
                }
            }
        });

        mSeekBar = new SeekBar (context, null);
        mSeekBar.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 180), -2));
        mSeekBar.setNoTheme ();

        mTrackInfoView = new TrackInfoView (context, null);
        mTrackInfoView.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 256), (int) getPx (context, 48)));

        mMediaControlsContainer = findViewById (R.id.ui_container);
        mTrackViewContainer = findViewById (R.id.track_view_container);


        addView (mAlbumButton);

        mMediaControlsContainer.addView (mPlayButton);
        mMediaControlsContainer.addView (mSeekBar);
        mMediaControlsContainer.addView (mTrackInfoView);

        addView (mPlaylistSelector);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);

        if (!changed) return;

        mDismissBound = getWidth () / 4;
        mPeekBound = (int) (0.6 * getWidth ());
        mPeekBound = (int) (0.55 * getWidth ());
        mShowBound = (int) (0.75 * getWidth ());

        mTrackViewX = getWidth () / 2 - getPx (getContext (), 256) / 2;
        mTrackViewY = getHeight () / 2 - getPx (getContext (), 256) / 2;

        mTrackInfoViewX = mTrackViewX;
        mTrackInfoViewY = mTrackViewY + getPx (getContext (), 280);

        mPlayButtonX = mTrackViewX;
        mPlayButtonY = getHeight () - mSeekBar.getHeight () - getPx (getContext (), 24);

        mSeekBarX = mPlayButtonX + getPx (getContext (), 76);
        mSeekBarY = getHeight () - mSeekBar.getHeight () - getPx (getContext (), 24);

        mPlayButton.setX (mPlayButtonX);
        mPlayButton.setY (mPlayButtonY);

        mSeekBar.setX (mSeekBarX);
        mSeekBar.setY (mSeekBarY);

        mTrackInfoView.setX (mTrackInfoViewX);
        mTrackInfoView.setY (mTrackInfoViewY);

        mAlbumButton.setX (getWidth () / 2 - mAlbumButton.getWidth () / 2);
        mAlbumButton.setY (mTrackInfoViewY + mTrackInfoView.getHeight () + getPx (getContext (), 12));

        mPlaylistSelector.setY (getHeight ());
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        return ! mEnabled;
    }

    public void setEnabled (boolean enabled) {
        mEnabled = enabled;
    }

    public void setPlaylists (int selected, Playlists playlists) {
        this.mSelectedTrack = selected;
        this.mPlaylists = playlists;

        mPlaylistSelector.setPlaylists (selected, playlists);


        mSelectPlaylistButton.setText ("Add to " + playlists.items.get (selected).name);
    }

    private void setTrack (TrackModel trackModel) {
        this.mTrack = trackModel;

        if (trackModel.artists.size () == 0) return;

        mTrackView.setTrack (trackModel);
        mTrackInfoView.setParams (trackModel.name, trackModel.artists.get (0).name, trackModel.album.name);
        mAlbumButton.update (trackModel.album.name);
//        mAlbumButton.setText ("Album " + mTrack.album.name);
    }

    public void updateTrack (TrackModel trackModel) {
        mTrackViewContainer.removeView (mTrackView);

        mTrackView = new TrackView (getContext (), this, mTrackViewX/*this.getWidth () / 2*/, mTrackViewY/*this.getHeight () / 2*/);
        mTrackView.setAlpha (0f);
        mTrackView.setInteractionListener (new TrackView.InteractionListener () {
            boolean added = false;

            @Override
            public void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
                dispatchOnPositionChanged (v, x, y, scaleX, scaleY, stretchX, stretchY, down);

                mMediaControlsContainer.setAlpha (1 - (float) Math.sqrt (scaleX * scaleX + scaleY * scaleY));

                if (x + mTrackView.getWidth () / 2 >= mPeekBound && x + mTrackView.getWidth () / 2 < mShowBound) {
                    mMainFragment.peekPlaylistView ();
                    scaleTrackView (mTrackView, 1f);
                } else if (x + mTrackView.getWidth () / 2 >= mShowBound) {
                    mMainFragment.showPlaylistView ();
                    scaleTrackView (mTrackView, 0.5f);

                    if (!down) {
                        mTrackView.setInteractionListener (null);

                        mMainFragment.pushSelected ();

                        mTrackView.animate ()
                                .x (getWidth ())
                                .setDuration (200)
                                .start ();

                        mMediaControlsContainer.animate ()
                                .alpha (1f)
                                .setDuration (200)
                                .start ();

                        mMainFragment.hidePlaylistView ();
                    }
                } else if (x + mTrackView.getWidth () / 2 <= mDismissBound) {
                    scaleTrackView (mTrackView, 0.5f);
                    mMainFragment.glowRed ();
                } else {
                    mMainFragment.hidePlaylistView ();
                    scaleLock = null;
                    scaleTrackView (mTrackView, 1);
                    mMainFragment.glow ();
                }
            }
        });

        mTrackViewContainer.addView (mTrackView);

        Palette palette = Palette.from (trackModel.thumbnails.get (0)).generate ();
        Hct backgroundHct = Hct.fromInt (palette.getDominantSwatch ().getRgb ());
        backgroundHct.setTone (90);

        mTrackView.getViewTreeObserver ().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener () {
            @Override
            public void onGlobalLayout () {
                setTrack (trackModel);

                mTrackView.animate ()
                        .alpha (1f)
                        .setDuration (getResources ().getInteger (android.R.integer.config_longAnimTime))
                        .start ();

                mTrackView.getViewTreeObserver ().removeOnGlobalLayoutListener (this);
            }
        });
    }

    public void seekTo (float length) {

        mSeekBar.seekTo (length);
    }

    public void setTheme (int color) {
        mPlaylistSelector.setTheme (color);

        mSeekBar.updateColor (color);
        mTrackInfoView.updateColor (color);

        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);
        int c = hct.toInt ();

        mSelectPlaylistButton.setTextColor (c);

        updateColor (color);

    }

    private ValueAnimator animator;

    private void updateColor (int color) {
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

    private void setColor (int color) {
        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);

        int colorPrimary = hct.toInt ();

        hct.setTone (SECONDARY);

        int colorSecondary = hct.toInt ();

        mPlayButton.setIconTint (new ColorStateList (new int[][] {new int[] {}}, new int[] {colorPrimary}));

        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed}, // enabled
                new int[] {android.R.attr.state_focused | android.R.attr.state_hovered}, // disabled
                new int[] {android.R.attr.state_focused}, // disabled
                new int[] {android.R.attr.state_hovered}, // unchecked
                new int[] {}
        };

        Color p = Color.valueOf (colorPrimary);

        int [] colors = new int [] {
                Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                Color.argb (0.04f * 255, p.red (), p.green (), p.blue ()),
                Color.argb (0.00f * 255, p.red (), p.green (), p.blue ()),
        };

        int[][] buttonStates = new int[][] {
                new int[] { android.R.attr.checked}, // enabled
                new int[] {-android.R.attr.checked}, // unchecked
        };

        p = Color.valueOf (colorPrimary);

        int [] buttonColors = new int [] {
                Color.argb (255, p.red (), p.green (), p.blue ()),
                Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
        };

        //        <selector xmlns:android="http://schemas.android.com/apk/res/android">
//  <item android:color="?attr/colorPrimary" android:state_checked="true"/>
//  <item android:alpha="0.12" android:color="?attr/colorOnSurface" android:state_checked="false"/>
//</selector>

        ColorStateList ripple = new ColorStateList (states, colors);
        mSelectPlaylistButton.setRippleColor (ripple);
        mSelectPlaylistButton.setStrokeColor (new ColorStateList (buttonStates, buttonColors));

        mAlbumButton.setColor (color);

        SpannableString s = new SpannableString (mSelectPlaylistButton.getText ());
        s.setSpan (new ForegroundColorSpan (colorPrimary),
                0,
                mSelectPlaylistButton.getText ().length () - mPlaylists.items.get (mSelectedTrack).name.length () - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        s.setSpan (new ForegroundColorSpan (colorSecondary),
                mSelectPlaylistButton.getText ().length () - mPlaylists.items.get (mSelectedTrack).name.length (),
                mSelectPlaylistButton.getText ().length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mSelectPlaylistButton.setText (s);

        String album = "Album " + mTrack.album.name;

        SpannableString s2 = new SpannableString (album);
        s2.setSpan (new ForegroundColorSpan (colorPrimary),
                0,
                album.length () - mTrack.album.name.length () - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        s2.setSpan (new ForegroundColorSpan (colorSecondary),
                album.length () - mTrack.album.name.length (),
                album.length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void removeTrackView () {
        mTrackViewContainer.removeView (mTrackView);
    }

    private void flingOut (float velocityX, float velocityY) {
        float targetX = velocityX;
        float targetY = velocityY;

        int durationX = (int) Math.abs ((getWidth () / velocityX));
        int durationY = (int) Math.abs ((getWidth () / velocityY));

        FlingAnimation flingX = new FlingAnimation (mTrackView, DynamicAnimation.TRANSLATION_X);
        flingX.setStartVelocity (velocityX)
//                .setMinValue(0)
//                .setMaxValue(maxScroll)
                .setFriction (0.1f)
                .start ();

        FlingAnimation flingY = new FlingAnimation (mTrackView, DynamicAnimation.TRANSLATION_Y);
        flingY.setStartVelocity (velocityY)
                .setFriction (0.1f)
                .start ();
    }

    private ValueAnimator scaleAnimator;
    private float currentScale;

    private void scaleTrackView (TrackView trackView, float scale) {
        if (currentScale == scale) return;

        if (scaleLock == null) {
            currentScale = scale;
            scaleLock = new Object ();

            scaleAnimator = ValueAnimator.ofFloat (trackView.getScaleX (), scale);
            scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
                @Override
                public void onAnimationUpdate (ValueAnimator animation) {
                    trackView.setScaleX ((Float) animation.getAnimatedValue ());
                    trackView.setScaleY ((Float) animation.getAnimatedValue ());
                }
            });
            scaleAnimator.addListener (new AnimatorListenerAdapter () {
                @Override
                public void onAnimationEnd (Animator animation) {
                    super.onAnimationEnd (animation);

                    scaleLock = null;
                }

                @Override
                public void onAnimationCancel (Animator animation) {
                    super.onAnimationCancel (animation);

                    scaleLock = null;
                }
            });
            scaleAnimator.start ();
        }
    }

    public void setMainFragmentInteractionListener (InteractionListener mainFragmentInteractionListener) {
        mMainFragmentInteractionListener = mainFragmentInteractionListener;
    }

    public void dispatchOnPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
        if (mMainFragmentInteractionListener != null)
            mMainFragmentInteractionListener.onPositionChanged (v, x + getWidth () / 2, y + getHeight () / 2, scaleX, scaleY, stretchX, stretchY, down);
    }

    @Override
    public void onThemeChanged (int id, Theme theme) {
        Log.e ("THEME", "theme changed");

        int c0 = theme.getColor (ColorProfile.SECONDARY);

        Log.e ("THEME", "primary: " + c0);
    }

    public interface InteractionListener {
        void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down);
    }
}
