package com.pixel.spotify;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.pixel.components.bitmap.BitmapTools;
import com.pixel.components.color.Hct;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.Playlists;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotify.ui.DragView;
import com.pixel.spotify.ui.PlaylistView;
import com.pixel.spotify.ui.TrackView;
import com.pixel.spotify.ui.UiState;
import com.pixel.spotifyapi.SpotifyService;

import java.util.List;

public class MainFragment extends Fragment {
    private static final String TAG = "Drag";
    private static final String DEBUG = "MainActivity:DEBUG";

    @LayoutRes
    private static final int LAYOUT = R.layout.fragment_main;

    private SpotifyService spotifyService;

    private CoordinatorLayout surfaceView;
    private CoordinatorLayout playlistViewContainer;
    private View overlay;
    private int overlayColor;

    private PlaylistView playlistView;
    private DragView dragView;

    private UserModel user;

    private TrackModel track;
    private int selected;
    private List <PlaylistModel> playlists;

    private Object postTrackLock;

    private int baseThemeColor;
    private int foregroundThemeColor;
    private int backgroundThemeColor;

    private OnUiStateChangedListener mOnUiStateChangedListener;

    public MainFragment (SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    public void setSpotifyService (SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate (LAYOUT, container, false);
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);

        surfaceView = view.findViewById (R.id.surface_view);
        playlistViewContainer = view.findViewById (R.id.playlist_view_container);
        overlay = view.findViewById (R.id.overlay);//new View (getContext ());
        overlay.setBackgroundColor (0);

        playlistView = new PlaylistView (getContext ());
        playlistViewContainer.addView (playlistView);

        dragView = new DragView (getContext (), this);
        dragView.setInteractionListener (new DragView.InteractionListener () {
            @Override
            public void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
                float stretch = 0.5f * stretchY;

                float viewY = dragView.getY () + dragView.getHeight () / 2 + (stretch * dragView.getHeight () / 2) - playlistView.getHeight () / 2;

                playlistView.setY (viewY);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener (view, new OnApplyWindowInsetsListener () {
            @Override
            public WindowInsetsCompat onApplyWindowInsets (View v, WindowInsetsCompat windowInsets) {
                Insets insets = windowInsets.getInsets (WindowInsetsCompat.Type.systemBars ());

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) dragView.getLayoutParams ();
                params.leftMargin = insets.left;
                params.rightMargin = insets.right;
                params.topMargin = insets.top;
                params.bottomMargin = insets.bottom;

                dragView.setLayoutParams (params);

                ViewGroup.MarginLayoutParams menuButtonParams = new ViewGroup.MarginLayoutParams (50, 50);
                menuButtonParams.topMargin = insets.top;

                return WindowInsetsCompat.CONSUMED;
            }
        });

        ((ConstraintLayout) view).addView (dragView);
    }

    @Override
    public void onResume () {
        super.onResume ();

        View view = getView ();

        playlistView.setX (view.getWidth ());
        playlistView.setY (dragView.getY () + dragView.getHeight () / 2);
    }

    public void setUiStateChangedListener (OnUiStateChangedListener l) {
        mOnUiStateChangedListener = l;
    }

    public void requestHideUi () {
        if (mOnUiStateChangedListener != null) {
            mOnUiStateChangedListener.onUiStateChanged (UiState.HIDDEN);
        }
    }

    public void requestShowUi () {
        Log.e ("debug2", "requestShowUi");

        if (mOnUiStateChangedListener != null) {
            mOnUiStateChangedListener.onUiStateChanged (UiState.VISIBLE);
        }
    }

    public void setEnabled (boolean enabled) {
        dragView.setEnabled (enabled);
    }

    int c = 0;

    public void add () {
        add (playlists.get (selected));
    }

    public void add (PlaylistModel playlist) {
        c = c + 1;
        if (c == 7) c = 0;

        if (postTrackLock == null) {
            postTrackLock = new Object ();

            SpotifyServiceAdapter.addTrack (spotifyService, user.id, playlist.id, track.uri, new SpotifyServiceManagerCallback () {
                @Override
                public void onPostTrack () {
                    SpotifyServiceAdapter.getTrack (spotifyService, user.id, c, new SpotifyServiceManagerCallback () {
                        @Override
                        public void onGetTrack (TrackModel trackModel) {
                            getActivity ().runOnUiThread (() -> {
                                setTrack (trackModel);
                                updateTheme ();
                                postTrackLock = null;
                            });

                        }
                    });
                }
            });
        }
    }


    public void open (String what) {
        Intent i = new Intent (Intent.ACTION_VIEW);
        i.setData (Uri.parse (what));
        i.putExtra (Intent.EXTRA_REFERRER, "android-app://" + getContext ().getPackageName ());

        startActivity (i);
    }

    public void updateTheme () {
        Bitmap thumbnail = track.thumbnails.get (0);

        getTrackThemeColors (thumbnail);

        setBackground (thumbnail);

        glow ();

        dragView.seekTo (1);
        dragView.setTheme (baseThemeColor);
    }

    public void setSelected (PlaylistModel playlist) {
        selected = playlists.indexOf (playlist);
        playlistView.setPlaylist (playlist);

        SharedPreferences prefs = getContext ().getSharedPreferences (user.id, Context.MODE_PRIVATE);
        prefs.edit ()
                .putString ("selected", playlist.id)
                .commit ();
    }

    public void setUser (UserModel user) {
        this.user = user;
    }

    public void setTrack (TrackModel track) {
        this.track = track;
        dragView.seekTo (1);

        dragView.updateTrack (track);
    }

    public void setPlaylists (int selected, Playlists playlists) {
        this.selected = selected;
        this.playlists = playlists.items;

        dragView.setPlaylists (selected, playlists);
        playlistView.setPlaylist (playlists.items.get (selected));
    }

    ValueAnimator xAnimator;
    ValueAnimator scaleAnimator;

    boolean isHidden = true;
    boolean isPeeking = false;
    boolean isShowing = false;

    private ValueAnimator animator;

    public void getTrackThemeColors (Bitmap track) {
        Palette themeColors = Palette.from (track).generate ();

        baseThemeColor = themeColors.getDominantSwatch ().getRgb ();

        Hct backgroundThemeColorHct = Hct.fromInt (baseThemeColor);
        backgroundThemeColorHct.setTone (20);

        backgroundThemeColor = backgroundThemeColorHct.toInt ();

        Hct foregroundThemeColorHct = Hct.fromInt (baseThemeColor);
        foregroundThemeColorHct.setTone (80);

        foregroundThemeColor = foregroundThemeColorHct.toInt ();
    }

    private void glow (int color) {
        animator = ValueAnimator.ofObject (new ArgbEvaluator (), overlayColor, color);
        animator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        animator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                Color c = Color.valueOf ((Integer) animator.getAnimatedValue ());

                overlayColor = Color.argb (128, c.red (), c.green (), c.blue ());

                overlay.setBackgroundColor (overlayColor);
            }
        });

        animator.start ();
    }

    private boolean isDefault = false;

    public void glow () {
        if (isDefault) return;

        isDefault = true;

        glow (backgroundThemeColor);
    }

    public void glowRed () {
        if (!isDefault) return;

        isDefault = false;

        glow (Color.RED);
    }

    public void setBackground (Bitmap bitmap) {
        CoordinatorLayout surfaceViewOverlay = new CoordinatorLayout (getContext ());
        surfaceViewOverlay.setLayoutParams (new ConstraintLayout.LayoutParams (-1, -1));
        surfaceViewOverlay.setBackground (surfaceView.getBackground ());
        surfaceViewOverlay.setRenderEffect (RenderEffect.createBlurEffect (surfaceView.getHeight () / 8, surfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        surfaceView.addView (surfaceViewOverlay);

        surfaceViewOverlay.setAlpha (1f);

        float w = (float) surfaceView.getWidth () / (float) surfaceView.getHeight () * bitmap.getHeight ();
        float x = (bitmap.getWidth () - w) / 2;

        Bitmap resizedBitmap = BitmapTools.from (bitmap, (int) x, 0, (int) w, bitmap.getHeight ());

        surfaceView.setBackground (new BitmapDrawable (getResources (), resizedBitmap));
        surfaceView.setRenderEffect (RenderEffect.createBlurEffect (surfaceView.getHeight () / 8, surfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        surfaceViewOverlay.animate ()
                .alpha (0f)
                .setDuration (200)
                .setListener (new AnimatorListenerAdapter () {
                    @Override
                    public void onAnimationEnd (Animator animation) {
                        surfaceView.removeView (surfaceViewOverlay);
                    }
                })
                .start ();
    }

    public void peekPlaylist () {
        if (isPeeking) return;

        isShowing = false;
        isPeeking = true;
        isHidden = false;

        xAnimator = ValueAnimator.ofFloat (playlistView.getX (), dragView.getWidth () - 100);
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setX ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator = ValueAnimator.ofFloat (playlistView.getScaleX (), 1f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setScaleX ((Float) animation.getAnimatedValue ());
                playlistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }

    public void showPlaylist () {
        if (isShowing) return;

        isShowing = true;
        isPeeking = false;
        isHidden = false;

        xAnimator = ValueAnimator.ofFloat (playlistView.getX (), dragView.getWidth () - playlistView.getWidth ());
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setX ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator = ValueAnimator.ofFloat (playlistView.getScaleX (), 1.25f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setScaleX ((Float) animation.getAnimatedValue ());
                playlistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }

    public void hidePlaylist () {
        if (isHidden) return;

        isShowing = false;
        isPeeking = false;
        isHidden = true;

        xAnimator = ValueAnimator.ofFloat (playlistView.getX (), dragView.getWidth ());
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setX ((Float) animation.getAnimatedValue ());
            }
        });
        xAnimator.addListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd (Animator animation) {
                super.onAnimationEnd (animation);
            }
        });

        scaleAnimator = ValueAnimator.ofFloat (playlistView.getScaleX (), 1f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                playlistView.setScaleX ((Float) animation.getAnimatedValue ());
                playlistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }
}