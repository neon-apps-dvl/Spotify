package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;
import android.util.Log;

import com.pixel.components.bitmap.BitmapTools;
import com.pixel.components.bitmap.SerializableBitmap;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotifyapi.Objects.Artist;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.Artists;
import com.pixel.spotifyapi.Objects.Image;
import com.pixel.spotifyapi.Objects.Track;
import com.pixel.spotifyapi.Objects.TrackSimple;
import com.pixel.spotifyapi.SpotifyService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrackModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public AlbumModel album;
    public long durationMs;
    public List <ArtistModel> artists;
    public List <Bitmap> thumbnails;

    public TrackModel () {
        artists = new ArrayList <> ();
        thumbnails = new ArrayList <> ();
    }

    public static TrackModel fromTrackBeta (SpotifyService spotifyService, Track track) {
        TrackModel trackModel = new TrackModel ();

        Log.d ("TrackModel", "making TrackModel from Track - " + track.name);

        for (Image thumbnail : track.album.images) {
            trackModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        String artistsRequest = "";

        for (ArtistSimple artistSimple : track.artists) {
            artistsRequest = artistsRequest + artistSimple.id + (track.artists.indexOf (artistSimple) != track.artists.size () - 1 ? "," : "");
        }

        Artists artists = spotifyService.getArtists (artistsRequest);

        for (Artist artist : artists.artists) {
            trackModel.artists.add (ArtistModel.fromArtist (artist));
        }

        trackModel.id = track.id;
        trackModel.uri = track.uri;
        trackModel.name = track.name;
        trackModel.album = AlbumModel.fromAlbum (spotifyService, track.album);
        trackModel.durationMs = track.duration_ms;

        return trackModel;
    }


    public static TrackModel fromTrack (SpotifyService spotifyService, Track track) {
        TrackModel trackModel = new TrackModel ();

        Log.d ("TrackModel", "making TrackModel from Track - " + track.name);

        for (Image thumbnail : track.album.images) {
            trackModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        for (ArtistSimple artistSimple : track.artists) {
            trackModel.artists.add (ArtistModel.fromArtist (spotifyService, artistSimple));
        }

        trackModel.id = track.id;
        trackModel.uri = track.uri;
        trackModel.name = track.name;
        trackModel.album = AlbumModel.fromAlbum (spotifyService, track.album);
        trackModel.durationMs = track.duration_ms;

        return trackModel;
    }

    public static void fromTrackBeta (SpotifyService spotifyService, Track track, SpotifyServiceManagerCallback callback) {
        Log.d ("TrackModel", "making async TrackModel from Track - " + track.name);

        new Thread (() -> {
            callback.onGetTrack (fromTrackBeta (spotifyService, track));
        }).start ();
    }

    public static void fromTrack (SpotifyService spotifyService, Track track, SpotifyServiceManagerCallback callback) {
        Log.d ("TrackModel", "making async TrackModel from Track - " + track.name);

        new Thread (() -> {
            callback.onGetTrack (fromTrack (spotifyService, track));
        }).start ();
    }

    public static TrackModel fromTrack (SpotifyService spotifyService, TrackSimple trackSimple) {
        Log.d ("TrackModel", "making TrackModel from TrackSimple - " + trackSimple.name);

        Track track = spotifyService.getTrack (trackSimple.id);

        return fromTrack (spotifyService, track);
    }

    public static void fromTrack (SpotifyService spotifyService, TrackSimple trackSimple, SpotifyServiceManagerCallback callback) {
        Log.d ("TrackModel", "making async TrackModel from TrackSimple - " + trackSimple.name);

        new Thread (() -> {
            callback.onGetTrack (fromTrack (spotifyService, trackSimple));
        }).start ();
    }

    public static TrackModel from (SerializableTrackModel serializableTrackModel) {
        TrackModel trackModel = new TrackModel ();

        for (SerializableBitmap serializableBitmap : serializableTrackModel.thumbnails) {
            trackModel.thumbnails.add (serializableBitmap.getBitmap ());
        }

        for (SerializableArtistModel serializableArtist : serializableTrackModel.artists) {
            trackModel.artists.add (ArtistModel.from (serializableArtist));
        }

        trackModel.id = serializableTrackModel.id;
        trackModel.uri = serializableTrackModel.uri;
        trackModel.name = serializableTrackModel.name;
        trackModel.album = AlbumModel.from (serializableTrackModel.album);
        trackModel.durationMs = serializableTrackModel.durationMs;

        return trackModel;
    }
}
