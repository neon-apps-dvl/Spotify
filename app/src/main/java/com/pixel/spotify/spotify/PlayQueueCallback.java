package com.pixel.spotify.spotify;

import com.pixel.spotify.spotify.models.TrackModel;

public interface PlayQueueCallback {
    void onGetNext (TrackModel... tracks);
}