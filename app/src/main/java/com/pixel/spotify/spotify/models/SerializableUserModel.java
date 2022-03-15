package com.pixel.spotify.spotify.models;

import android.graphics.Bitmap;

import com.pixel.components.bitmap.SerializableBitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializableUserModel implements Serializable {
    public String id;
    public String uri;
    public String name;
    public String email;
    public int followers;
    public List <SerializableBitmap> thumbnails;

    public SerializableUserModel () {
        thumbnails = new ArrayList <> ();
    }

    public static SerializableUserModel from (UserModel userModel) {
        SerializableUserModel serializableUserModel = new SerializableUserModel ();

        for (Bitmap bitmap : userModel.thumbnails) {
            serializableUserModel.thumbnails.add (SerializableBitmap.from (bitmap));
        }

        serializableUserModel.id = userModel.id;
        serializableUserModel.uri = userModel.uri;
        serializableUserModel.name = userModel.name;
        serializableUserModel.email = userModel.email;
        serializableUserModel.followers = userModel.followers;

        return serializableUserModel;
    }
}
