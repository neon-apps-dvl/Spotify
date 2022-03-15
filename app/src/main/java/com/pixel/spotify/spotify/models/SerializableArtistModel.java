package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.SerializableBitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializableArtistModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public int followers;
    public List <SerializableBitmap> thumbnails;

    public SerializableArtistModel () {
        thumbnails = new ArrayList <> ();
    }

    public static SerializableArtistModel from (ArtistModel artistModel) {
        SerializableArtistModel serializableArtistModel = new SerializableArtistModel ();

        for (Bitmap bitmap : artistModel.thumbnails) {
            serializableArtistModel.thumbnails.add (SerializableBitmap.from (bitmap));
        }

        serializableArtistModel.id = artistModel.id;
        serializableArtistModel.uri = artistModel.uri;
        serializableArtistModel.name = artistModel.name;
        serializableArtistModel.followers = artistModel.followers;

        return serializableArtistModel;
    }
}
