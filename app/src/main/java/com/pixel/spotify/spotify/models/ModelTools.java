package com.pixel.spotify.spotify.models;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ModelTools {
    public static UserModel getUserModel (File object) {
        UserModel userModel = new UserModel ();

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (object));
            userModel = UserModel.from ((SerializableUserModel) inputStream.readObject ());
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return userModel;
    }

    public static TrackModel getTrackModel (File object) {
        TrackModel trackModel = new TrackModel ();

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (object));
            trackModel = TrackModel.from ((SerializableTrackModel) inputStream.readObject ());
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return trackModel;
    }

    public static AlbumModel getAlbumModel (File object) {
        AlbumModel albumModel = new AlbumModel ();

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (object));
            albumModel = AlbumModel.from ((SerializableAlbumModel) inputStream.readObject ());
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return albumModel;
    }

    public static ArtistModel getArtistModel (File object) {
        ArtistModel artistModel = new ArtistModel ();

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (object));
            artistModel = ArtistModel.from ((SerializableArtistModel) inputStream.readObject ());
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return artistModel;
    }

    public static PlaylistModel getPlaylistModel (File object) {
        PlaylistModel playlistModel = new PlaylistModel ();

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (object));
            playlistModel = PlaylistModel.from ((SerializablePlaylistModel) inputStream.readObject ());
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return playlistModel;
    }

    public static void writeModel (File object, Object serializableModel) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream (new FileOutputStream (object));
            outputStream.writeObject (serializableModel);
        } catch (Exception e) {
            e.printStackTrace ();
        }
    }

    public static void writePlaylists (Context context, UserModel userModel, List <PlaylistModel> playlists) {
        SerializableArray <SerializablePlaylistModel> serializableArray = new SerializableArray <> ();

        for (PlaylistModel playlist : playlists) {
            serializableArray.add (SerializablePlaylistModel.from (playlist));
        }

        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user,  "playlists.array");

        if (!user.exists ()) user.mkdir ();

        ModelTools.writeModel (data, serializableArray);
    }

    public static List <PlaylistModel> getPlaylists (Context context, UserModel userModel) {
        List <PlaylistModel> playlists = new ArrayList <> ();

        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user, "playlists.array");

        if (! data.exists ()) return null;

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (data));
            SerializableArray <SerializablePlaylistModel> serializableArray = (SerializableArray <SerializablePlaylistModel>) inputStream.readObject ();
            inputStream.close ();

            for (SerializablePlaylistModel serializablePlaylistModel : serializableArray) {
                playlists.add (PlaylistModel.from (serializablePlaylistModel));
            }

            return playlists;
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return null;
    }

    public void writeUser (Context context, UserModel userModel) {
        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user,  userModel.id + ".model");

        if (!user.exists ()) user.mkdir ();

        ModelTools.writeModel (data, SerializableUserModel.from (userModel));
    }

    public UserModel getUser (Context context, UserModel userModel) {
        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user, userModel.id + ".model");

        if (! data.exists ()) return null;

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (data));
            UserModel model = UserModel.from ((SerializableUserModel) inputStream.readObject ());
            inputStream.close ();

            return model;
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return null;
    }

    public void writeTracks (Context context, UserModel userModel, PlaylistModel playlist, List <TrackModel> trackModels) {
        SerializableArray <SerializableTrackModel> serializableArray = new SerializableArray <> ();

        for (TrackModel track : trackModels) {
            serializableArray.add (SerializableTrackModel.from (track));
        }

        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user,  playlist.id + ".array");

        if (!user.exists ()) user.mkdir ();

        ModelTools.writeModel (data, serializableArray);
    }

    public List <TrackModel> getTracks (Context context, UserModel userModel, PlaylistModel playlistModel) {
        List <TrackModel> tracks = new ArrayList <> ();

        File user = new File (context.getCacheDir (), userModel.id);
        File data = new File (user, playlistModel.id + ".array");

        if (! data.exists ()) return null;

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (data));
            SerializableArray <SerializableTrackModel> serializableArray = (SerializableArray <SerializableTrackModel>) inputStream.readObject ();
            inputStream.close ();

            for (SerializableTrackModel serializableTrackModel : serializableArray) {
                tracks.add (TrackModel.from (serializableTrackModel));
            }

            return tracks;
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return null;
    }
}
