package com.pixel.spotify.spotify;

import com.pixel.spotify.spotify.models.PlaylistModel;

import java.util.List;

public interface UserPlaylistsPullTaskCallback {
    void onComplete (List <PlaylistModel> playlists);
}
