package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.SerializableBitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializablePlaylistModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public int trackCount;
    public int followers;
    public boolean isPublic;
    public String description;
    public List <SerializableBitmap> thumbnails;

    public SerializablePlaylistModel () {
        thumbnails = new ArrayList <> ();
    }

    public static SerializablePlaylistModel from (PlaylistModel playlistModel) {
        SerializablePlaylistModel serializablePlaylistModel = new SerializablePlaylistModel ();

        for (Bitmap bitmap : playlistModel.thumbnails) {
            serializablePlaylistModel.thumbnails.add (SerializableBitmap.from (bitmap));
        }

        serializablePlaylistModel.id = playlistModel.id;
        serializablePlaylistModel.uri = playlistModel.uri;
        serializablePlaylistModel.name = playlistModel.name;
        serializablePlaylistModel.trackCount = playlistModel.trackCount;
        serializablePlaylistModel.followers = playlistModel.followers;
        serializablePlaylistModel.isPublic = playlistModel.isPublic;
        serializablePlaylistModel.description = playlistModel.description;

        return serializablePlaylistModel;
    }
}
