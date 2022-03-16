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

import neon.pixel.components.color.Hct;

public class DragView extends CoordinatorLayout {
    private static final String TAG = "DragView";

    @LayoutRes
    private static final int LAYOUT = R.layout.layout_drag_view;

    public MainFragment mMainFragment;

    private CoordinatorLayout mediaControlsContainer;
    private CoordinatorLayout trackViewContainer;

    private TrackView trackView;
    private int selected;
    private Playlists playlists;

    private InteractionListener interactionListener;

    float trackViewAnchorX;
    float trackViewAnchorY;

    float trackInfoViewAnchorX;
    float trackInfoViewAnchorY;

    float playButtonAnchorX;
    float playButtonAnchorY;

    float seekBarAnchorX;
    float seekBarAnchorY;

    int quickAddViewAnchorX;
    int quickAddViewAnchorY;

    int dismissBound;
    int peekBound;
    int showBound;
    int addBound;

    private Object scaleLock = null;

    private TrackModel trackModel;

    private Palette trackColors;

    private PlaylistSelector playlistSelector;
    private MaterialButton selectPlaylistButton;

//    private Chip albumButton;
    private AlbumButton albumButton;

    private MaterialButton playButton;
    private SeekBar seekBar;
    private TrackInfoView trackInfoView;

    private boolean isPlaying = false;

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

        playlistSelector = new PlaylistSelector (context);
        playlistSelector.setOnSelectionChangedListener ((selectedPlaylist, pinned) -> {
            if (pinned) {
                Log.e (TAG, "pinned: " + selectedPlaylist.name);

                selected = playlists.items.indexOf (selectedPlaylist);
                selectPlaylistButton.setText ("Selected " + selectedPlaylist.name);
                setColor (themeColor);

                mMainFragment.setSelected (selectedPlaylist);
            }
            else {
//                int i = playlists.indexOf (selectedPlaylist);
                selectedPlaylist.trackCount += 1;
//                playlists.set (i, selectedPlaylist);
                playlistSelector.setPlaylists (selected, playlists);

                mMainFragment.add (selectedPlaylist);
                playlistSelector.close ();
            }
        });
        playlistSelector.setOnStateChangedListener (state -> {
            switch (state) {
                case OPEN:
                    mMainFragment.requestHideUi ();
                    break;

                case CLOSED:
                    mMainFragment.requestShowUi ();
                    break;
            }
        });

        selectPlaylistButton = findViewById (R.id.select_playlist_button);
        selectPlaylistButton.setText ("Add to playlist");
        selectPlaylistButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                playlistSelector.open ();
            }
        });

        albumButton = new AlbumButton (context);
        albumButton.setLayoutParams (new CoordinatorLayout.LayoutParams (0, (int) getPx (context, 32)));
        albumButton.setOnClickListener (v -> {
            mMainFragment.open (trackModel.album.uri);
        });

        playButton = (MaterialButton) LayoutInflater.from (context).inflate (R.layout.play_button, null);//new MaterialButton (new ContextThemeWrapper (context, R.style.Widget_Material3_Button_OutlinedButton));
        playButton.setIcon (getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ()));
        playButton.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 64), (int) getPx (context, 64)));
        playButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                if (!isPlaying) {
                    isPlaying = true;
                    playButton.setIcon (getResources ().getDrawable (R.drawable.ic_pause_24, context.getTheme ()));
                } else if (isPlaying) {
                    isPlaying = false;
                    playButton.setIcon (getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ()));
                }
            }
        });

        seekBar = new SeekBar (context, null);
        seekBar.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 180), -2));
        seekBar.setNoTheme ();

        trackInfoView = new TrackInfoView (context, null);
        trackInfoView.setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 256), (int) getPx (context, 48)));

        mediaControlsContainer = findViewById (R.id.media_controls_container);
        trackViewContainer = findViewById (R.id.track_view_container);

