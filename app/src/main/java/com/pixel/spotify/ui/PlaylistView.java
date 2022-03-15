package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.pixel.spotify.R;
import com.pixel.spotify.spotify.models.PlaylistModel;

public class PlaylistView extends CoordinatorLayout {
    private static final String TAG = "AlbumView";

    private static int size;

    private ImageView thumbnailView;

    private PlaylistModel playlistModel;

    public PlaylistView (Context context) {
        super (context);

        size = (int) getPx (context, 128);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate (R.layout.layout_playlist_view, this, true);
        setLayoutParams (new ViewGroup.LayoutParams (size, size));

        thumbnailView = findViewById (R.id.thumbnail_view);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);

        int minw = (int) (128 * getResources ().getDisplayMetrics ().density);
        int w = resolveSizeAndState(minw, widthMeasureSpec, 0);

        int minh = (int) (128 * getResources ().getDisplayMetrics ().density);
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    public void setPlaylist (PlaylistModel playlistModel) {
        this.playlistModel = playlistModel;

        thumbnailView.setImageBitmap (playlistModel.thumbnails.get (0));
    }
}
