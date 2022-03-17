package com.pixel.spotify.ui.mainfragment;

import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SURFACE;
import static com.pixel.spotify.ui.color.Color.UI_THEME;
import static neon.pixel.components.Components.getPx;

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
import android.view.ViewPropertyAnimator;

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

import com.pixel.spotify.OnUiStateChangedListener;
import com.pixel.spotify.R;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.Playlists;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotify.ui.PlaylistView;
import com.pixel.spotify.ui.TrackView;
import com.pixel.spotify.ui.UiState;
import com.pixel.spotify.ui.color.ColorProfile;
import com.pixel.spotifyapi.SpotifyService;

import java.util.List;

import neon.pixel.components.android.dynamictheme.DynamicTheme;
import neon.pixel.components.android.theme.Theme;
import neon.pixel.components.bitmap.BitmapTools;
import neon.pixel.components.color.Hct;

public class MainFragment extends Fragment {
    private static final String TAG = "Drag";
    private static final String DEBUG = "MainActivity:DEBUG";

    @LayoutRes
    private static final int LAYOUT = R.layout.fragment_main;

    private SpotifyService mSpotifyService;

    private CoordinatorLayout mSurfaceView;
    private CoordinatorLayout mPlaylistViewContainer;
    private View mSurfaceViewOverlay;
    private int mSurfaceViewOverlayColor;

    private PlaylistView mPlaylistView;
    private DragView mDragView;

    private UserModel mUser;

    private TrackModel mTrack;
    private int mSelected;
    private List <PlaylistModel> mPlaylists;

    private Object mPostTrackLock;

    private int mBaseThemeColor;
    private int mColorPrimary;
    private int mColorSecondary;
    private int mColorSurface;

    private boolean mIsPlaylistViewHidden = true;
    private boolean mIsPlaylistViewPeeking = false;
    private boolean mIsPlaylistViewShowing = false;

    private OnUiStateChangedListener mOnUiStateChangedListener;

    public MainFragment (SpotifyService spotifyService) {
        mSpotifyService = spotifyService;
    }

