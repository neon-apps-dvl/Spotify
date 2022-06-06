package com.pixel.spotify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AppRepo {
    private static final String TAG = "AppRepo";

    private Queue <String> trackQueue = new LinkedList <> ();
    private List <String> mPlaylists = new ArrayList <> ();
    private String mPinnedPlaylist;

    private List <Listener> mListeners = new ArrayList <> ();

    private static AppRepo sInstance;

    private AppRepo () {

    }

    public static AppRepo getInstance () {
        if (sInstance == null) sInstance = new AppRepo ();

        return sInstance;
    }

    public void getNextTrack () {
        //fetch data

        String tempTrack = "6KEO499uUuvv65wAfOltod";

        for (Listener l : mListeners) {
            l.onReturnNextTrack (tempTrack);
        }
    }

    public String getPinnedPlaylist () {
        return mPinnedPlaylist;
    }

    public List <String> getPlaylists () {
        return mPlaylists;
    }

    public void setPlaylists (List <String> playlists) {
        for (String playlist : playlists) {
            if (! mPlaylists.contains (playlist)) mPlaylists.add (playlist);
        }

        for (Listener listener : mListeners) {
            listener.onPlaylistsChanged (mPlaylists);
        }
    }

    public void setPinnedPlaylist (String pinnedPlaylist) {
        if (mPlaylists.contains (pinnedPlaylist)) mPinnedPlaylist = pinnedPlaylist;
    }

    public void registerListener (Listener l) {
        if (!mListeners.contains (l)) mListeners.add (l);
    }

    public interface Listener {
        void onReturnNextTrack (String id);

        void onPlaylistsChanged (List <String> playlists);

        void onPinnedPlaylistChanged (String pinnedPlaylist);
    }
}
