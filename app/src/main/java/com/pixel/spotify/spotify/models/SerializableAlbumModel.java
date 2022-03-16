package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import neon.pixel.components.bitmap.SerializableBitmap;

public class SerializableAlbumModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public String type;
    public List <ArtistModel> artists;
    public List <SerializableBitmap> thumbnails;
    public List <String> genres;

    public SerializableAlbumModel () {
        artists = new ArrayList <> ();
        thumbnails = new ArrayList <> ();
    }

    public static SerializableAlbumModel from (AlbumModel albumModel) {
        SerializableAlbumModel serializableAlbumModel = new SerializableAlbumModel ();

        for (Bitmap bitmap : albumModel.thumbnails) {
            serializableAlbumModel.thumbnails.add (SerializableBitmap.from (bitmap));
        }

        serializableAlbumModel.id = albumModel.id;
        serializableAlbumModel.uri = albumModel.uri;
        serializableAlbumModel.name = albumModel.name;
        serializableAlbumModel.type = albumModel.type;
        serializableAlbumModel.artists = albumModel.artists;
        serializableAlbumModel.genres = albumModel.genres;

        return serializableAlbumModel;
    }
}
