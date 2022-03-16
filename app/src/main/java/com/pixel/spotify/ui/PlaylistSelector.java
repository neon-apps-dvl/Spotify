package com.pixel.spotify.ui;

import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
import static com.pixel.spotify.ui.color.Color.DynamicTone.SURFACE;
import static neon.pixel.components.Components.getPx;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pixel.spotify.R;
import com.pixel.spotify.spotify.models.PlaylistModel;
import com.pixel.spotify.spotify.models.Playlists;

import java.util.ArrayList;
import java.util.List;

import neon.pixel.components.color.Hct;
import neon.pixel.components.listview.ListView;

public class PlaylistSelector extends ConstraintLayout {
    @LayoutRes
    private static final int LAYOUT = R.layout.layout_playlist_selector;

    private GradientDrawable shapeOverlay;

    private View addPlaylistButton;
    private ImageView add;
    private TextView text;

    private ConstraintLayout topBar;
    private MaterialButton closeButton;
    private ListView listView;

    private TextView titleView1;
    private TextView titleViewPlaylist;
    private TextView infoView;

    private int selected;
    private Playlists playlists;
    private List <View> items;
    private List <String> ids;

    private int baseColor;

    private OnSelectionChangedListener mOnSelectionChangedListener;
    private OnStateChangedListener mOnStateChangedListener;

