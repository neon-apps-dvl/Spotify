package com.pixel.spotify;

import static com.pixel.spotify.ui.color.Color.UI_THEME;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.Playlists;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotify.ui.MainBackdrop;
import com.pixel.spotify.ui.UiState;
import com.pixel.spotify.ui.color.ColorProfile;
import com.pixel.spotify.ui.mainfragment.MainFragment;
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

import java.util.List;

import neon.pixel.components.android.dynamictheme.DynamicTheme;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements OnUiStateChangedListener {
    private static final String TAG = "MainActivity";
    private static final String DEBUG = "MainActivity:DEBUG";

    private static final String CLIENT_ID = "86ef10fd39344e10a060ade09f7c7a78";

    private static final String SELECTED_PLAYLIST = "selected";
    private static final String SPOTIFY_PREMIUM = "premium";
    private static final String SPOTIFY = "com.spotify.music";

    private static final int REQUEST_AUTH = 0;
    private SpotifyApi spotifyApi;
    private SpotifyService spotifyService;
    private SpotifyAppRemote spotifyAppRemote;

    private UserModel user;
    private List <PlaylistModel> playlists;

    private ConstraintLayout mainView;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private MainFragment mainFragment;
    private MainBackdrop mMainBackdrop;

    private String accessToken;

    private Object trackLock;
    private Object playlistsLock;

    boolean shouldAllowTouch = true;

    @SuppressLint ("ClickableViewAccessibility")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        createDynamicTheme ();

        setContentView (R.layout.activity_main);
        mainView = findViewById (R.id.main_view);

        mMainBackdrop = findViewById (R.id.main_backdrop);
        mMainBackdrop.setOnStateChangedListener (isOpen -> {
            if (isOpen) mainFragment.setEnabled (false);
            else mainFragment.setEnabled (true);
        });

        trackLock = new Object ();
        playlistsLock = new Object ();

        mainView.getViewTreeObserver ().addOnPreDrawListener (new ViewTreeObserver.OnPreDrawListener () {
            @Override
            public boolean onPreDraw () {
                if (trackLock == null && playlistsLock == null) {
                    mainView.getViewTreeObserver ().removeOnPreDrawListener (this);
                    return true;
                } else return false;
            }
        });

        WindowCompat.setDecorFitsSystemWindows (getWindow (), false);

        CoordinatorLayout fragContainer = new CoordinatorLayout (this);
        fragContainer.setLayoutParams (new CoordinatorLayout.LayoutParams (-1, -1));
        fragContainer.setId (View.generateViewId ());

        mainFragment = new MainFragment (spotifyService);
        mainFragment.setUiStateChangedListener (this);

        fragmentManager = getSupportFragmentManager ();
        fragmentTransaction = fragmentManager.beginTransaction ();
        fragmentTransaction.add (fragContainer.getId (), mainFragment)
                .commit ();

        mMainBackdrop.setFrontView (fragContainer);
        
        if (checkSpotifyInstalled ()) {
            auth ();
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (event.getAction () == MotionEvent.ACTION_UP) {

        }

        return super.onTouchEvent (event);
    }

    @Override
    protected void onResume () {
        super.onResume ();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed ();
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
        super.onActivityResult (requestCode, resultCode, intent);

        if (requestCode == REQUEST_AUTH) {
            AuthorizationResponse response = AuthorizationClient.getResponse (resultCode, intent);

            switch (response.getType ()) {
                case TOKEN:
                    accessToken = response.getAccessToken ();

                    spotifyApi = new SpotifyApi ();
                    spotifyApi.setAccessToken (accessToken);
                    spotifyService = spotifyApi.getService ();

                    mainFragment.setSpotifyService (spotifyService);

                    spotifyService.getCurrentUser (new SpotifyCallback <UserPrivate> () {
                        @Override
                        public void failure (SpotifyError spotifyError) {
                            Log.e (TAG, spotifyError.getMessage ());
                        }

                        @Override
                        public void success (UserPrivate userPrivate, Response response) {
                            if (isPremium (userPrivate)) {
                                UserModel.from (userPrivate, new SpotifyServiceManagerCallback () {
                                    @Override
                                    public void onGetUser (UserModel userModel) {
                                        user = userModel;

                                        mainFragment.setUser (user);

                                        SharedPreferences prefs = getSharedPreferences (user.id, MODE_PRIVATE);
                                        String selectedId = prefs.getString (SELECTED_PLAYLIST, null);

                                        SpotifyServiceAdapter.getTrack (spotifyService, user.id, 0, new SpotifyServiceManagerCallback () {
                                            @Override
                                            public void onGetTrack (TrackModel trackModel) {
                                                super.onGetTrack (trackModel);

                                                runOnUiThread (() -> {
                                                    mainFragment.setTrack (trackModel);

                                                    trackLock = null;
                                                });
                                            }
                                        });

                                        SpotifyServiceAdapter.getUserPlaylists (spotifyService, user, new SpotifyServiceManagerCallback () {
                                            int selected = 0;

                                            @Override
                                            public void onGetUserPlaylists (List <PlaylistModel> playlists) {
                                                MainActivity.this.playlists = playlists;

                                                Playlists p = new Playlists ();

                                                for (PlaylistModel playlist : playlists) {
                                                    p.add (playlist);

                                                    if (playlist.id.equals (selectedId)) {
                                                        selected = playlists.indexOf (playlist);
                                                    }
                                                }

                                                runOnUiThread (() -> {
                                                    mainFragment.setPlaylists (selected, p);

                                                    mainFragment.updateTheme ();
                                                });

                                                playlistsLock = null;
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });

                    break;

                case ERROR:
                    break;

                default:
            }
        }
    }

    public void createDynamicTheme () {
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