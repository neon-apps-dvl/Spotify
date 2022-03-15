package com.pixel.spotify.spotify.adapter;

import com.pixel.spotify.spotify.models.AlbumModel;
import com.pixel.spotify.spotify.models.ArtistModel;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;

import java.util.List;

public abstract class SpotifyServiceManagerCallback {
    public void onGetUserPlaylists (List <PlaylistModel> playlists) {}

    public void onGetPlaylistTracks (List <TrackModel> playlists) {}

    public void onGetAlbumTracks (List <TrackModel> playlists) {}

    public void onGetTrack (TrackModel trackModel) {}

    public void onGetArtist (ArtistModel artistModel) {}

    public void onGetAlbum (AlbumModel albumModel) {}

    public void onGetUser (UserModel userModel) {}

    public void onGetPlaylist (PlaylistModel playlistModel) {}

    public void onPostTrack () {}

}
