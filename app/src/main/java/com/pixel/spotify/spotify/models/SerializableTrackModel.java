package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.SerializableBitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializableTrackModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public SerializableAlbumModel album;
    public long durationMs;
    public List <SerializableArtistModel> artists;
    public List <SerializableBitmap> thumbnails;

    public SerializableTrackModel () {
        artists = new ArrayList <> ();
        thumbnails = new ArrayList <> ();
    }

    public static SerializableTrackModel from (TrackModel trackModel) {
        SerializableTrackModel serializableTrackModel = new SerializableTrackModel ();

        for (Bitmap bitmap : trackModel.thumbnails) {
            serializableTrackModel.thumbnails.add (SerializableBitmap.from (bitmap));
        }

        for (ArtistModel artist : trackModel.artists) {
            serializableTrackModel.artists.add (SerializableArtistModel.from (artist));
        }

        serializableTrackModel.id = trackModel.id;
        serializableTrackModel.uri = trackModel.uri;
        serializableTrackModel.name = trackModel.name;
        serializableTrackModel.durationMs = trackModel.durationMs;

        return serializableTrackModel;
    }
}
