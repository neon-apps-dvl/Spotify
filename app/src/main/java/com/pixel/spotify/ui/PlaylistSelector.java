//package com.pixel.spotify.ui;
//
//import static com.pixel.spotify.ui.color.Color.DynamicTone.PRIMARY;
//import static com.pixel.spotify.ui.color.Color.DynamicTone.SECONDARY;
//import static com.pixel.spotify.ui.color.Color.DynamicTone.SURFACE;
//import static neon.pixel.components.Components.getPx;
//
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.graphics.Color;
//import android.graphics.Outline;
//import android.graphics.drawable.Drawable;
//import android.graphics.drawable.GradientDrawable;
//import android.util.TypedValue;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewOutlineProvider;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.LayoutRes;
//import androidx.annotation.NonNull;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.coordinatorlayout.widget.CoordinatorLayout;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.button.MaterialButton;
//import com.pixel.spotify.R;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import neon.pixel.components.color.Hct;
//import neon.pixel.components.listview.ListView;
//
//public class PlaylistSelector extends ConstraintLayout {
//    @LayoutRes
//    private static final int LAYOUT = R.layout.layout_playlist_selector;
//
//    private GradientDrawable shapeOverlay;
//
//    private View mAddPlaylistButton;
//    private ImageView add;
//    private TextView text;
//
//    private ConstraintLayout mTopBar;
//    private MaterialButton mCloseButton;
//    private ListView mListView;
//
//    private TextView mTitleView;
//    private TextView mTitleViewPlaylist;
//    private TextView mInfoView;
//
//    private int mSelectedPlaylist;
//    private Playlists mPlaylists;
//    private List <View> mItems;
//    private List <String> mIds;
//
//    private int baseColor;
//
//    private OnSelectionChangedListener mOnSelectionChangedListener;
//    private OnStateChangedListener mOnStateChangedListener;
//
//    public PlaylistSelector (@NonNull Context context) {
//        super (context);
//
//        mPlaylists = new Playlists ();
//        mItems = new ArrayList <> ();
//        mIds = new ArrayList <> ();
//
//        LayoutInflater layoutInflater = LayoutInflater.from (context);
//        layoutInflater.inflate (LAYOUT, this, true);
//
//        shapeOverlay = (GradientDrawable) getResources ().getDrawable (R.drawable.playlist_selector_shape_overlay, getContext ().getTheme ()).mutate ();
//
//        mAddPlaylistButton = View.inflate (getContext (), R.layout.layout_add_playlist_button, null);
//
//        mAddPlaylistButton.setBackgroundColor (getResources ().getColor (android.R.color.transparent, getContext ().getTheme ()));
//        mAddPlaylistButton.setClickable (true);
//
//        TypedValue ripple = new TypedValue ();
//        context.getTheme ().resolveAttribute (android.R.attr.selectableItemBackground, ripple, true);
//        mAddPlaylistButton.setBackgroundResource (ripple.resourceId);
//        mAddPlaylistButton.setOnClickListener (v -> {
//
//        });
//
//        add = mAddPlaylistButton.findViewById (R.id.add);
//        text = mAddPlaylistButton.findViewById (R.id.text);
//
//        setBackground (shapeOverlay);
//        setClipToOutline (true);
//
//        setX (getPx (context, 12));
//        setY (getPx (context, 12));
//
//        mTopBar = findViewById (R.id.top_bar);
//
//        mTitleView = findViewById (R.id.title_view_1);
//        mTitleViewPlaylist = findViewById (R.id.title_view);
//        mInfoView = findViewById (R.id.info_view);
//
//        mCloseButton = findViewById (R.id.close_button);
//        mCloseButton.setOnClickListener (v -> close ());
//
//        mListView = findViewById (R.id.playlist_list_view);
//        mListView.addOnScrollListener (new RecyclerView.OnScrollListener () {
//            @Override
//            public void onScrollStateChanged (@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged (recyclerView, newState);
//            }
//
//            final int MAX_ELEVATION = (int) getPx (getContext (), 4);
//
//            @Override
//            public void onScrolled (@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled (recyclerView, dx, dy);
//
//                float offset = recyclerView.computeVerticalScrollOffset ();
//                float range = recyclerView.computeVerticalScrollRange ();
//                float extent = recyclerView.computeVerticalScrollExtent ();
//                float maxOffset = range - extent;
//
//                float position = maxOffset > 0 ? offset / maxOffset : 0;
//
//                float rawElevation = position / 0.1f * MAX_ELEVATION;
//                float elevation = rawElevation <= MAX_ELEVATION ? rawElevation : MAX_ELEVATION;
//
//                mTopBar.setElevation (elevation);
//            }
//        });
//    }
//
//    @Override
//    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout (changed, left, top, right, bottom);
//
//        if (! changed) return;
//
//        View parent = (View) getParent ();
//
//        setLayoutParams (new CoordinatorLayout.LayoutParams ((int) (parent.getWidth () - getPx (getContext (), 24)), (int) (parent.getHeight () - getPx (getContext (), 24))));
//
//        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams ((int) (mListView.getWidth () - getPx (getContext (), 12)), (int) getPx (getContext (), 88));
//        params.leftMargin = (int) getPx (getContext (), 12);
//        params.rightMargin = (int) getPx (getContext (), 12);
//
//        mAddPlaylistButton.setOutlineProvider (new ViewOutlineProvider () {
//            @Override
//            public void getOutline (View view, Outline outline) {
//                outline.setRoundRect ((int) (view.getLeft () - getPx (getContext (), 12)),
//                        view.getTop (),
//                        (int) (view.getRight () - getPx (getContext (), 24)),
//                        view.getBottom (),
//                        24);
//            }
//        });
//        mAddPlaylistButton.setClipToOutline (true);
//        mAddPlaylistButton.setLayoutParams (params);
//    }
//
//    public void setPlaylists (int selected, Playlists playlists) {
//        this.mSelectedPlaylist = selected;
//
//        for (PlaylistModel playlist : playlists.items) {
//            if (! this.mPlaylists.contains (playlist)) {
//                View item = View.inflate (getContext (), R.layout.playlist_selector_list_item, null);
//                item.setLayoutParams (new ViewGroup.LayoutParams (-1, (int) getPx (getContext (), 88)));
//                item.setBackgroundColor (getResources ().getColor (android.R.color.transparent, getContext ().getTheme ()));
//
//                item.setOnClickListener (v -> {
//                    int i = mItems.indexOf (v);
//                    PlaylistModel p = playlists.items.get (i);
//
//                    if (mOnSelectionChangedListener != null) mOnSelectionChangedListener.onSelectionChanged (p, false);
//                });
//
//                item.setOnLongClickListener (v -> {
//                    int i = mItems.indexOf (v);
//                    PlaylistModel p = this.mPlaylists.items.get (i);
//
//                    if (i != 0) {
//                        this.mPlaylists.items.set (i, this.mPlaylists.items.get (0));
//                        this.mPlaylists.items.set (0, p);
//
//                        mItems.set (i, mItems.get (0));
//                        mItems.set (0, v);
//
//                        mListView.moveItem (i, 0);
//
//                        mListView.scrollToPosition (0);
//                        mListView.moveItem (1, i);
//
//                        this.mSelectedPlaylist = 0;
//
//                        if (mOnSelectionChangedListener != null) mOnSelectionChangedListener.onSelectionChanged (p, true);
//                    }
//
//                    mTitleViewPlaylist.setText (p.name);
//
//                    setTextColor (baseColor);
//
//                    return true;
//                });
//
//                ImageView thumbnail = item.findViewById (R.id.playlist_thumbnail);
//                TextView title = item.findViewById (R.id.playlist_title);
//                TextView count = item.findViewById (R.id.playlist_count);
//
//                if (playlist.thumbnails.size () > 0) thumbnail.setImageBitmap (playlist.thumbnails.get (0));
//
//                title.setText (playlist.name);
//                if (playlist.trackCount > 0)
//                    count.setText (getResources ().getQuantityString (R.plurals.playlist_track_count, playlist.trackCount, playlist.trackCount));
//                else
//                    count.setText (getResources ().getQuantityString (R.plurals.playlist_track_count, playlist.trackCount));
//
//                this.mPlaylists.add (playlist);
//                mItems.add (item);
//                mListView.addItem (mItems.size () - 1, item);
//            }
//        }
//
//        mListView.addItem (mAddPlaylistButton);
//
//
//        if (selected != 0) {
//            PlaylistModel p = this.mPlaylists.items.get (this.mSelectedPlaylist);
//            View v = mItems.get (this.mSelectedPlaylist);
//
//            this.mPlaylists.items.set (this.mSelectedPlaylist, this.mPlaylists.items.get (0));
//            this.mPlaylists.items.set (0, p);
//
//            mItems.set (this.mSelectedPlaylist, mItems.get (0));
//            mItems.set (0, v);
//
//            mListView.moveItem (this.mSelectedPlaylist, 0);
//
//            mListView.scrollToPosition (0);
//            mListView.moveItem (1, this.mSelectedPlaylist);
//
//            this.mSelectedPlaylist = 0;
//        }
//
//        mTitleViewPlaylist.setText (this.mPlaylists.items.get (this.mSelectedPlaylist).name);
//
//        setColor (baseColor);
//    }
//
//    public void setColor (int color) {
//        baseColor = color;
//
//        Hct hct = Hct.fromInt (color);
//        hct.setTone (SURFACE);
//        int backgroundColor = hct.toInt ();
//
//        hct.setTone (PRIMARY);
//        int colorPrimary = hct.toInt ();
//
//        hct.setTone (SECONDARY);
//        int colorSecondary = hct.toInt ();
//
//        shapeOverlay.setColor (backgroundColor);
//        setBackground (shapeOverlay);
//
//        Drawable d = getContext ().getDrawable (R.drawable.rounded_rect_12).mutate ();
//        d.setTint (Color.argb (128,
//                Color.red (colorSecondary),
//                Color.green (colorSecondary),
//                Color.blue (colorSecondary)));
//        Drawable a = getContext ().getDrawable (R.drawable.ic_add_24).mutate ();
//        a.setTint (colorPrimary);
//
//        add.setBackground (d);
//        add.setImageDrawable (a);
//
//        text.setTextColor (colorPrimary);
//
//        mTopBar.setBackgroundColor (backgroundColor);
//        mCloseButton.setIconTint (new ColorStateList (new int[][]{{}}, new int[] {colorPrimary}));
//        int[][] closeButtonRippleStates = new int[][] {
//                new int[] { android.R.attr.state_pressed}, // enabled
//                new int[] {android.R.attr.state_focused | android.R.attr.state_hovered}, // disabled
//                new int[] {android.R.attr.state_focused}, // disabled
//                new int[] {-android.R.attr.state_hovered}, // unchecked
//                new int[] {}
//        };
//
//        android.graphics.Color holder = android.graphics.Color.valueOf (colorPrimary);
//
//        int [] closeButtonRippleColors = new int [] {
//                android.graphics.Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
//                android.graphics.Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
//                android.graphics.Color.argb (0.12f * 255, holder.red (), holder.green (), holder.blue ()),
//                android.graphics.Color.argb (0.04f * 255, holder.red (), holder.green (), holder.blue ()),
//                android.graphics.Color.argb (0.00f * 255, holder.red (), holder.green (), holder.blue ()),
//        };
//
//        ColorStateList closeButtonRipple = new ColorStateList (closeButtonRippleStates, closeButtonRippleColors);
//        mCloseButton.setRippleColor (closeButtonRipple);
//        setTextColor (color);
//
//        for (View item : mItems) {
//            TextView title = item.findViewById (R.id.playlist_title);
//            TextView count = item.findViewById (R.id.playlist_count);
//
//            title.setTextColor (colorPrimary);
//            count.setTextColor (colorSecondary);
//        }
//    }
//
//    private void setTextColor (int color) {
//        Hct hct = Hct.fromInt (color);
//        hct.setTone (PRIMARY);
//
//        int colorPrimary = hct.toInt ();
//
//        hct.setTone (SECONDARY);
//
//        int colorSecondary = hct.toInt ();
//
//        mTitleView.setTextColor (colorPrimary);
//        mTitleViewPlaylist.setTextColor (colorSecondary);
//        mInfoView.setTextColor (colorSecondary);
//    }
//
//    public void open () {
//        animate ()
//                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
//                .y (getPx (getContext (), 12))
//                .start ();
//
//        if (mOnStateChangedListener != null) mOnStateChangedListener.onStateChanged (State.OPEN);
//    }
//
//    public void close () {
//        View parent = (View) getParent ();
//
//        animate ()
//                .setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime))
//                .y (parent.getHeight ())
//                .start ();
//
//        if (mOnStateChangedListener != null) mOnStateChangedListener.onStateChanged (State.CLOSED);
//    }
//
//    public void setOnSelectionChangedListener (OnSelectionChangedListener l) {
//        this.mOnSelectionChangedListener = l;
//    }
//
//    public void setOnStateChangedListener (OnStateChangedListener l) {
//        this.mOnStateChangedListener = l;
//    }
//
//    public interface OnSelectionChangedListener {
//        void onSelectionChanged (PlaylistModel selectedPlaylist, boolean pinned);
//    }
//
//    public interface OnStateChangedListener {
//        void onStateChanged (State state);
//    }
//
//    public enum State {
//        OPEN,
//        CLOSED
//    }
//}
