package com.pixel.spotify.spotify.adapter;

import androidx.annotation.NonNull;

import com.pixel.spotifyapi.SpotifyService;

import java.util.HashMap;
import java.util.Map;

public class SpotifyServiceAdapter {
    private static SpotifyService sInstance;

    public static void createInstance (SpotifyService instance) {
        sInstance = instance;
    }

    public static SpotifyService getInstance () {
        return sInstance;
    }

    private static void addTrack (@NonNull String user, @NonNull String playlist, @NonNull String track) {
        SpotifyService spotifyService = getInstance ();

        Map <String, Object> trackData = new HashMap <> ();
        trackData.put ("uris", track);

        Map <String, Object> trackData1 = new HashMap <> ();
        trackData1.put ("uris", track);

        spotifyService.addTracksToPlaylist (user, playlist, trackData1, trackData);
    }
}
