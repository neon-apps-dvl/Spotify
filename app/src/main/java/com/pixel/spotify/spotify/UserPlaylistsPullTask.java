package com.pixel.spotify.spotify;

import com.pixel.spotify.Task;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotifyapi.SpotifyService;

import java.util.List;

public class UserPlaylistsPullTask extends Task {
    private static final long PERIOD = 60 * 5000l;

    private static UserPlaylistsPullTask sInstance = new UserPlaylistsPullTask ();

    private UserPlaylistsPullTaskCallback mCallback;

    private SpotifyService mSpotifyService;
    private UserModel mUser;

    public static UserPlaylistsPullTask getInstance (SpotifyService spotifyService, UserModel user, UserPlaylistsPullTaskCallback callback) {
        sInstance.mSpotifyService = spotifyService;
        sInstance.mUser = user;
        sInstance.mCallback = callback;

        sInstance.stop ();

        return sInstance;
    }

    public void startPeriodic () {
        startPeriodic (PERIOD);
    }

    public void doOneTime () {
        startOneTime ();
    }

    public void pull () {
        stop ();
        startPeriodic ();
    }

    @Override
    public void doWork () {
        List <PlaylistModel> playlists = SpotifyServiceAdapter.getUserPlaylists (mSpotifyService, mUser);

        mCallback.onComplete (playlists);
    }
}
