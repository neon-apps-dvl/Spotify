package com.pixel.spotify.ui;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static neon.pixel.components.Components.getPx;
import static neon.pixel.components.Components.getPxF;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.pixel.spotify.Adapter;
import com.pixel.spotify.AlbumView;
import com.pixel.spotify.ArtistView;
import com.pixel.spotify.R;
import com.pixel.spotifyapi.Objects.ArtistSimple;
import com.pixel.spotifyapi.Objects.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import neon.pixel.components.color.Argb;
import neon.pixel.components.color.Hct;

public class MediaControlsView extends CoordinatorLayout {
    private static final String TAG = "MediaControlsView";

    @LayoutRes
    private static final int LAYOUT = R.layout.layout_media_controls;

    private static final int CORNERS_DP = 24;

    private Track mTrack;
    private int mColor;

    private String mTrackName;
    private String mArtists;
    private String mAlbum;
    private long mDuration;

    private boolean mIsPlaying = false;

    private CoordinatorLayout mContainer;
    private CoordinatorLayout mView;
    private ConstraintLayout mContent;
    private BottomSheetBehavior mBehavior;

    private TextView mTrackNameView;
    private MaterialButton mPlayButton;
    private AppCompatSeekBar mSeekBar;
    private TextView mProgressView;
    private TextView mDurationView;

    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;

    @StringRes
    private int [] mTabTitles = {
            R.string.album_tab_title,
            R.string.artist_tab_title
    };

    private AlbumView mAlbumView;
    private List <ArtistView> mArtistViews;

    private Drawable mPlayDrawable;
    private Drawable mPauseDrawable;

    public MediaControlsView (@NonNull Context context) {
        super (context, null);
    }

    public MediaControlsView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super (context, attrs);

        LayoutInflater layoutInflater = LayoutInflater.from (context);
        layoutInflater.inflate (LAYOUT, this, true);

        setBackground (null);

        mContainer = findViewById (R.id.container);
        mView = findViewById (R.id.view);
        mContent = findViewById (R.id.content);
        mBehavior = BottomSheetBehavior.from (mView);
        mBehavior.addBottomSheetCallback (mCallback);

        mTrackNameView = findViewById (R.id.track_name_view);
        mPlayButton = findViewById (R.id.play_button);
        mSeekBar = findViewById (R.id.seek_bar);
        mProgressView = findViewById (R.id.progress_view);
        mDurationView = findViewById (R.id.duration_view);

        mTabLayout = findViewById (R.id.tab_layout);
        mViewPager = findViewById (R.id.view_pager);

        mPlayDrawable = getResources ().getDrawable (R.drawable.ic_play_24, context.getTheme ());
        mPauseDrawable = getResources ().getDrawable (R.drawable.ic_pause_24, context.getTheme ());

        mPlayButton.setOnClickListener (v -> {
            if (! mIsPlaying) {
                mIsPlaying = true;
                mPlayButton.setIcon (mPauseDrawable);
            }
            else {
                mIsPlaying = false;
                mPlayButton.setIcon (mPlayDrawable);
            }
        });

        mAlbumView = new AlbumView (context);
        mArtistViews = new ArrayList <> ();

        List <View> items = new ArrayList <> ();
        items.add (mAlbumView);

