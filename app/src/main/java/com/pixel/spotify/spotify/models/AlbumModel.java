package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.BitmapTools;
import com.pixel.components.bitmap.SerializableBitmap;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotifyapi.Objects.Album;
import com.pixel.spotifyapi.Objects.AlbumSimple;
import com.pixel.spotifyapi.Objects.Artist;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.Artists;
import com.pixel.spotifyapi.Objects.Image;
import com.pixel.spotifyapi.SpotifyService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public String type;
    public List <ArtistModel> artists;
    public List <Bitmap> thumbnails;
    public List <String> genres;

    public AlbumModel () {
        artists = new ArrayList <> ();
        thumbnails = new ArrayList <> ();
    }

    public static AlbumModel fromAlbumBeta (SpotifyService spotifyService, Album album) {
        AlbumModel albumModel = new AlbumModel ();

        for (Image thumbnail : album.images) {
            albumModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        String artistsRequest = "";

        for (ArtistSimple artistSimple : album.artists) {
            artistsRequest = artistsRequest + artistSimple.id + (album.artists.indexOf (artistSimple) != album.artists.size () - 1 ? "," : "");
        }

        Artists artists = spotifyService.getArtists (artistsRequest);

        for (Artist artist : artists.artists) {
            albumModel.artists.add (ArtistModel.fromArtist (artist));
        }

        albumModel.id = album.id;
        albumModel.uri = album.uri;
        albumModel.name = album.name;
        albumModel.type = album.album_type;
        albumModel.genres = album.genres;

        return albumModel;
    }

    public static void fromAlbumBeta (SpotifyService spotifyService, Album album, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetAlbum (fromAlbumBeta (spotifyService, album));
        }).start ();
    }


    public static AlbumModel fromAlbum (SpotifyService spotifyService, Album album) {
        AlbumModel albumModel = new AlbumModel ();

        for (Image thumbnail : album.images) {
            albumModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        for (ArtistSimple artistSimple : album.artists) {
            albumModel.artists.add (ArtistModel.fromArtist (spotifyService, artistSimple));
        }

        albumModel.id = album.id;
        albumModel.uri = album.uri;
        albumModel.name = album.name;
        albumModel.type = album.album_type;
        albumModel.genres = album.genres;

        return albumModel;
    }

    public static void fromAlbum (SpotifyService spotifyService, Album album, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetAlbum (fromAlbum (spotifyService, album));
        }).start ();
    }

    public static AlbumModel fromAlbum (SpotifyService spotifyService, AlbumSimple albumSimple) {
        Album album = spotifyService.getAlbum (albumSimple.id);

        return fromAlbum (spotifyService, album);
    }

    public static void fromAlbum (SpotifyService spotifyService, AlbumSimple albumSimple, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetAlbum (fromAlbum (spotifyService, albumSimple));
        }).start ();
    }

    public static AlbumModel from (SerializableAlbumModel serializableAlbumModel) {
        AlbumModel albumModel = new AlbumModel ();

        for (SerializableBitmap serializableBitmap : serializableAlbumModel.thumbnails) {
            albumModel.thumbnails.add (serializableBitmap.getBitmap ());
        }

        albumModel.id = serializableAlbumModel.id;
        albumModel.uri = serializableAlbumModel.uri;
        albumModel.name = serializableAlbumModel.name;
        albumModel.type = serializableAlbumModel.type;
        albumModel.artists = serializableAlbumModel.artists;
        albumModel.genres = serializableAlbumModel.genres;

        return albumModel;
    }
}