//        ConstraintLayout albumButtonWrapper = findViewById (R.id.album_button_wrapper);
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams (-2, -2);
//        params.leftToLeft = albumButtonWrapper.getId ();
//        params.rightToRight = albumButtonWrapper.getId ();
//        albumButton.setLayoutParams (params);
//        albumButtonWrapper.addView (albumButton);

        addView (albumButton);

        mediaControlsContainer.addView (playButton);
        mediaControlsContainer.addView (seekBar);
        mediaControlsContainer.addView (trackInfoView);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);

        if (!changed) return;

        dismissBound = getWidth () / 4;
        peekBound = (int) (0.6 * getWidth ());
        peekBound = (int) (0.55 * getWidth ());
        showBound = (int) (0.75 * getWidth ());
        addBound = (int) (0.75 * getWidth ());

        trackViewAnchorX = getWidth () / 2 - getPx (getContext (), 256) / 2;
        trackViewAnchorY = getHeight () / 2 - getPx (getContext (), 256) / 2;

        trackInfoViewAnchorX = trackViewAnchorX;
        trackInfoViewAnchorY = trackViewAnchorY + getPx (getContext (), 280);

        playButtonAnchorX = trackViewAnchorX;
        playButtonAnchorY = getHeight () - seekBar.getHeight () - getPx (getContext (), 24);

        seekBarAnchorX = playButtonAnchorX + getPx (getContext (), 76);
        seekBarAnchorY = getHeight () - seekBar.getHeight () - getPx (getContext (), 24);

        quickAddViewAnchorX = getWidth ();
        quickAddViewAnchorY = (int) (getHeight () / 2 - getPx (getContext (), 128));

        playButton.setX (playButtonAnchorX);
        playButton.setY (playButtonAnchorY);

        seekBar.setX (seekBarAnchorX);
        seekBar.setY (seekBarAnchorY);

        trackInfoView.setX (trackInfoViewAnchorX);
        trackInfoView.setY (trackInfoViewAnchorY);

        albumButton.setX (getWidth () / 2 - albumButton.getWidth () / 2);
        albumButton.setY (trackInfoViewAnchorY + trackInfoView.getHeight () + getPx (getContext (), 12));

        playlistSelector.setLayoutParams (new ViewGroup.LayoutParams (getWidth (), getHeight ()));
        playlistSelector.setY (getHeight ());
        addView (playlistSelector);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        return ! mEnabled;
    }

    public void setEnabled (boolean enabled) {
        mEnabled = enabled;
    }

    public void setPlaylists (int selected, Playlists playlists) {
        this.selected = selected;
        this.playlists = playlists;

        playlistSelector.setPlaylists (selected, playlists);


        selectPlaylistButton.setText ("Add to " + playlists.items.get (selected).name);
    }

    private void setTrack (TrackModel trackModel) {
        this.trackModel = trackModel;

        if (trackModel.artists.size () == 0) return;

        trackView.setTrack (trackModel);
        trackInfoView.setParams (trackModel.name, trackModel.artists.get (0).name, trackModel.album.name);
        albumButton.update (trackModel.album.name);
//        albumButton.setText ("Album " + trackModel.album.name);
    }

    public void updateTrack (TrackModel trackModel) {
        trackViewContainer.removeView (trackView);

        trackView = new TrackView (getContext (), this, trackViewAnchorX/*this.getWidth () / 2*/, trackViewAnchorY/*this.getHeight () / 2*/);
        trackView.setAlpha (0f);
        trackView.setInteractionListener (new TrackView.InteractionListener () {
            boolean added = false;

            @Override
            public void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
                dispatchOnPositionChanged (v, x, y, scaleX, scaleY, stretchX, stretchY, down);

                mediaControlsContainer.setAlpha (1 - (float) Math.sqrt (scaleX * scaleX + scaleY * scaleY));

                if (x + trackView.getWidth () / 2 >= peekBound && x + trackView.getWidth () / 2 < showBound) {
                    mMainFragment.peekPlaylist ();
                    scaleTrackView (trackView, 1f);
                } else if (x + trackView.getWidth () / 2 >= showBound) {
                    mMainFragment.showPlaylist ();
                    scaleTrackView (trackView, 0.5f);

                    if (!down) {
                        trackView.setInteractionListener (null);

                        mMainFragment.add ();

                        trackView.animate ()
                                .x (getWidth ())
                                .setDuration (200)
                                .start ();

                        mediaControlsContainer.animate ()
                                .alpha (1f)
                                .setDuration (200)
                                .start ();

                        mMainFragment.hidePlaylist ();
                    }
                } else if (x + trackView.getWidth () / 2 <= dismissBound) {
                    scaleTrackView (trackView, 0.5f);
                    mMainFragment.glowRed ();
                } else {
                    mMainFragment.hidePlaylist ();
                    scaleLock = null;
                    scaleTrackView (trackView, 1);
                    mMainFragment.glow ();
                }
            }
        });

        trackViewContainer.addView (trackView);

        Palette palette = Palette.from (trackModel.thumbnails.get (0)).generate ();
        Hct backgroundHct = Hct.fromInt (palette.getDominantSwatch ().getRgb ());
        backgroundHct.setTone (90);

        trackView.getViewTreeObserver ().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener () {
            @Override
            public void onGlobalLayout () {
                setTrack (trackModel);

                trackView.animate ()
                        .alpha (1f)
                        .setDuration (getResources ().getInteger (android.R.integer.config_longAnimTime))
                        .start ();

                trackView.getViewTreeObserver ().removeOnGlobalLayoutListener (this);
            }
        });
    }

    public void seekTo (float length) {

        seekBar.seekTo (length);
    }

    public void setTheme (int color) {
        playlistSelector.setTheme (color);

        seekBar.updateColor (color);
        trackInfoView.updateColor (color);

        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);
        int c = hct.toInt ();

        selectPlaylistButton.setTextColor (c);

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

        playButton.setIconTint (new ColorStateList (new int[][] {new int[] {}}, new int[] {colorPrimary}));

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
        selectPlaylistButton.setRippleColor (ripple);
        selectPlaylistButton.setStrokeColor (new ColorStateList (buttonStates, buttonColors));

        albumButton.setColor (color);

        SpannableString s = new SpannableString (selectPlaylistButton.getText ());
        s.setSpan (new ForegroundColorSpan (colorPrimary),
                0,
                selectPlaylistButton.getText ().length () - playlists.items.get (selected).name.length () - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        s.setSpan (new ForegroundColorSpan (colorSecondary),
                selectPlaylistButton.getText ().length () - playlists.items.get (selected).name.length (),
                selectPlaylistButton.getText ().length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        selectPlaylistButton.setText (s);

        String album = "Album " + trackModel.album.name;

        SpannableString s2 = new SpannableString (album);
        s2.setSpan (new ForegroundColorSpan (colorPrimary),
                0,
                album.length () - trackModel.album.name.length () - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        s2.setSpan (new ForegroundColorSpan (colorSecondary),
                album.length () - trackModel.album.name.length (),
                album.length (),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void removeTrackView () {
        trackViewContainer.removeView (trackView);
    }

    private void flingOut (float velocityX, float velocityY) {
        float targetX = velocityX;
        float targetY = velocityY;

        int durationX = (int) Math.abs ((getWidth () / velocityX));
        int durationY = (int) Math.abs ((getWidth () / velocityY));

        FlingAnimation flingX = new FlingAnimation (trackView, DynamicAnimation.TRANSLATION_X);
        flingX.setStartVelocity (velocityX)
//                .setMinValue(0)
//                .setMaxValue(maxScroll)
                .setFriction (0.1f)
                .start ();

        FlingAnimation flingY = new FlingAnimation (trackView, DynamicAnimation.TRANSLATION_Y);
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

    public void setInteractionListener (InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    public void dispatchOnPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
        if (interactionListener != null)
            interactionListener.onPositionChanged (v, x + getWidth () / 2, y + getHeight () / 2, scaleX, scaleY, stretchX, stretchY, down);
    }

    public interface InteractionListener {
        void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down);
    }
}