    public void setSpotifyService (SpotifyService spotifyService) {
        mSpotifyService = spotifyService;
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

        mSurfaceView = view.findViewById (R.id.surface_view);
        mPlaylistViewContainer = view.findViewById (R.id.playlist_view_container);
        mSurfaceViewOverlay = view.findViewById (R.id.overlay);//new View (getContext ());
        mSurfaceViewOverlay.setBackgroundColor (0);

        mPlaylistView = new PlaylistView (getContext ());
        mPlaylistViewContainer.addView (mPlaylistView);

        mDragView = new DragView (getContext (), this);
        mDragView.setMainFragmentInteractionListener (new DragView.InteractionListener () {
            @Override
            public void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
                float stretch = 0.5f * stretchY;

                float viewY = mDragView.getY () + mDragView.getHeight () / 2 + (stretch * mDragView.getHeight () / 2) - mPlaylistView.getHeight () / 2;

                mPlaylistView.setY (viewY);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener (view, new OnApplyWindowInsetsListener () {
            @Override
            public WindowInsetsCompat onApplyWindowInsets (View v, WindowInsetsCompat windowInsets) {
                Insets insets = windowInsets.getInsets (WindowInsetsCompat.Type.systemBars ());

                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mDragView.getLayoutParams ();
                params.leftMargin = insets.left;
                params.rightMargin = insets.right;
                params.topMargin = insets.top;
                params.bottomMargin = insets.bottom;

                mDragView.setLayoutParams (params);

                ViewGroup.MarginLayoutParams menuButtonParams = new ViewGroup.MarginLayoutParams (50, 50);
                menuButtonParams.topMargin = insets.top;

                return WindowInsetsCompat.CONSUMED;
            }
        });

        ((ConstraintLayout) view).addView (mDragView);
    }

    @Override
    public void onResume () {
        super.onResume ();

        View view = getView ();

        mPlaylistView.setX (view.getWidth ());
        mPlaylistView.setY (mDragView.getY () + mDragView.getHeight () / 2);

        DynamicTheme.addOnThemeChangedListener (UI_THEME, mDragView);
    }

    public void setUser (UserModel user) {
        this.mUser = user;
    }

    public void setTrack (TrackModel track) {
        this.mTrack = track;
        mDragView.seekTo (1);

        mDragView.newTrackView (track);
    }

    public void setPlaylists (int selected, Playlists playlists) {
        this.mSelected = selected;
        this.mPlaylists = playlists.items;

        mDragView.setPlaylists (selected, playlists);
        mPlaylistView.setPlaylist (playlists.items.get (selected));
    }

    public void setSelected (PlaylistModel playlist) {
        mSelected = mPlaylists.indexOf (playlist);
        mPlaylistView.setPlaylist (playlist);

        SharedPreferences prefs = getContext ().getSharedPreferences (mUser.id, Context.MODE_PRIVATE);
        prefs.edit ()
                .putString ("mSelected", playlist.id)
                .commit ();
    }

    public void pushSelected () {
        push (mPlaylists.get (mSelected));
    }

    int c = 0;

    public void push (PlaylistModel playlist) {
        c = c + 1;
        if (c == 7) c = 0;

        if (mPostTrackLock == null) {
            mPostTrackLock = new Object ();

            SpotifyServiceAdapter.addTrack (mSpotifyService, mUser.id, playlist.id, mTrack.uri, new SpotifyServiceManagerCallback () {
                @Override
                public void onPostTrack () {
                    SpotifyServiceAdapter.getTrack (mSpotifyService, mUser.id, c, new SpotifyServiceManagerCallback () {
                        @Override
                        public void onGetTrack (TrackModel trackModel) {
                            getActivity ().runOnUiThread (() -> {
                                setTrack (trackModel);
                                updateTheme ();
                                mPostTrackLock = null;
                            });

                        }
                    });
                }
            });
        }
    }

    public void setEnabled (boolean enabled) {
        mDragView.setEnabled (enabled);
    }

    public void setUiStateChangedListener (OnUiStateChangedListener l) {
        mOnUiStateChangedListener = l;
    }

    public void open (String what) {
        Intent i = new Intent (Intent.ACTION_VIEW);
        i.setData (Uri.parse (what));
        i.putExtra (Intent.EXTRA_REFERRER, "android-app://" + getContext ().getPackageName ());

        startActivity (i);
    }

    public void requestShowUi () {
        Log.e ("debug2", "requestShowUi");

        if (mOnUiStateChangedListener != null) {
            mOnUiStateChangedListener.onUiStateChanged (UiState.VISIBLE);
        }
    }

    public void requestHideUi () {
        if (mOnUiStateChangedListener != null) {
            mOnUiStateChangedListener.onUiStateChanged (UiState.HIDDEN);
        }
    }

    public void updateTheme () {
        Bitmap thumbnail = mTrack.thumbnails.get (0);

        setThemeColors (thumbnail);

        Theme theme = DynamicTheme.getTheme (UI_THEME);
        theme.setColor (ColorProfile.MAIN, mBaseThemeColor);
        theme.setColor (ColorProfile.SURFACE, mColorSurface);
        theme.setColor (ColorProfile.PRIMARY, mColorPrimary);
        theme.setColor (ColorProfile.SECONDARY, mColorSecondary);
        DynamicTheme.notifyThemeChanged (UI_THEME);

        setSurfaceView (thumbnail);

        glow ();

        mDragView.seekTo (1);
        //mDragView.setColor (mBaseThemeColor);
    }

    private void setThemeColors (Bitmap thumbnail) {
        Palette themeColors = Palette.from (thumbnail).generate ();

        mBaseThemeColor = themeColors.getDominantSwatch ().getRgb ();

        Hct hct = Hct.fromInt (mBaseThemeColor);
        hct.setTone (SURFACE);
        mColorSurface = hct.toInt ();

        hct = Hct.fromInt (mBaseThemeColor);
        hct.setTone (PRIMARY);
        mColorPrimary = hct.toInt ();

        hct = Hct.fromInt (mBaseThemeColor);
        hct.setTone (SECONDARY);
        mColorSurface = hct.toInt ();
    }

    private void setSurfaceViewOverlayColor (int color) {
        ValueAnimator surfaceViewOverlayColorAnimator = ValueAnimator.ofObject (new ArgbEvaluator (), mSurfaceViewOverlayColor, color);
        surfaceViewOverlayColorAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        surfaceViewOverlayColorAnimator.addUpdateListener (animation -> {
            Color c = Color.valueOf ((Integer) surfaceViewOverlayColorAnimator.getAnimatedValue ());

            mSurfaceViewOverlayColor = Color.argb (128, c.red (), c.green (), c.blue ());

            mSurfaceViewOverlay.setBackgroundColor (mSurfaceViewOverlayColor);
        });

        surfaceViewOverlayColorAnimator.start ();
    }

    private boolean isDefault = false;

    public void glow () {
        if (isDefault) return;

        isDefault = true;

        setSurfaceViewOverlayColor (mColorSurface);
    }

    public void glowRed () {
        if (!isDefault) return;

        isDefault = false;

        setSurfaceViewOverlayColor (Color.RED);
    }

    private void setSurfaceView (Bitmap bitmap) {
        CoordinatorLayout surfaceViewOverlay = new CoordinatorLayout (getContext ());
        surfaceViewOverlay.setLayoutParams (new ConstraintLayout.LayoutParams (-1, -1));
        surfaceViewOverlay.setBackground (mSurfaceView.getBackground ());
        surfaceViewOverlay.setRenderEffect (RenderEffect.createBlurEffect (mSurfaceView.getHeight () / 8, mSurfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        mSurfaceView.addView (surfaceViewOverlay);

        surfaceViewOverlay.setAlpha (1f);

        float w = (float) mSurfaceView.getWidth () / (float) mSurfaceView.getHeight () * bitmap.getHeight ();
        float x = (bitmap.getWidth () - w) / 2;

        Bitmap resizedBitmap = BitmapTools.from (bitmap, (int) x, 0, (int) w, bitmap.getHeight ());

        mSurfaceView.setBackground (new BitmapDrawable (getResources (), resizedBitmap));
        mSurfaceView.setRenderEffect (RenderEffect.createBlurEffect (mSurfaceView.getHeight () / 8, mSurfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        surfaceViewOverlay.animate ()
                .alpha (0f)
                .setDuration (200)
                .setListener (new AnimatorListenerAdapter () {
                    @Override
                    public void onAnimationEnd (Animator animation) {
                        mSurfaceView.removeView (surfaceViewOverlay);
                    }
                })
                .start ();
    }

    public void peekPlaylistView () {
        if (mIsPlaylistViewPeeking) return;

        mIsPlaylistViewShowing = false;
        mIsPlaylistViewPeeking = true;
        mIsPlaylistViewHidden = false;

        ViewPropertyAnimator playlistViewPositionAnimator = mPlaylistView.animate ()
                .x (mDragView.getWidth () - getPx (getContext (), 64))
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));

        ViewPropertyAnimator playlistViewScaleAnimator = mPlaylistView.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .scaleX (1)
                .scaleY (1);

        playlistViewPositionAnimator.start ();
        playlistViewScaleAnimator.start ();
    }

    public void showPlaylistView () {
        if (mIsPlaylistViewShowing) return;

        mIsPlaylistViewShowing = true;
        mIsPlaylistViewPeeking = false;
        mIsPlaylistViewHidden = false;

        ViewPropertyAnimator playlistViewPositionAnimator = mPlaylistView.animate ()
                .x (mDragView.getWidth () - mPlaylistView.getWidth ())
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));

        ViewPropertyAnimator playlistViewScaleAnimator = mPlaylistView.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .scaleX (1.25f)
                .scaleY (1.25f);

        playlistViewPositionAnimator.start ();
        playlistViewScaleAnimator.start ();
    }

    public void hidePlaylistView () {
        if (mIsPlaylistViewHidden) return;

        mIsPlaylistViewShowing = false;
        mIsPlaylistViewPeeking = false;
        mIsPlaylistViewHidden = true;

        ViewPropertyAnimator playlistViewPositionAnimator = mPlaylistView.animate ()
                .x (mDragView.getWidth ())
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));

        ViewPropertyAnimator playlistViewScaleAnimator = mPlaylistView.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .scaleX (1)
                .scaleY (1);

        playlistViewPositionAnimator.start ();
        playlistViewScaleAnimator.start ();
    }

