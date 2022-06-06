package com.pixel.spotify.ui;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SURFACE;
import static com.pixel.spotify.ui.color.Color.UI_THEME;
import static neon.pixel.components.Components.getPx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.palette.graphics.Palette;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.pixel.spotify.AppRepo;
import com.pixel.spotify.PlayQueue;
import com.pixel.spotify.R;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.ui.color.ColorProfile;
import com.pixel.spotifyapi.Objects.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import neon.pixel.components.android.dynamictheme.DynamicTheme;
import neon.pixel.components.android.dynamictheme.OnThemeChangedListener;
import neon.pixel.components.android.theme.Theme;
import neon.pixel.components.bitmap.BitmapTools;
import neon.pixel.components.color.Hct;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContentFragment extends Fragment implements OnThemeChangedListener, AppRepo.Listener {
    private String mUser;

    private List <PlaylistWrapper> mPlaylists;
    private PlaylistWrapper mPinnedPlaylist;
    private TrackWrapper mTrack;
    private TrackWrapper mNextTrack;

    private Bitmap mSurfaceBitmap;
    private int mSurfaceOverlayColor;

    private ContentViewModel mContentViewModel;

    private ImageView mSurfaceView;
    private ImageView mTempSurfaceView;
    private View mSurfaceViewOverlay;

    private ImageView mTrackView;
    private ImageView mNextTrackView;
    private ImageView mPlaylistView;
    private MediaControlsView mMediaControlsView;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        AppRepo.getInstance ()
                .registerListener (this);

        return inflater.inflate (R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);

        mSurfaceView = view.findViewById (R.id.surface_view);
        mTempSurfaceView = view.findViewById (R.id.temp_surface_view);
        mSurfaceViewOverlay = view.findViewById (R.id.surface_view_overlay);

        mTrackView = view.findViewById (R.id.track_view);
        mNextTrackView = view.findViewById (R.id.next_track_view);
        mPlaylistView = view.findViewById (R.id.playlist_view);

        mMediaControlsView = view.findViewById (R.id.media_controls_view);

        mSurfaceView.setRenderEffect (RenderEffect.createBlurEffect (100, 100, Shader.TileMode.CLAMP));

        ViewCompat.setOnApplyWindowInsetsListener (view, (v, windowInsetsCompat) -> {
            Insets insets = windowInsetsCompat.getInsets (WindowInsetsCompat.Type.systemBars ());

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mMediaControlsView.getLayoutParams ();
            marginLayoutParams.topMargin = insets.top + getPx (getContext (), 12 + 48 + 12 + 128 + 12);
            marginLayoutParams.bottomMargin = insets.bottom + getPx (getContext (), 24);

            mMediaControlsView.setLayoutParams (marginLayoutParams);

            return WindowInsetsCompat.CONSUMED;
        });

        mContentViewModel = new ViewModelProvider (requireActivity ()).get (ContentViewModel.class);

        mContentViewModel.getUser ().observe (requireActivity (), user -> {
            mUser = user;
        });

        mContentViewModel.getPlaylists ().observe (requireActivity (), playlists -> {
            mPlaylists = playlists;
        });

        mContentViewModel.getPinnedPlaylist ().observe (requireActivity (), pinnedPlaylist -> {

        });

        mContentViewModel.getTrack ().observe (requireActivity (), track -> {
            Bitmap trackBitmap = track.thumbnail;

            mTrackView.setImageBitmap (trackBitmap);

            float w = (float) mSurfaceView.getWidth () / (float) mSurfaceView.getHeight () * trackBitmap.getHeight ();
            float x = (trackBitmap.getWidth () - w) / 2;

            Bitmap surfaceBitmap = BitmapTools.from (trackBitmap, (int) x, 0, (int) w, trackBitmap.getHeight ());
            mTempSurfaceView.setImageBitmap (mSurfaceBitmap);
            mTempSurfaceView.setVisibility (View.VISIBLE);

            mSurfaceView.setImageBitmap (surfaceBitmap);

            mTempSurfaceView.animate ()
                    .alpha (0f)
                    .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                    .setListener (new AnimatorListenerAdapter () {
                        @Override
                        public void onAnimationEnd (Animator animation) {
                            super.onAnimationEnd (animation);

                            mTempSurfaceView.setVisibility (View.GONE);
                        }
                    })
                    .start ();
            mSurfaceBitmap = surfaceBitmap;

            int trackColorRgb = Palette.from (trackBitmap)
                    .generate ()
                    .getDominantSwatch ()
                    .getRgb ();

            int trackColorArgb = Color.argb (128,
                    Color.red (trackColorRgb),
                    Color.green (trackColorRgb),
                    Color.blue (trackColorRgb));

            ValueAnimator argbAnimator = ValueAnimator.ofObject (new ArgbEvaluator (),
                    mSurfaceOverlayColor,
                    trackColorArgb);

            argbAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            argbAnimator.addUpdateListener (animation -> {
                mSurfaceOverlayColor = (int) animation.getAnimatedValue ();

                mSurfaceViewOverlay.setBackgroundColor ((Integer) animation.getAnimatedValue ());
            });

            argbAnimator.setStartDelay (2000);
            argbAnimator.start ();
        });

//        Executors.newSingleThreadExecutor ().
//                execute (() -> getTrack (track -> {
//                    Handler.createAsync (Looper.getMainLooper ())
//                            .post (() -> mContentViewModel.setTrack (track));
//                }));
    }

    private TrackView createTrackView (Map <Track, Bitmap> track) {
        TrackView trackView = new TrackView (getContext (), getView ().getWidth () / 2, getView ().getHeight () / 2);
        trackView.setTrack (track);
        trackView.setInteractionListener (new TrackView.InteractionListener () {
            final float PEEK_BOUND = getView ().getWidth () * 0.5f;

            boolean pInPeek = false;
            boolean nowInPeek = false;

            boolean isPeeking = false;
            boolean isState = false;

            @Override
            public void onPositionChanged (float x, float y, boolean down) {
                pInPeek = nowInPeek;

                if (x >= PEEK_BOUND) {
                    nowInPeek = true;
                } else {
                    nowInPeek = false;
                }

                if (nowInPeek && !pInPeek) isPeeking = true;
                else isPeeking = false;
                if (!nowInPeek && pInPeek) isState = true;
                else isState = false;

                if (isPeeking) {
                    mTrackView.animate ()
                            .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                            .scaleX (0.5f)
                            .scaleY (0.5f)
                            .start ();

                    mPlaylistView.animate ()
                            .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                            .x (getView ().getWidth () - mPlaylistView.getWidth () - 100)
                            .scaleX (1.25f)
                            .scaleY (1.25f)
                            .start ();
                }

                if (isState) {
                    mTrackView.animate ()
                            .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                            .scaleX (1f)
                            .scaleY (1f)
                            .start ();

                    mPlaylistView.animate ()
                            .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                            .x (getView ().getWidth () - 100)
                            .scaleX (1f)
                            .scaleY (1f)
                            .start ();
                }
            }
        });

        return trackView;
    }

    private void setTrackView (TrackView trackView) {
    }

    private void setPlaylistView (Bitmap bitmap) {
    }

    private void updateTheme (Bitmap bitmap) {
        int baseColor = Palette.from (bitmap)
                .generate ()
                .getDominantSwatch ()
                .getRgb ();

        Hct hct = Hct.fromInt (baseColor);
        hct.setTone (PRIMARY);
        int colorPrimary = hct.toInt ();

        hct.setTone (SECONDARY);
        int colorSecondary = hct.toInt ();

        hct.setTone (SURFACE);
        int colorSurface = hct.toInt ();

        Theme theme = DynamicTheme.getTheme (UI_THEME);
        theme.setColor (ColorProfile.PRIMARY, colorPrimary);
        theme.setColor (ColorProfile.SECONDARY, colorSecondary);
        theme.setColor (ColorProfile.SURFACE, colorSurface);

        mMediaControlsView.setColor (baseColor);

        DynamicTheme.notifyThemeChanged (UI_THEME);
    }

    private void setSurface (Bitmap bitmap) {
        float w = (float) mSurfaceView.getWidth () / (float) mSurfaceView.getHeight () * bitmap.getHeight ();
        float x = (bitmap.getWidth () - w) / 2;

        Bitmap resizedBitmap = BitmapTools.from (bitmap, (int) x, 0, (int) w, bitmap.getHeight ());

        mTempSurfaceView.setVisibility (View.VISIBLE);
        mTempSurfaceView.setAlpha (1f);
        mTempSurfaceView.setBackground (mSurfaceView.getBackground ());
        mTempSurfaceView.setRenderEffect (RenderEffect.createBlurEffect (mSurfaceView.getHeight () / 8, mSurfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        mSurfaceView.setBackground (new BitmapDrawable (getResources (), resizedBitmap));
        mSurfaceView.setRenderEffect (RenderEffect.createBlurEffect (mSurfaceView.getHeight () / 8, mSurfaceView.getHeight () / 8, Shader.TileMode.CLAMP));

        mTempSurfaceView.animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .alpha (0f)
                .start ();

        ValueAnimator valueAnimator = ValueAnimator.ofObject (new ArgbEvaluator (),
                ((ColorDrawable) mSurfaceViewOverlay.getBackground ()).getColor (),
                DynamicTheme.getTheme (UI_THEME)
                        .getColor (ColorProfile.SURFACE));

        valueAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        valueAnimator.addUpdateListener (animation -> {
            int c = (Integer) animation.getAnimatedValue ();

            int cl = Color.argb (
                    128,
                    Color.red (c),
                    Color.green (c),
                    Color.blue (c)
            );

            mSurfaceViewOverlay.setBackgroundColor (cl);
        });

        valueAnimator.start ();
    }

    @Override
    public void onResume () {
        super.onResume ();

        int viewWidth = getView ().getWidth ();
        int viewHeight = getView ().getHeight ();

        mTrackView.setX (viewWidth / 2 - mTrackView.getWidth () / 2);
        mTrackView.setY (viewHeight / 2 - mTrackView.getHeight () / 2);
        mTrackView.setOnTouchListener (new OnTouchListener (viewWidth, viewHeight));

        mNextTrackView.setX (viewWidth / 2 - mTrackView.getWidth () / 2);
        mNextTrackView.setY (viewHeight / 2 - mTrackView.getHeight () / 2);
        mNextTrackView.setVisibility (View.GONE);

        mPlaylistView.setX (getView ().getWidth () - 100);
        mPlaylistView.setY (getView ().getHeight () / 2 - mPlaylistView.getHeight () / 2);

        mMediaControlsView.addBottomSheetCallback (new BottomSheetBehavior.BottomSheetCallback () {
            final float x = getPx (getContext (), 48 - 64);
            final float y = getPx (getContext (), 48 + 12 - 8);

            final float SCALE = 0.5f;

            @Override
            public void onStateChanged (@NonNull View bottomSheet, int newState) {
                if (newState == STATE_COLLAPSED) {

                }
            }

            @Override
            public void onSlide (@NonNull View bottomSheet, float slideOffset) {
                float anchorX = getView ().getWidth () / 2 - mTrackView.getWidth () / 2;
                float anchorY = getView ().getHeight () / 2 - mTrackView.getHeight () / 2;

                float nX = x + (anchorX - x) * (1 - slideOffset);
                float nY = y + (anchorY - y) * (1 - slideOffset);

                float s = 1 - slideOffset * SCALE;

                Log.e ("S", "o: " + nX);

                mTrackView.setX (nX);
                mTrackView.setY (nY);
                mTrackView.setScaleX (s);
                mTrackView.setScaleY (s);
            }
        });

//        mMediaControlsView.setColor (Color.RED);
    }

    @Override
    public void onPause () {
        super.onPause ();
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();
    }

    @Override
    public void onReturnNextTrack (String id) {
        SpotifyServiceAdapter.getInstance ()
                .getTrack (id, new retrofit.Callback <Track> () {
                    @Override
                    public void success (Track track, Response response) {
                        Executors.newSingleThreadExecutor ()
                                .execute (() -> {
                                    Bitmap trackThumbnail = BitmapTools.from (track.album.images.get (0).url);

                                    mNextTrack = new TrackWrapper (track, trackThumbnail);

                                    if (mNextTrack != null) {
                                        mTrack = mNextTrack;
                                        mNextTrack = new TrackWrapper (track, trackThumbnail);
                                    }

                                    Handler.createAsync (Looper.getMainLooper ())
                                                    .post (() -> mContentViewModel.setTrack (mTrack));
                                });
                    }

                    @Override
                    public void failure (RetrofitError error) {

                    }
                });
    }

    @Override
    public void onPlaylistsChanged (List <String> playlists) {
        for (String playlist : playlists) {

        }

        mContentViewModel.setPlaylists (new ArrayList <> ());
    }

    @Override
    public void onPinnedPlaylistChanged (String pinnedPlaylist) {
        mContentViewModel.setPinnedPlaylist (new PlaylistWrapper ());
    }

    @Override
    public void onThemeChangedListenerAdded (int id) {

    }

    @Override
    public void onThemeChangedListenerRemoved (int id) {

    }

    @Override
    public void onThemeChanged (int id, Theme theme) {

    }

    private void getTrack (Callback c) {
        PlayQueue playQueue = PlayQueue.getInstance ();

        while (playQueue.peek () == null) {

        }

        c.onGet (playQueue.pop ());
    }

    interface Callback {
        void onGet (Map <Track, Bitmap> track);
    }

    private class OnTouchListener implements View.OnTouchListener {
        private static final String TAG = "OTL";

        private final int mViewWidth;
        private final int mViewHeight;
        private final int mPeekBound;
        private final int mDismissBound;

        private final int mMaxSx;
        private final int mMaxSy;
        private final int mMaxI;

        private float mX;
        private float mY;

        private float mTy;

        private float mDx;
        private float mDy;

        private float mSx;
        private float mSy;

        private float mIx;
        private float mIy;
        private Object mIdleLock;
        private Object mPeekLock = new Object ();
        private Object mDismissLock = new Object ();

        private ValueAnimator mRxAnimator;
        private ValueAnimator mIxAnimator;

        private ValueAnimator mXAnimator;
        private ValueAnimator mYAnimator;

        public OnTouchListener (int viewWidth, int viewHeight) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;

            mPeekBound = mViewWidth - (viewWidth - getPx (getContext (), 256)) / 2;//(int) (0.75f * viewWidth);
            mDismissBound = (viewWidth - getPx (getContext (), 256)) / 2;

            mMaxSx = getPx (getContext (), 64);
            mMaxSy = getPx (getContext (), 256);
            mMaxI = mViewWidth - getPx (getContext (), 384);
        }

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            if (event.getAction () == ACTION_DOWN) {
                mX = v.getX ();
                mY = v.getY ();

                mTy = event.getRawY ();

                mDx = v.getX () - event.getRawX ();
                mDy = v.getY () - event.getRawY ();
            }

            if (event.getAction () == ACTION_MOVE) {
                mSy = (event.getRawY () - mTy) / mViewHeight * mMaxSy;

                if (event.getRawX () + mDx + v.getWidth () / 2 > mDismissBound && event.getRawX () + mDx + v.getWidth () / 2 < mPeekBound) {
                    if (mIdleLock == null) {
                        // onIdle
                        mIdleLock = new Object (); // lock peek
                        mPeekLock = null; // unlock peek
                        mDismissLock = null;

                        inflateX (0);
                        resize (v, 1f);
                    }

                    mX = event.getRawX () + mDx + mIx;
                }

                if (event.getRawX () + mDx + v.getWidth () / 2 >= mPeekBound) {
                    mSx = (event.getRawX () + mDx + v.getWidth () / 2 - mPeekBound) / mViewWidth * mMaxSx;

                    if (mPeekLock == null) {
                        // onPeek
                        mPeekLock = new Object (); // lock peek
                        mIdleLock = null;
                        mDismissLock = null;

                        inflateX (mMaxI);
                        resize (v, 0.5f);
                    }

                    mX = mPeekBound - v.getWidth () / 2 + mIx + mSx;
                }

                if (event.getRawX () + mDx + v.getWidth () / 2 <= mDismissBound) {
                    mSx = (event.getRawX () + mDx + v.getWidth () / 2 - mDismissBound) / mViewWidth * mMaxSx;

                    if (mDismissLock == null) {
                        // onDismiss

                        mDismissLock = new Object ();
                        mIdleLock = null;
                        mPeekLock = null;

                        inflateX (-mMaxI);
                        resize (v, 0.5f);
                    }
                    mX = mDismissBound - v.getWidth () / 2 + mIx;
                }

                v.setX (mX);
                mY = mViewHeight / 2 - v.getHeight () / 2 + mSy;
                v.setY (mY);
            }
            if (event.getAction () == ACTION_UP) {
                reset (v);
                resize (v, 1f);
            }

            return true;
        }

        private void reset (View v) {
            int newX = mViewWidth / 2 - v.getWidth () / 2;
            int newY = mViewHeight / 2 - v.getHeight () / 2;

            if (mXAnimator != null && mXAnimator.isRunning ()) mXAnimator.cancel ();
            mXAnimator = ValueAnimator.ofFloat (mX, newX);
            mXAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            mXAnimator.addUpdateListener (animation -> {
                mX = (float) animation.getAnimatedValue ();
                v.setX (mX);
            });

            if (mYAnimator != null && mYAnimator.isRunning ()) mYAnimator.cancel ();
            mYAnimator = ValueAnimator.ofFloat (mY, newY);
            mYAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            mYAnimator.addUpdateListener (animation -> {
                mY = (float) animation.getAnimatedValue ();
                v.setY (mY);
            });

            mXAnimator.start ();
            mYAnimator.start ();
        }

        private void resize (View v, float newR) {
            if (mRxAnimator != null && mRxAnimator.isRunning ()) mRxAnimator.cancel ();
            mRxAnimator = ValueAnimator.ofFloat (v.getScaleX (), newR);
            mRxAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            mRxAnimator.addUpdateListener (animation -> {
                float r = (float) animation.getAnimatedValue ();
                v.setScaleX (r);
                v.setScaleY (r);
            });
            mRxAnimator.start ();
        }

        private void inflateX (float newIx) {
            if (mIxAnimator != null && mIxAnimator.isRunning ()) mIxAnimator.cancel ();
            mIxAnimator = ValueAnimator.ofFloat (mIx, newIx);
            mIxAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            mIxAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
                @Override
                public void onAnimationUpdate (ValueAnimator animation) {
                    mIx = (float) animation.getAnimatedValue ();
                }
            });
            mIxAnimator.start ();
        }
    }
}