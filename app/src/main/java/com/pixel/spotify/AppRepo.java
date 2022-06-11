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

    String r;

    Queue <String> queue = new LinkedList <> ();
    List <String> tracks = new ArrayList <> ();

    public void getNextTrack () {
        //fetch data

        String t1 = "37lsV513gD04gFvKIPCw4N";

        String t2 = "5rurZZeggozpAZIHbI55cm";
        String t3 = "0rI56S1biB0efYypn7eNpP";

        r = t1;

        for (Listener l : mListeners) {
            l.onReturnNextTrack (r);
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