        Adapter temp = new Adapter ();
        temp.setItems (items);
        mViewPager.setAdapter (temp);
    }

    @Override
    protected void onLayout (boolean changed, int l, int t, int r, int b) {
        super.onLayout (changed, l, t, r, b);

        if (! changed) return;

//        new TabLayoutMediator (mTabLayout, mViewPager, (tab, position) -> {
//            tab.setText (getResources ().getString (mTabTitles [position])); // i love you so fucking much
//        }).attach ();
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        super.onSizeChanged (w, h, oldw, oldh);

        mContainer.setOutlineProvider (new ViewOutlineProvider () {
            @Override
            public void getOutline (View view, Outline outline) {
                outline.setRoundRect (
                        view.getLeft (),
                        view.getTop (),
                        view.getRight (),
                        view.getBottom (),
                        getPxF (getContext (), CORNERS_DP)
                );
            }
        });
    }

    public void setTrack (TrackWrapper wrappedTrack) {
        Track track = wrappedTrack.track;
        Bitmap bitmap = wrappedTrack.thumbnail;

        List <View> tabs = new ArrayList <> ();

        mAlbumView = new AlbumView (getContext ());
        tabs.add (mAlbumView);

        mArtistViews.clear ();

        for (ArtistSimple artist : track.artists) {
            ArtistView artistView = new ArtistView (getContext ());
            mArtistViews.add (artistView);
        }

        tabs.addAll (mArtistViews);

        Adapter adapter = new Adapter ();
        adapter.setItems (tabs);
        mViewPager.setAdapter (adapter);

        new TabLayoutMediator (mTabLayout, mViewPager, (tab, position) -> {
            if (position == 0) return;

            tab.setText (track.artists.get (position - 1) .name);
//                tab.setText (track.album.name); // i love you so fucking much
        }).attach ();

        SpannableString trackName = new SpannableString (track.name + " "  + track.artists.get (0).name);
        int color = Palette.from (bitmap).generate ()
                .getDominantSwatch ()
                .getRgb ();

        Hct surfaceColor = Hct.fromInt (color);
        surfaceColor.setTone (10);

        Hct primaryColor = Hct.fromInt (color);
        primaryColor.setTone (90);

        Argb c = Argb.from (primaryColor.toInt ());
        c.setAlpha (0.6f * 255);
        int secondaryColor = c.toInt ();

        c.setAlpha (0.24f * 255);
        int tertiaryColor = c.toInt ();

        c.setAlpha (0.16f * 255);
        int buttonColor = c.toInt ();

        mContent.setBackgroundTintList (new ColorStateList (new int[][] {{}}, new int[] {surfaceColor.toInt ()}));

        trackName.setSpan (new ForegroundColorSpan (primaryColor.toInt ()), 0, track.name.length (), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        trackName.setSpan (new ForegroundColorSpan (secondaryColor), track.name.length () + 1, trackName.length (), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        mTrackNameView.setText (trackName);
        mTrackNameView.setAutoSizeTextTypeUniformWithConfiguration (getPx (getContext (), 16), getPx (getContext (), 32), 1, TypedValue.COMPLEX_UNIT_PX);

        mProgressView.setText ("00:00");
        mDurationView.setText (TimeUnit.MILLISECONDS.toMinutes (track.duration_ms)
                + ":"
                + (TimeUnit.MILLISECONDS.toSeconds(track.duration_ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(track.duration_ms))));

        mPlayButton.setIconTint (new ColorStateList (new int[][] {{}}, new int[] {primaryColor.toInt ()}));
        mPlayButton.setRippleColor (new ColorStateList (new int[][] {{}}, new int[] {buttonColor}));

        mSeekBar.setProgressBackgroundTintList (new ColorStateList (new int[][]{{}}, new int[] {tertiaryColor}));
        mSeekBar.setProgressTintList (new ColorStateList (new int[][]{{}}, new int[] {primaryColor.toInt ()}));
        mSeekBar.setThumbTintList (new ColorStateList (new int[][]{{}}, new int[] {primaryColor.toInt ()}));

        mProgressView.setTextColor (tertiaryColor);
        mDurationView.setTextColor (tertiaryColor);

        mTabLayout.setTabTextColors (secondaryColor, primaryColor.toInt ());
        mTabLayout.setSelectedTabIndicatorColor (primaryColor.toInt ());

        SpannableString albumTabTitle = new SpannableString ("From " + track.album.name);
        albumTabTitle.setSpan (new ForegroundColorSpan (secondaryColor), 0, 4, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        albumTabTitle.setSpan (new ForegroundColorSpan (primaryColor.toInt ()), 5, albumTabTitle.length (), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        mTabLayout.getTabAt (0).setText (albumTabTitle);

//        for (int i = 0; i < track.artists.size (); i ++) {
//            mTabLayout.getTabAt (i + 1).setText (track.artists.get (i).name);
//        }
    }

    public void setAlbum (AlbumWrapper album) {
        mAlbumView.setAlbum (album);
    }

    public void addBottomSheetCallback (BottomSheetBehavior.BottomSheetCallback c) {
        mBehavior.addBottomSheetCallback (c);
    }

    private BottomSheetBehavior.BottomSheetCallback mCallback = new BottomSheetBehavior.BottomSheetCallback () {
        boolean isOpen;

        @Override
        public void onStateChanged (@NonNull View bottomSheet, int newState) {
            if (newState != STATE_COLLAPSED) {

            }

            if (newState == STATE_COLLAPSED) ;
            else if (newState == STATE_EXPANDED) ;
        }

        @Override
        public void onSlide (@NonNull View bottomSheet, float slideOffset) {

        }
    };
}
