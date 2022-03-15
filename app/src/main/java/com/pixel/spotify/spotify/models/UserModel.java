package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.BitmapTools;
import com.pixel.components.bitmap.SerializableBitmap;
import com.pixel.spotify.spotify.adapter.SpotifyServiceManagerCallback;
import com.pixel.spotifyapi.Objects.Image;
import com.pixel.spotifyapi.Objects.UserPrivate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public String email;
    public int followers;
    public List <Bitmap> thumbnails;

    public UserModel () {
        thumbnails = new ArrayList <> ();
    }

    public static UserModel from (UserPrivate user) {
        UserModel userModel = new UserModel ();

        for (Image thumbnail : user.images) {
            userModel.thumbnails.add (BitmapTools.from (thumbnail.url));
        }

        userModel.id = user.id;
        userModel.uri = user.uri;
        userModel.name = user.display_name;
        userModel.email = user.email;
        userModel.followers = user.followers.total;

        return userModel;
    }

    public static void from (UserPrivate user, SpotifyServiceManagerCallback callback) {
        new Thread (() -> {
            callback.onGetUser (from (user));
        }).start ();
    }

    public static UserModel from (SerializableUserModel serializableUserModel) {
        UserModel userModel = new UserModel ();

        for (SerializableBitmap serializableBitmap : serializableUserModel.thumbnails) {
            userModel.thumbnails.add (serializableBitmap.getBitmap ());
        }

        userModel.id = serializableUserModel.id;
        userModel.uri = serializableUserModel.uri;
        userModel.name = serializableUserModel.name;
        userModel.email = serializableUserModel.email;
        userModel.followers = serializableUserModel.followers;

        return userModel;
    }
}
