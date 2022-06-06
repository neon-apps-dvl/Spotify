package com.pixel.spotify;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.pixel.spotify.spotify.adapter.SpotifyServiceAdapter;
import com.pixel.spotifyapi.Objects.PlaylistSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ContentWorker extends ListenableWorker {
    public ContentWorker (@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super (context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture <Result> startWork () {
        return CallbackToFutureAdapter.getFuture (completer -> {
            Callback callback = () -> {
                completer.set (Result.success ());
            };

            Executor executor = Executors.newSingleThreadExecutor ();
            executor.execute (() -> {
                String user = SpotifyServiceAdapter.getInstance ()
                        .getCurrentUser ()
                        .id;

                List <PlaylistSimple> playlists = SpotifyServiceAdapter.getInstance ()
                        .getCurrentUserPlaylists ()
                        .items;

                List <String> playlistIds = new ArrayList <> ();

                for (PlaylistSimple playlist : playlists) {
                    if (playlist.owner.id.equals (user)) {
                        playlistIds.add (playlist.id);
                    }
                }

                Handler.createAsync (Looper.getMainLooper ())
                        .post (() -> {
                            AppRepo.getInstance ()
                                    .setPlaylists (playlistIds);

                            AppRepo.getInstance ()
                                    .setPinnedPlaylist (playlistIds.get (0));
                        });

                callback.onCompleted ();
            });

            return callback;
        });
    }

    private interface Callback {
        void onCompleted ();
    }
}