    public PlaylistSelector (@NonNull Context context) {
        super (context);

        playlists = new Playlists ();
        items = new ArrayList <> ();
        ids = new ArrayList <> ();

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        shapeOverlay = (GradientDrawable) getResources ().getDrawable (R.drawable.playlist_selector_shape_overlay, getContext ().getTheme ()).mutate ();

        addPlaylistButton = View.inflate (getContext (), R.layout.layout_add_playlist_button, null);

        addPlaylistButton.setBackgroundColor (getResources ().getColor (android.R.color.transparent, getContext ().getTheme ()));
        addPlaylistButton.setClickable (true);

        TypedValue ripple = new TypedValue ();
        context.getTheme ().resolveAttribute (android.R.attr.selectableItemBackground, ripple, true);
        addPlaylistButton.setBackgroundResource (ripple.resourceId);
        addPlaylistButton.setOnClickListener (v -> {

        });

        add = addPlaylistButton.findViewById (R.id.add);
        text = addPlaylistButton.findViewById (R.id.text);

        setBackground (shapeOverlay);
        setClipToOutline (true);

        setX (getPx (context, 12));
        setY (getPx (context, 12));

        topBar = findViewById (R.id.top_bar);

        titleView1 = findViewById (R.id.title_view_1);
        titleViewPlaylist = findViewById (R.id.title_view);
        infoView = findViewById (R.id.info_view);

        closeButton = findViewById (R.id.close_button);
        closeButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                close ();
            }
        });

        listView = findViewById (R.id.playlist_list_view);
        listView.addOnScrollListener (new RecyclerView.OnScrollListener () {
            @Override
            public void onScrollStateChanged (@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged (recyclerView, newState);
            }

            final int MAX_ELEVATION = (int) getPx (getContext (), 4);

            @Override
            public void onScrolled (@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled (recyclerView, dx, dy);

                float offset = recyclerView.computeVerticalScrollOffset ();
                float range = recyclerView.computeVerticalScrollRange ();
                float extent = recyclerView.computeVerticalScrollExtent ();
                float maxOffset = range - extent;

                float position = maxOffset > 0 ? offset / maxOffset : 0;

                float rawElevation = position / 0.1f * MAX_ELEVATION;
                float elevation = rawElevation <= MAX_ELEVATION ? rawElevation : MAX_ELEVATION;

                topBar.setElevation (elevation);
            }
        });
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout (changed, left, top, right, bottom);

        if (! changed) return;

        View parent = (View) getParent ();

        setLayoutParams (new CoordinatorLayout.LayoutParams ((int) (parent.getWidth () - getPx (getContext (), 24)), (int) (parent.getHeight () - getPx (getContext (), 24))));

        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams ((int) (listView.getWidth () - getPx (getContext (), 12)), (int) getPx (getContext (), 88));
        params.leftMargin = (int) getPx (getContext (), 12);
        params.rightMargin = (int) getPx (getContext (), 12);

        addPlaylistButton.setOutlineProvider (new ViewOutlineProvider () {
            @Override
            public void getOutline (View view, Outline outline) {
                outline.setRoundRect ((int) (view.getLeft () - getPx (getContext (), 12)),
                        view.getTop (),
                        (int) (view.getRight () - getPx (getContext (), 24)),
                        view.getBottom (),
                        24);
            }
        });
        addPlaylistButton.setClipToOutline (true);
        addPlaylistButton.setLayoutParams (params);
    }

    public void setPlaylists (int selected, Playlists playlists) {
        Log.d ("selector", "setPlaylists: " + selected + " " + playlists.items.get (selected).name);

        this.selected = selected;
        //items.clear ();
//        this.playlists = playlists;

        for (PlaylistModel playlist : playlists.items) {
            if (! this.playlists.contains (playlist)) {
                View item = View.inflate (getContext (), R.layout.playlist_selector_list_item, null);
                item.setLayoutParams (new ViewGroup.LayoutParams (-1, (int) getPx (getContext (), 88)));
                item.setBackgroundColor (getResources ().getColor (android.R.color.transparent, getContext ().getTheme ()));

                item.setOnClickListener (v -> {
                    int i = items.indexOf (v);
                    PlaylistModel p = playlists.items.get (i);

                    if (mOnSelectionChangedListener != null) mOnSelectionChangedListener.onSelectionChanged (p, false);
                });

                item.setOnLongClickListener (v -> {
                    int i = items.indexOf (v);
                    PlaylistModel p = this.playlists.items.get (i);

                    Log.d ("selector", "pinned " + p.name + "@" + i);

                    if (i != 0) {
                        this.playlists.items.set (i, this.playlists.items.get (0));
                        this.playlists.items.set (0, p);

                        items.set (i, items.get (0));
                        items.set (0, v);

                        listView.moveItem (i, 0);

                        listView.scrollToPosition (0);
                        listView.moveItem (1, i);

                        this.selected = 0;

                        if (mOnSelectionChangedListener != null) mOnSelectionChangedListener.onSelectionChanged (p, true);
                    }

                    titleViewPlaylist.setText (p.name);

                    setTextColor (baseColor);

                    return true;
                });

                ImageView thumbnail = item.findViewById (R.id.playlist_thumbnail);
                TextView title = item.findViewById (R.id.playlist_title);
                TextView count = item.findViewById (R.id.playlist_count);

                if (playlist.thumbnails.size () > 0) thumbnail.setImageBitmap (playlist.thumbnails.get (0));

                title.setText (playlist.name);
                if (playlist.trackCount > 0)
                    count.setText (getResources ().getQuantityString (R.plurals.playlist_track_count, playlist.trackCount, playlist.trackCount));
                else
                    count.setText (getResources ().getQuantityString (R.plurals.playlist_track_count, playlist.trackCount));

                this.playlists.add (playlist);
                items.add (item);
                listView.addItem (items.size () - 1, item);
            }
        }

        listView.addItem (addPlaylistButton);


        if (selected != 0) {
            PlaylistModel p = this.playlists.items.get (this.selected);
            View v = items.get (this.selected);

            this.playlists.items.set (this.selected, this.playlists.items.get (0));
            this.playlists.items.set (0, p);

            items.set (this.selected, items.get (0));
            items.set (0, v);

            listView.moveItem (this.selected, 0);

            listView.scrollToPosition (0);
            listView.moveItem (1, this.selected);

            this.selected = 0;
        }

        titleViewPlaylist.setText (this.playlists.items.get (this.selected).name);

        setTheme (baseColor);
    }

    public void setTheme (int color) {
        baseColor = color;

        /*
        CONTAINER =
         */


        Hct hct = Hct.fromInt (color);
        hct.setTone (SURFACE); // 20
        int backgroundColor = hct.toInt ();

        hct.setTone (PRIMARY); // 80
        int colorPrimary = hct.toInt ();

        hct.setTone (SECONDARY); // 50
        int colorSecondary = hct.toInt ();

        shapeOverlay.setColor (backgroundColor);
        setBackground (shapeOverlay);

        Log.e ("afhsdkjhfjk", "fksdjhfkjsdhfjsdhfjksdhfjksdhjkf");

        Drawable d = getContext ().getDrawable (R.drawable.rounded_rect_12).mutate ();
        d.setTint (Color.argb (128,
                Color.red (colorSecondary),
                Color.green (colorSecondary),
                Color.blue (colorSecondary)));
        Drawable a = getContext ().getDrawable (R.drawable.ic_add_24).mutate ();
        a.setTint (colorPrimary);

        add.setBackground (d);
        add.setImageDrawable (a);

        text.setTextColor (colorPrimary);

        topBar.setBackgroundColor (backgroundColor);
        closeButton.setIconTint (new ColorStateList (new int[][]{{}}, new int[] {colorPrimary}));
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed}, // enabled
                new int[] {android.R.attr.state_focused | android.R.attr.state_hovered}, // disabled
                new int[] {android.R.attr.state_focused}, // disabled
                new int[] {-android.R.attr.state_hovered}, // unchecked
                new int[] {}
        };

        android.graphics.Color p = android.graphics.Color.valueOf (colorPrimary);

        int [] colors = new int [] {
                android.graphics.Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                android.graphics.Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                android.graphics.Color.argb (0.12f * 255, p.red (), p.green (), p.blue ()),
                android.graphics.Color.argb (0.04f * 255, p.red (), p.green (), p.blue ()),
                android.graphics.Color.argb (0.00f * 255, p.red (), p.green (), p.blue ()),
        };

        ColorStateList ripple = new ColorStateList (states, colors);
        closeButton.setRippleColor (ripple);
        setTextColor (color);

        for (View item : items) {
            TextView title = item.findViewById (R.id.playlist_title);
            TextView count = item.findViewById (R.id.playlist_count);

            title.setTextColor (colorPrimary);
            count.setTextColor (colorSecondary);
        }
    }

    private void setTextColor (int color) {
        Hct hct = Hct.fromInt (color);
        hct.setTone (PRIMARY);

        int colorPrimary = hct.toInt ();

        hct.setTone (SECONDARY);

        int colorSecondary = hct.toInt ();

//        SpannableString s = new SpannableString (titleViewPlaylist.getText ());
//        s.setSpan (new ForegroundColorSpan (colorPrimary),
//                0,
//                titleViewPlaylist.getText ().length () - playlists.items.get (selected).name.length () - 1,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        s.setSpan (new ForegroundColorSpan (colorSecondary),
//                titleViewPlaylist.getText ().length () - playlists.items.get (selected).name.length (),
//                titleViewPlaylist.getText ().length (),
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        titleView1.setTextColor (colorPrimary);
        titleViewPlaylist.setTextColor (colorSecondary);
        infoView.setTextColor (colorSecondary);
    }

    public void open () {
        animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .y (getPx (getContext (), 12))
                .start ();

        if (mOnStateChangedListener != null) mOnStateChangedListener.onStateChanged (State.OPEN);
    }

    public void close () {
        View parent = (View) getParent ();

        animate ()
                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
                .y (parent.getHeight ())
                .start ();

        if (mOnStateChangedListener != null) mOnStateChangedListener.onStateChanged (State.CLOSED);
    }

    public void setOnSelectionChangedListener (OnSelectionChangedListener l) {
        this.mOnSelectionChangedListener = l;
    }

    public void setOnStateChangedListener (OnStateChangedListener l) {
        this.mOnStateChangedListener = l;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged (PlaylistModel selectedPlaylist, boolean pinned);
    }

    public interface OnStateChangedListener {
        void onStateChanged (State state);
    }

    public enum State {
        OPEN,
        CLOSED
    }
}
