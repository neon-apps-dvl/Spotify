package com.pixel.spotify.ui;

import android.graphics.Bitmap;

import com.pixel.spotifyapi.Objects.Album;

public class AlbumWrapper {
    public Album album;
    public Bitmap thumbnail;

    public AlbumWrapper () {
    }

    public AlbumWrapper (Album album, Bitmap thumbnail) {
        this.album = album;
        this.thumbnail = thumbnail;
    }
}