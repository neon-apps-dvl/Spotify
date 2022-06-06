package com.pixel.spotify.ui;

import android.graphics.Bitmap;

import com.pixel.spotifyapi.Objects.Playlist;

public class PlaylistWrapper {
    public Playlist playlist;
    public Bitmap thumbnail;

    public PlaylistWrapper () {
    }

    public PlaylistWrapper (Playlist playlist, Bitmap thumbnail) {
        this.playlist = playlist;
        this.thumbnail = thumbnail;
    }
}