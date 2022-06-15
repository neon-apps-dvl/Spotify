package com.pixel.spotify.ui;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static neon.pixel.components.Components.getPx;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
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
import com.pixel.spotify.R;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.ui.color.ColorProfile;
import com.pixel.spotifyapi.Objects.Album;
import com.pixel.spotifyapi.Objects.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import neon.pixel.components.android.dynamictheme.DynamicTheme;
import neon.pixel.components.android.dynamictheme.OnThemeChangedListener;
import neon.pixel.components.android.theme.Theme;
import neon.pixel.components.bitmap.BitmapTools;
import retrofit.Callback;
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

    private Object mAlbumLock;

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
            mPinnedPlaylist = pinnedPlaylist;

            if (true) return; // cos i fucking cba to deal with this rn

            mPlaylistView.setImageBitmap (mPinnedPlaylist.thumbnail);
        });

        mContentViewModel.getTrack ().observe (requireActivity (), track -> {
            mTrack = track;

            Bitmap trackBitmap = mTrack.thumbnail;

            mTrackView.setImageBitmap (trackBitmap);

            float w = (float) mSurfaceView.getWidth () / (float) mSurfaceView.getHeight () * trackBitmap.getHeight ();
            float x = (trackBitmap.getWidth () - w) / 2;

            Bitmap surfaceBitmap = BitmapTools.from (trackBitmap, (int) x, 0, (int) w, trackBitmap.getHeight ());
            mSurfaceView.setImageBitmap (surfaceBitmap);

            //mMediaControlsView.setTrack (track);
            mMediaControlsView.nextTrack (track);

            DynamicTheme.getTheme (-1)
                    .setColor (ColorProfile.PRIMARY, Palette.from (trackBitmap).generate ()
                            .getDominantSwatch ()
                            .getRgb ());
            DynamicTheme.notifyThemeChanged (-1);
        });

        mContentViewModel.getNextTrack ().observe (requireActivity (), nextTrack -> {
            mNextTrack = nextTrack;

            mNextTrackView.setImageBitmap (mNextTrack.thumbnail);
        });
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

        mPlaylistView.setX (getView ().getWidth ());
        mPlaylistView.setY (getView ().getHeight () / 2 - mPlaylistView.getHeight () / 2);

        mMediaControlsView.addBottomSheetCallback (new BottomSheetBehavior.BottomSheetCallback () {
            final float x = getPx (getContext (), 48 - 64);
            final float y = getPx (getContext (), 48 + 12 - 8);

            final float SCALE = 0.5f;

            @Override
            public void onStateChanged (@NonNull View bottomSheet, int newState) {
                if (newState == STATE_EXPANDED && mAlbumLock == null) {
                    mAlbumLock = new Object ();

                    SpotifyServiceAdapter.getInstance ()
                            .getAlbum (mTrack.track.album.id, new Callback <Album> () {
                                @Override
                                public void success (Album album, Response response) {
                                    AlbumWrapper albumWrapper = new AlbumWrapper (album, mTrack.thumbnail);

                                    Log.e ("ALBUM", "album: " + album.name);

                                    mMediaControlsView.setAlbum (albumWrapper);
                                }

                                @Override
                                public void failure (RetrofitError error) {

                                }
                            });
                }
            }

            @Override
            public void onSlide (@NonNull View bottomSheet, float slideOffset) {
                int dy = (int) (getView ().getHeight () / 2 - mTrackView.getHeight () / 2 - mMediaControlsView.getY () + mTrackView.getHeight () * 1f);
                int sy = (int) (slideOffset * dy);
                int y = getView ().getHeight () / 2 - mTrackView.getHeight () / 2 - sy;

                mTrackView.setY (y);
                mTrackView.setScaleX (1 - slideOffset * 0.5f);
                mTrackView.setScaleY (1 - slideOffset * 0.5f);
            }
        });
    }

    @Override
    public void onPause () {
        super.onPause ();
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();

        mContentViewModel.getUser ().removeObservers (requireActivity ());
        mContentViewModel.getTrack ().removeObservers (requireActivity ());
        mContentViewModel.getNextTrack ().removeObservers (requireActivity ());
        mContentViewModel.getPlaylists ().removeObservers (requireActivity ());
        mContentViewModel.getPinnedPlaylist ().removeObservers (requireActivity ());

        mContentViewModel.setUser (mUser);
        mContentViewModel.setTrack (mTrack);
        mContentViewModel.setNextTrack (mNextTrack);
        mContentViewModel.setPlaylists (mPlaylists);
        mContentViewModel.setPinnedPlaylist (mPinnedPlaylist);
    }

    @Override
    public void onReturnNextTrack (String id) {
        SpotifyServiceAdapter.getInstance ()
                .getTrack (id, new retrofit.Callback <Track> () {
                    @Override
                    public void success (Track track, Response response) {
                        Executors.newSingleThreadExecutor ()
                                .execute (() -> {
                                    mAlbumLock = null;

                                    Bitmap nextTrackBitmap = BitmapTools.from (track.album.images.get (0).url);

                                    if (mNextTrack != null) {
                                        Log.e ("CF", "nextTrack track not null");

                                        mTrack = mNextTrack;
                                        mNextTrack = new TrackWrapper (track, nextTrackBitmap);

                                        Bitmap trackBitmap = mTrack.thumbnail;

                                        Log.e ("CF", "track: " + mTrack.track.name);
                                        Log.e ("CF", "nextTrack track: " + mNextTrack.track.name);

                                        // perform anim

                                        float w = (float) mSurfaceView.getWidth () / (float) mSurfaceView.getHeight () * trackBitmap.getHeight ();
                                        float x = (trackBitmap.getWidth () - w) / 2;

                                        Bitmap surfaceBitmap = BitmapTools.from (trackBitmap, (int) x, 0, (int) w, trackBitmap.getHeight ());

                                        int trackColorRgb = Palette.from (trackBitmap)
                                                .generate ()
                                                .getDominantSwatch ()
                                                .getRgb ();

                                        int trackColorArgb = Color.argb (128,
                                                Color.red (trackColorRgb),
                                                Color.green (trackColorRgb),
                                                Color.blue (trackColorRgb));

                                        Handler.createAsync (Looper.getMainLooper ())
                                                .post (() -> {
                                                    mTempSurfaceView.setImageBitmap (mSurfaceBitmap);
                                                    mTempSurfaceView.setVisibility (View.VISIBLE);

                                                    mTrackView.setImageBitmap (trackBitmap);
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

                                                    ValueAnimator argbAnimator = ValueAnimator.ofObject (new ArgbEvaluator (),
                                                            mSurfaceOverlayColor,
                                                            trackColorArgb);

                                                    argbAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
                                                    argbAnimator.addUpdateListener (animation -> {
                                                        mSurfaceOverlayColor = (int) animation.getAnimatedValue ();

                                                        mSurfaceViewOverlay.setBackgroundColor ((Integer) animation.getAnimatedValue ());
                                                    });

                                                    argbAnimator.addListener (new AnimatorListenerAdapter () {
                                                        @Override
                                                        public void onAnimationEnd (Animator animation) {
                                                            super.onAnimationEnd (animation);

                                                            // save state

                                                            mContentViewModel.setUser (mUser);
                                                            mContentViewModel.setTrack (mTrack);
                                                            mContentViewModel.setNextTrack (mNextTrack);
                                                            mContentViewModel.setPlaylists (mPlaylists);
                                                            mContentViewModel.setPinnedPlaylist (mPinnedPlaylist);
                                                        }
                                                    });

                                                    argbAnimator.start ();

                                                });
                                        // anim end

                                    } else {
                                        Log.e ("CF", "nextTrack track null, getting nextTrack");

                                        mNextTrack = new TrackWrapper (track, nextTrackBitmap);
                                        AppRepo.getInstance ()
                                                .getNextTrack ();
                                    }
                                });
                    }

                    @Override
                    public void failure (RetrofitError error) {
                    }
                });
    }

    @Override
    public void onPlaylistsChanged (List <String> playlists) {
        if (true) return;

        for (String playlist : playlists) {

        }

        mContentViewModel.setPlaylists (new ArrayList <> ());
    }

    @Override
    public void onPinnedPlaylistChanged (String pinnedPlaylist) {
        if (true) return;

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

        private float mPx;

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
                if (mXAnimator != null && mXAnimator.isRunning ()) mXAnimator.cancel ();
                if (mYAnimator != null && mYAnimator.isRunning ()) mYAnimator.cancel ();

                mX = v.getX ();
                mY = v.getY ();

                mTy = event.getRawY ();

                mDx = v.getX () - event.getRawX ();
                mDy = v.getY () - event.getRawY ();
            }

            if (event.getAction () == ACTION_MOVE) {
                mSy = (event.getRawY () - mTy) / mViewHeight * mMaxSy;

                mPx = mViewWidth - (v.getX () + v.getWidth () / 2 - mViewWidth / 2) / mViewWidth / 2 * getPx (getContext (), 256);

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

                mPlaylistView.setX (mPx);
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

                mPx = mViewWidth - (mX + v.getWidth () / 2 - mViewWidth / 2) / mViewWidth / 2 * getPx (getContext (), 256);

                v.setX (mX);
                mPlaylistView.setX (mPx);
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