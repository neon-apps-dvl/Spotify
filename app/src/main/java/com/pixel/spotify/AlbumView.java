package com.pixel.spotify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.pixel.spotify.ui.AlbumWrapper;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.TrackSimple;

import java.util.ArrayList;
import java.util.List;

import neon.pixel.components.listview.ListView;

public class AlbumView extends CoordinatorLayout {
    private AlbumWrapper mAlbum;
    private ListView mListView;

    private static final int LAYOUT = R.layout.fragment_album;

    public AlbumView (@NonNull Context context) {
        super (context);

        LayoutInflater.from (context).inflate (LAYOUT, this, true);

        mListView = findViewById (R.id.list_view);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged (w, h, oldw, oldh);
    }

    public void setAlbum (AlbumWrapper album) {
        mAlbum = album;

        List <View> items = new ArrayList <> ();

        for (TrackSimple track : mAlbum.album.tracks.items) {
            View item = View.inflate (getContext (), R.layout.album_view_item, null);
            TextView titleView = item.findViewById (R.id.title_view);
            TextView artistsView = item.findViewById (R.id.artists_view);

            titleView.setText (track.name);

            List <String> artists = new ArrayList <> ();

            for (ArtistSimple artist : track.artists) {
                artists.add (artist.name);
            }

            artistsView.setText (String.join (", ", artists));

            items.add (item);
        }

        mListView.addItems (items);
    }
}
