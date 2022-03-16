package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import neon.pixel.components.bitmap.BitmapTools;
import neon.pixel.components.bitmap.SerializableBitmap;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotifyapi.Objects.Image;
import com.pixel.spotifyapi.Objects.Playlist;
import com.pixel.spotifyapi.Objects.PlaylistSimple;
import com.pixel.spotifyapi.SpotifyService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlaylistModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public int trackCount;
    public int followers;
    public boolean isPublic;
    public String description;
    public List <Bitmap> thumbnails;

    public PlaylistModel () {
        thumbnails = new ArrayList <> ();
    }

    public static PlaylistModel fromPlaylist (Playlist playlist) {
        PlaylistModel playlistModel = new PlaylistModel ();

        for (Image thumbnail : playlist.images) {
            playlistModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        playlistModel.id = playlist.id;
        playlistModel.uri = playlist.uri;
        playlistModel.name = playlist.name;
        playlistModel.trackCount = playlist.tracks.total;
        playlistModel.description = playlist.description;
        playlistModel.followers = playlist.followers.total;
        playlistModel.isPublic = playlist.is_public;

        return playlistModel;
    }

    public static void fromPlaylist (Playlist playlist, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetPlaylist (fromPlaylist (playlist));
        }).start ();
    }

    public static PlaylistModel fromPlaylist (SpotifyService spotifyService, PlaylistSimple playlistSimple) {
        Playlist playlist = spotifyService.getPlaylist (playlistSimple.owner.id, playlistSimple.id);

        return fromPlaylist (playlist);
    }

    public static void fromPlaylist (SpotifyService spotifyService, PlaylistSimple playlistSimple, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetPlaylist (fromPlaylist (spotifyService, playlistSimple));
        }).start ();
    }

    public static PlaylistModel from (SerializablePlaylistModel serializablePlaylistModel) {
        PlaylistModel playlistModel = new PlaylistModel ();

        for (SerializableBitmap serializableBitmap : serializablePlaylistModel.thumbnails) {
            playlistModel.thumbnails.add (serializableBitmap.getBitmap ());
        }

        playlistModel.id = serializablePlaylistModel.id;
        playlistModel.uri = serializablePlaylistModel.uri;
        playlistModel.name = serializablePlaylistModel.name;
        playlistModel.trackCount = serializablePlaylistModel.trackCount;
        playlistModel.followers = serializablePlaylistModel.followers;
        playlistModel.isPublic = serializablePlaylistModel.isPublic;
        playlistModel.description = serializablePlaylistModel.description;

        return playlistModel;
    }
}
