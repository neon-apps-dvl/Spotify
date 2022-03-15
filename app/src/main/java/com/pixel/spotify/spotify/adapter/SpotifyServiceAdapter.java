package com.pixel.spotify.spotify.adapter;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotifyapi.Objects.Album;
import com.pixel.spotifyapi.Objects.PlaylistSimple;
import com.pixel.spotifyapi.Objects.PlaylistTrack;
import com.pixel.spotifyapi.Objects.Track;
import com.pixel.spotifyapi.Objects.TrackSimple;
import com.pixel.spotifyapi.Objects.UserPrivate;
import com.pixel.spotifyapi.SpotifyService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotifyServiceAdapter {
    public static void getTrack (@NonNull SpotifyService spotifyService, @NonNull String userId, int c, @NonNull SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetTrack (getTrack (spotifyService, userId, c));
        }).start ();
    }

    public static TrackModel getTrack (@NonNull SpotifyService spotifyService, @NonNull String userId, int c) {
        String tmmlp2 = "3IaAYtmN8T0YIYVqnxNnVz"; //"6DN7GcZF1HywzrkGN6Eeqk";
        String recovery = "5M6ARntRsBgfp9YINgRHrH"; //"1gUI4keDXbeSil6rwY9qUm";
        String tsslp = "59z8uxWZVFpL2LfZ5C9AzY"; //"10nO3EJJDMm6j6d2uK3Jah";
        String tmmlp = "1WopKLOG4Kud0H1r3xrCfT"; //"6t7956yu5zYf5A829XRiHC";
        String relapse = "6KEO499uUuvv65wAfOltod"; //"7MZzYkbHL9Tk3O6WeD4Z0Z";
        String mtbmb = "7ccTcabbJlCJiIqtrSSwBk"; //"4otkd9As6YaxxEkIjXPiZ6";
        String mtbmbsb = "1sv41rYgHhPWdyzwk5K9zy"; //"3MKvhQoFSrR2PrxXXBHe9B";

        String [] r = {
                recovery,
                tmmlp2,
                tsslp,
                tmmlp,
                relapse,
                mtbmb,
                mtbmbsb
        };

        Track track = spotifyService.getTrack (r[c]);
        Log.d ("MainActivity:DEBUG", track.name);

        return TrackModel.fromTrack (spotifyService, track);
    }

    public static void addTrack (@NonNull SpotifyService spotifyService, @NonNull String user, @NonNull String playlist, @NonNull String track, @NonNull SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            addTrack (spotifyService, user, playlist, track);
            callback.onPostTrack ();
        }).start ();
    }

    private static void addTrack (@NonNull SpotifyService spotifyService, @NonNull String user, @NonNull String playlist, @NonNull String track) {
        Map <String, Object> trackData = new HashMap <> ();
        trackData.put ("uris", track);

        Map <String, Object> trackData1 = new HashMap <> ();
        trackData1.put ("uris", track);

        spotifyService.addTracksToPlaylist (user, playlist, trackData1, trackData);
    }

    public static void getPlaylistTracks (@NonNull SpotifyService spotifyService, @NonNull String id, @NonNull SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetPlaylistTracks (getPlaylistTracks (spotifyService, id));
        }).start ();
    }

    private static List <TrackModel> getPlaylistTracks (@NonNull SpotifyService spotifyService, @NonNull String id) {
        UserPrivate currentUser = spotifyService.getCurrentUser ();
        Map <String, Bitmap> thumbnails = new HashMap <> ();

        List <TrackModel> tracks = new ArrayList <> ();
        List <PlaylistTrack> playlistTracks = spotifyService.getPlaylistTracks (currentUser.id, id).items;

        for (PlaylistTrack playlistTrack : playlistTracks) {
            tracks.add (TrackModel.fromTrack (spotifyService, playlistTrack.track));
        }

        return tracks;
    }

    public static void getUserPlaylists (@NonNull SpotifyService spotifyService, @NonNull UserModel user, @NonNull SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            long s = System.currentTimeMillis ();

            callback.onGetUserPlaylists (getUserPlaylists (spotifyService, user));

            long e = System.currentTimeMillis ();

            long t = e - s;

            Log.d ("ms", "playlists GET returned in " + t + "ms");
        }).start ();
    }

    public static List <PlaylistModel> getUserPlaylists (@NonNull SpotifyService spotifyService, @NonNull UserModel user) {
        List <PlaylistSimple> playlists = spotifyService.getCurrentUserPlaylists ().items;

        List <PlaylistModel> userPlaylists = new ArrayList <> ();

        for (int i = 0; i < playlists.size (); i = i + 1) {
            PlaylistSimple playlist = playlists.get (i);

            if (!playlist.owner.id.equals (user.id)) {
                playlists.remove (playlist);
            }
        }

        for (PlaylistSimple playlistSimple : playlists) {
            if (playlistSimple.owner.id.equals (user.id)) userPlaylists.add (PlaylistModel.fromPlaylist (spotifyService, playlistSimple));
        }

        return userPlaylists;
    }

    private static void getRecentlyPlayed (@NonNull SpotifyService spotifyService) {
        UserPrivate currentUser = spotifyService.getCurrentUser ();
    }

    public static List <TrackModel> getAlbumTracks (@NonNull SpotifyService spotifyService, @NonNull String id) {
        List <TrackModel> tracks = new ArrayList <> ();
        Album album = spotifyService.getAlbum (id);

        for (TrackSimple track : album.tracks.items) {
            tracks.add (TrackModel.fromTrack (spotifyService, track));
        }

        return tracks;
    }

    public static void getAlbumTracks (@NonNull SpotifyService spotifyService, @NonNull String id, @NonNull SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetAlbumTracks (getAlbumTracks (spotifyService, id));
        }).start ();
    }
}
