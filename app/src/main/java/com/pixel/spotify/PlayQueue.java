package com.pixel.spotify;

import android.graphics.Bitmap;

import com.pixel.spotifyapi.Objects.Track;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class PlayQueue {
    private Queue <Map <Track, Bitmap>> mQueue = new LinkedList <> ();

    private static PlayQueue sInstance;

    private PlayQueue () {

    }

    public static PlayQueue getInstance () {
        if (sInstance == null) sInstance = new PlayQueue ();

        return sInstance;
    }

    public void push (Map <Track, Bitmap> track) {
        mQueue.add (track);
    }

    public void push (List <Map <Track, Bitmap>> tracks) {
        for (Map <Track, Bitmap> wrappedTrack : tracks) {
            mQueue.add (wrappedTrack);
        }
    }

    public Map <Track, Bitmap> peek () {
        return mQueue.peek ();
    }

    public Map <Track, Bitmap> pop () {
        return mQueue.poll ();
    }

    public interface Listener {

    }
}
