package com.pixel.spotify.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class ContentViewModel extends ViewModel {
    private MutableLiveData <String> mUser;
    private MutableLiveData <List <PlaylistWrapper>> mPlaylists;
    private MutableLiveData <PlaylistWrapper> mPinnedPlaylist;
    private MutableLiveData <TrackWrapper> mTrack;

    public MutableLiveData <List <PlaylistWrapper>> getPlaylists () {
        if (mPlaylists == null) mPlaylists = new MutableLiveData <> ();

        return mPlaylists;
    }

    public MutableLiveData <PlaylistWrapper> getPinnedPlaylist () {
        if (mPinnedPlaylist == null) mPinnedPlaylist = new MutableLiveData <> ();

        return mPinnedPlaylist;
    }

    public MutableLiveData <TrackWrapper> getTrack () {
        if (mTrack == null) mTrack = new MutableLiveData <> ();

        return mTrack;
    }

    public LiveData <String> getUser () {
        if (mUser == null) mUser = new MutableLiveData <> ();

        return mUser;
    }

    public void setUser (String user) {
        mUser.setValue (user);
    }

    public void setPlaylists (List <PlaylistWrapper> playlists) {
        mPlaylists.setValue (playlists);
    }

    public void setPinnedPlaylist (PlaylistWrapper pinnedPlaylist) {
        mPinnedPlaylist.setValue (pinnedPlaylist);
    }

    public void setTrack (TrackWrapper track) {
        mTrack.setValue (track);
    }
}
