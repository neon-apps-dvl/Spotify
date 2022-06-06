package com.pixel.spotify;

import static com.pixel.spotify.ui.color.Color.UI_THEME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.ui.ContentFragment;
import com.pixel.spotify.ui.ContentViewModel;
import com.pixel.spotify.ui.MainBackdrop;
import com.pixel.spotify.ui.UiState;
import com.pixel.spotify.ui.color.ColorProfile;
import com.pixel.spotifyapi.Objects.Track;
import com.pixel.spotifyapi.Objects.Tracks;
import com.pixel.spotifyapi.Objects.UserPrivate;
import com.pixel.spotifyapi.SpotifyApi;
import com.pixel.spotifyapi.SpotifyCallback;
import com.pixel.spotifyapi.SpotifyError;
import com.pixel.spotifyapi.SpotifyService;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import neon.pixel.components.android.dynamictheme.DynamicTheme;
import neon.pixel.components.bitmap.BitmapTools;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements OnUiStateChangedListener {
    private static final String TAG = "MainActivity";
    private static final String DEBUG = "MainActivity:DEBUG";

    private static final String CLIENT_ID = "86ef10fd39344e10a060ade09f7c7a78";

    private static final String SPOTIFY_PREMIUM = "premium";
    private static final String SPOTIFY = "com.spotify.music";

    private static final int REQUEST_AUTH = 0;
    private SpotifyService mSpotifyService;
    private SpotifyAppRemote mSpotifyAppRemote;

    private ContentViewModel mViewModel;

    private MainBackdrop mMainBackdrop;
    private FragmentContainerView mFragmentContainerView;

    private ContentFragment mContentFragment;

    @SuppressLint ("ClickableViewAccessibility")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        createDynamicTheme ();

        WindowCompat.setDecorFitsSystemWindows (getWindow (), false);

        setContentView (R.layout.activity_main);

        mViewModel = new ViewModelProvider (this).get (ContentViewModel.class);

        mMainBackdrop = findViewById (R.id.main_backdrop);
        mMainBackdrop.setOnStateChangedListener (isOpen -> {
            if (isOpen) ;
            else ;
        });

        DynamicTheme.addOnThemeChangedListener (UI_THEME, mMainBackdrop);

        mFragmentContainerView = new FragmentContainerView (this);
        mFragmentContainerView.setLayoutParams (new CoordinatorLayout.LayoutParams (-1, -1));
        mFragmentContainerView.setId (View.generateViewId ());

        mContentFragment = new ContentFragment ();

        getSupportFragmentManager ().beginTransaction ()
                .add (mFragmentContainerView.getId (), mContentFragment)
                .commit ();

        mMainBackdrop.setFrontView (mFragmentContainerView);

        if (checkSpotifyInstalled ()) {
            auth ();
        }
    }

    @Override
    protected void onResume () {
        super.onResume ();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
        super.onActivityResult (requestCode, resultCode, intent);

        if (requestCode == REQUEST_AUTH) {
            AuthorizationResponse response = AuthorizationClient.getResponse (resultCode, intent);

            switch (response.getType ()) {
                case TOKEN:
                    String accessToken = response.getAccessToken ();

                    SpotifyApi spotifyApi = new SpotifyApi ();
                    spotifyApi.setAccessToken (accessToken);
                    mSpotifyService = spotifyApi.getService ();

                    SpotifyServiceAdapter.createInstance (mSpotifyService);

                    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder (ContentWorker.class)
                            .build ();

                    WorkManager.getInstance (this)
                            .enqueue (workRequest);

                    String track = "6KEO499uUuvv65wAfOltod";
                            //"7h2OEj0ifXb3UdgvTmCqfY";

                    SpotifyServiceAdapter.getInstance ()
                            .getTracks (track, new SpotifyCallback <Tracks> () {
                                @Override
                                public void failure (SpotifyError spotifyError) {

                                }

                                @Override
                                public void success (Tracks tracks, Response response) {
                                    Executor executor = Executors.newSingleThreadExecutor ();

                                    executor.execute (() -> {
                                        Track track = tracks.tracks.get (0);
                                        Bitmap bitmap = BitmapTools.from (track.album.images.get (0).url);

                                        AppRepo.getInstance ()
                                                        .getNextTrack ();

                                        PlayQueue.getInstance ()
                                                .push (Collections.singletonMap (track, bitmap));
                                    });
                                }
                            });

                    break;

                case ERROR:
                    break;

                default:
            }
        }
    }

    private void createDynamicTheme () {
        DynamicTheme.newInstance (UI_THEME, ColorProfile.class);
    }

    public void auth () {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder (CLIENT_ID, AuthorizationResponse.Type.TOKEN, "http://localhost");

        builder.setScopes (new String[] {"streaming", "user-follow-read", "playlist-read-private", "user-read-private", "user-library-modify", "playlist-modify-private", "playlist-modify-public"});
        AuthorizationRequest request = builder.build ();

        AuthorizationClient.openLoginActivity (this, REQUEST_AUTH, request);

        ConnectionParams connectionParams = new ConnectionParams.Builder (CLIENT_ID)
                .setRedirectUri ("http://localhost")
                .build ();

        SpotifyAppRemote.connect (MainActivity.this, connectionParams, new Connector.ConnectionListener () {
            @Override
            public void onConnected (SpotifyAppRemote spotifyAppRemote) {
                Log.d (TAG, "SpotifyAppRemote connected");
            }

            @Override
            public void onFailure (Throwable error) {
                Log.d (TAG, "SpotifyAppRemote failed to connect - " + error.getMessage ());
            }
        });
    }

    private boolean checkSpotifyInstalled () {
        try {
            getPackageManager ().getPackageInfo (SPOTIFY, 0);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    private boolean isPremium (UserPrivate currentUser) {
        return currentUser.product.equals (SPOTIFY_PREMIUM);
    }

    @Override
    public void onUiStateChanged (UiState uiState) {
        switch (uiState) {
            case VISIBLE:
                mMainBackdrop.showUi ();
                break;

            case HIDDEN:
                mMainBackdrop.hideUi ();
                break;
        }
    }
}