    @Deprecated
    public void peekPlaylistViewObject () {
        if (mIsPlaylistViewPeeking) return;

        mIsPlaylistViewShowing = false;
        mIsPlaylistViewPeeking = true;
        mIsPlaylistViewHidden = false;

        ValueAnimator xAnimator = ValueAnimator.ofFloat (mPlaylistView.getX (), mDragView.getWidth () - 100);
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setX ((Float) animation.getAnimatedValue ());
            }
        });

        ValueAnimator scaleAnimator = ValueAnimator.ofFloat (mPlaylistView.getScaleX (), 1f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setScaleX ((Float) animation.getAnimatedValue ());
                mPlaylistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }

    @Deprecated
    public void showPlaylistViewObject () {
        if (mIsPlaylistViewShowing) return;

        mIsPlaylistViewShowing = true;
        mIsPlaylistViewPeeking = false;
        mIsPlaylistViewHidden = false;

        ValueAnimator xAnimator = ValueAnimator.ofFloat (mPlaylistView.getX (), mDragView.getWidth () - mPlaylistView.getWidth ());
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setX ((Float) animation.getAnimatedValue ());
            }
        });

        ValueAnimator scaleAnimator = ValueAnimator.ofFloat (mPlaylistView.getScaleX (), 1.25f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setScaleX ((Float) animation.getAnimatedValue ());
                mPlaylistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }

    @Deprecated
    public void hidePlaylistViewObject () {
        if (mIsPlaylistViewHidden) return;

        mIsPlaylistViewShowing = false;
        mIsPlaylistViewPeeking = false;
        mIsPlaylistViewHidden = true;

        ValueAnimator xAnimator = ValueAnimator.ofFloat (mPlaylistView.getX (), mDragView.getWidth ());
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setX ((Float) animation.getAnimatedValue ());
            }
        });
        xAnimator.addListener (new AnimatorListenerAdapter () {
            @Override
            public void onAnimationEnd (Animator animation) {
                super.onAnimationEnd (animation);
            }
        });

        ValueAnimator scaleAnimator = ValueAnimator.ofFloat (mPlaylistView.getScaleX (), 1f);
        scaleAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        scaleAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                mPlaylistView.setScaleX ((Float) animation.getAnimatedValue ());
                mPlaylistView.setScaleY ((Float) animation.getAnimatedValue ());
            }
        });

        scaleAnimator.start ();
        xAnimator.start ();
    }
}