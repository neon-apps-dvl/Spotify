package com.pixel.spotify.spotify;

import android.content.Context;

import com.pixel.spotify.Task;
import com.pixel.spotify.spotify.models.TrackModel;
import com.pixel.spotify.spotify.models.UserModel;
import com.pixel.spotifyapi.Objects.Track;
import com.pixel.spotifyapi.SpotifyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayQueue implements Serializable {
    private static final String PLAY_QUEUE_DIR = "play_queues";
    private static final String PLAY_QUEUE = ".play";

    private static int MAX_QUEUE_SIZE = 4;

    private String [] mQueue;
    private TrackModel [] mDequeued;

    public PlayQueue () {
        mQueue = new String[0];
        mDequeued = new TrackModel [0];
    }

    private PlayQueue (String [] q) {
        mQueue = q;
        mDequeued = new TrackModel[0];
    }

    public void getNext (SpotifyService spotifyService, PlayQueueCallback callback) {
        Task loadTask = new Task () {
            @Override
            public void doWork () {
                callback.onGetNext ((TrackModel []) load (spotifyService, 0).toArray ());
            }
        };
    }

    private static PlayQueue load (Context context, UserModel user) {
        File tempParamsDir = new File (context.getFilesDir (), PLAY_QUEUE_DIR);
        File userPlayQueueFile = new File (tempParamsDir, user.id + PLAY_QUEUE);

        try {
            ObjectInputStream inputStream = new ObjectInputStream (new FileInputStream (userPlayQueueFile));
            String [] q = (String[]) inputStream.readObject ();
            inputStream.close ();

            return new PlayQueue (q);
        } catch (Exception e) {
            e.printStackTrace ();
        }

        return new PlayQueue ();
    }

    private List <TrackModel> load (SpotifyService spotifyService, int count) {
        List <TrackModel> l = new ArrayList <> ();

        String tmmlp2 = "3IaAYtmN8T0YIYVqnxNnVz"; //"6DN7GcZF1HywzrkGN6Eeqk";
        String recovery = "5M6ARntRsBgfp9YINgRHrH"; //"1gUI4keDXbeSil6rwY9qUm";
        String tsslp = "59z8uxWZVFpL2LfZ5C9AzY"; //"10nO3EJJDMm6j6d2uK3Jah";
        String tmmlp = "1WopKLOG4Kud0H1r3xrCfT"; //"6t7956yu5zYf5A829XRiHC";
        String relapse = "6KEO499uUuvv65wAfOltod"; //"7MZzYkbHL9Tk3O6WeD4Z0Z";
        String mtbmb = "7ccTcabbJlCJiIqtrSSwBk"; //"4otkd9As6YaxxEkIjXPiZ6";
        String mtbmbsb = "1sv41rYgHhPWdyzwk5K9zy"; //"3MKvhQoFSrR2PrxXXBHe9B";

        String [] r = {
                tmmlp2,
                recovery,
                tsslp,
                tmmlp,
                relapse,
                mtbmb,
                mtbmbsb
        };

        for (String t : r) {
            Track track = spotifyService.getTrack (t);

            l.add (TrackModel.fromTrack (spotifyService, track));
        }

        return l;
    }
}
