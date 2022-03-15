package com.pixel.spotify.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.pixel.components.seekbar.SeekBar;
import com.pixel.spotify.R;

public class MediaControlsView extends ConstraintLayout {
    public static final int REQUEST_SUCCESS = 0;
    public static final int REQUEST_FAILURE = 1;

    private SeekBar seekBar;
    private TextView progressView;
    private TextView lengthView;

    private MediaController mediaController;

    private long progressMs;
    private long lengthMs;

    boolean isPlaying = false;
    long skipToPreviousWindowMs = 2000;

    public MediaControlsView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater inflater = LayoutInflater.from (context);
        inflater.inflate (R.layout.layout_media_controls, this, true);

        setBackground (null);

        seekBar = findViewById (R.id.seek_bar);
        progressView = findViewById (R.id.progress_view);
        lengthView = findViewById (R.id.length_view);

        seekBar.setInteractionListener (new SeekBar.InteractionListener () {
            @Override
            public void onProgressChanged (float newProgress, int flags) {
                if (mediaController != null) mediaController.onProgressChangeRequested (newProgress, flags);
            }
        });
    }

    public void play () {
        if (mediaController != null) isPlaying = mediaController.onPlayRequested ();
    }

    public void pause () {
        if (mediaController != null) isPlaying = ! mediaController.onPauseRequested ();
    }

    public void setMediaController (@NonNull MediaController mediaController) {
        this.mediaController = mediaController;
    }

    public long getProgress () {
        return progressMs;
    }

    public long getLength () {
        return lengthMs;
    }

    public boolean setProgress (long progressMs) {
        if (mediaController != null && mediaController.onPauseRequested ()) {
            this.progressMs = progressMs;

            progressView.setText (formatMs (progressMs));

            return true;
        }

        return false;
    }

    public void setLengthMs (long lengthMs) {
        this.lengthMs = lengthMs;

        lengthView.setText (formatMs (lengthMs));
    }

    public interface MediaController {
        boolean onPlayRequested ();

        boolean onPauseRequested ();

        boolean onProgressChangeRequested (float newProgress, int flags);
    }

    private static String formatMs (long ms) {
        int s = (int) ((ms / 1000) % 60);
        int mins = (int) ((ms / 1000) / 60);

        String d = String.format ("%02d", mins) + ":" + String.format ("%02d", s);

        return d;
    }
}
