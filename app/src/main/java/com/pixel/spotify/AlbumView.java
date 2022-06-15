package com.pixel.spotify;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;

import com.pixel.spotify.ui.AlbumWrapper;
import com.pixel.spotify.ui.Tools;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.TrackSimple;

import java.util.ArrayList;
import java.util.List;

import neon.pixel.components.color.Argb;
import neon.pixel.components.color.Hct;
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

    public void setAlbum (AlbumWrapper album, String id) {
        mAlbum = album;

        List <View> items = new ArrayList <> ();

//        mListView.setBackgroundColor (Color.MAGENTA);

        int color = Palette.from (mAlbum.thumbnail).generate ()
                .getDominantSwatch ()
                .getRgb ();

        Hct colorPrimary = Hct.fromInt (color);
        colorPrimary.setTone (90);

        Argb colorSecondary = Argb.from (colorPrimary.toInt ());
        colorSecondary.setAlpha (0.6f * 255);

        for (TrackSimple track : mAlbum.album.tracks.items) {
            View item = LayoutInflater.from (getContext ()).inflate (R.layout.album_view_item, null, false);
//            item.setBackgroundColor (Color.RED);
            item.setLayoutParams (new ViewGroup.LayoutParams (-1, -2));
            TextView titleView = item.findViewById (R.id.title_view);
            TextView artistsView = item.findViewById (R.id.artists_view);
            TextView durationView = item.findViewById (R.id.duration_view);

            titleView.setText (track.name);
            durationView.setText (Tools.getTimeString (track.duration_ms));

            List <String> artists = new ArrayList <> ();

            for (ArtistSimple artist : track.artists) {
                artists.add (artist.name);
            }

            artistsView.setText (String.join (", ", artists));

            if (track.id.equals (id)) titleView.setTextColor (colorPrimary.toInt ());
            else titleView.setTextColor (colorSecondary.toInt ());

            artistsView.setTextColor (colorSecondary.toInt ());
            durationView.setTextColor (colorSecondary.toInt ());

            items.add (item);
            mListView.addItem (item);
        }

        Log.e ("null check", "item: " + items.get (0));

//        mListView.addItems (items);
    }
}
