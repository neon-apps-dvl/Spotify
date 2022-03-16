package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import neon.pixel.components.bitmap.BitmapTools;
import neon.pixel.components.bitmap.SerializableBitmap;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotifyapi.Objects.Artist;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.Image;
import com.pixel.spotifyapi.SpotifyService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArtistModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public int followers;
    public List <Bitmap> thumbnails;

    public ArtistModel () {
        thumbnails = new ArrayList <> ();
    }

    public static ArtistModel fromArtist (Artist artist) {
        ArtistModel artistModel = new ArtistModel ();

        for (Image thumbnail : artist.images) {
            artistModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        artistModel.id = artist.id;
        artistModel.uri = artist.uri;
        artistModel.name = artist.name;
        artistModel.followers = artist.followers.total;

        return artistModel;
    }

    public static void fromArtist (Artist artist, SpotifyServiceManagerCallback callback) {
        ArtistModel artistModel = new ArtistModel ();

        new Thread (() -> {
            callback.onGetArtist (fromArtist (artist));
        }).start ();
    }

    public static ArtistModel fromArtist (SpotifyService spotifyService, ArtistSimple artistSimple) {
        Artist artist = spotifyService.getArtist (artistSimple.id);

        return fromArtist (artist);
    }

    public static void fromArtist (SpotifyService spotifyService, ArtistSimple artistSimple, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetArtist (fromArtist (spotifyService, artistSimple));
        }).start ();
    }

    public static ArtistModel from (SerializableArtistModel serializableArtistModel) {
        ArtistModel artistModel = new ArtistModel ();

        for (SerializableBitmap serializableBitmap : serializableArtistModel.thumbnails) {
            artistModel.thumbnails.add (serializableBitmap.getBitmap ());
        }

        artistModel.id = serializableArtistModel.id;
        artistModel.uri = serializableArtistModel.uri;
        artistModel.name = serializableArtistModel.name;
        artistModel.followers = serializableArtistModel.followers;

        return artistModel;
    }
}
