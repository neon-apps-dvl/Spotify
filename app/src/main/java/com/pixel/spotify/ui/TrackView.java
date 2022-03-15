package com.pixel.spotify.ui;

import static com.pixel.components.Components.getPx;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import com.pixel.spotify.R;
import com.pixel.spotify.spotify.models.TrackModel;

import java.util.ArrayList;
import java.util.List;

public class TrackView extends CoordinatorLayout {
    private static final String TAG = "View";

    public static final int RELEASED = 0;
    public static final int PRESSED = 1;
    public static final int MOVED = 2;

    private float anchorX;
    private float anchorY;

    protected float rawScaleX;
    protected float rawScaleY;
    protected float scaleX;
    protected float scaleY;
    protected float stretchX;
    protected float stretchY;

    private float touchX;
    private float touchY;
    private float dx;
    private float dy;

    private ValueAnimator dxAnimator;
    private ValueAnimator dyAnimator;

    private ValueAnimator xAnimator;
    private ValueAnimator yAnimator;

    protected View anchorView;

    private ImageView thumbnailView;
    public TrackModel trackModel;

    List <Drawable> autoplayListThumbnails;
    List <String> autoplayListTracks;

    GestureDetector trackViewGestureDetector;

    private InteractionListener interactionListener;

    @SuppressLint ("ClickableViewAccessibility")
    public TrackView (Context context, View anchorView, float anchorX, float anchorY) {
        super (context);
        this.anchorView = anchorView;
        this.anchorX = anchorX;
        this.anchorY = anchorY;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate (R.layout.layout_track_view, this, true);
        setLayoutParams (new ViewGroup.LayoutParams ((int) getPx (context, 256), (int) getPx (context, 256)));
        setElevation (getPx (getContext (), 8)); // FIXME: set elevation

        thumbnailView = findViewById (R.id.thumbnail_view);

        autoplayListThumbnails = new ArrayList <> ();
        autoplayListTracks = new ArrayList <> ();

        setOnTouchListener (onTouchListener);
        trackViewGestureDetector = new GestureDetector (getContext (), trackViewOnGestureListener);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure (widthMeasureSpec, heightMeasureSpec);

        int minw = (int) (128 * getResources ().getDisplayMetrics ().density);
        int w = resolveSizeAndState(minw, widthMeasureSpec, 0);

        int minh = (int) (128 * getResources ().getDisplayMetrics ().density);
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setX (anchorX);
        setY (anchorY);

        setMeasuredDimension(w, h);
    }

    public void setTrack (TrackModel trackModel) {
        this.trackModel = trackModel;

        thumbnailView.setImageBitmap (trackModel.thumbnails.get (0));
    }

    private void snapToAnchor () {
        xAnimator = ValueAnimator.ofFloat (getX (), anchorX);
        xAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        xAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                setX ((Float) animation.getAnimatedValue ());

                computeScale ();
                computeStretch ();

                dispatchOnPositionChanged (TrackView.this, getX (), getY (), scaleX, scaleY, stretchX, stretchY, false);
            }
        });

        yAnimator = ValueAnimator.ofFloat (getY (), anchorY);
        yAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
        yAnimator.addUpdateListener (new ValueAnimator.AnimatorUpdateListener () {
            @Override
            public void onAnimationUpdate (ValueAnimator animation) {
                setY ((Float) animation.getAnimatedValue ());

                computeScale ();
                computeStretch ();

                dispatchOnPositionChanged (TrackView.this, getX (), getY (), scaleX, scaleY, stretchX, stretchY, false);
            }
        });

        xAnimator.start ();
        yAnimator.start ();
    }

    public void flingOut (float velocityX, float velocityY) {
        FlingAnimation flingX = new FlingAnimation (this, DynamicAnimation.TRANSLATION_X);
        flingX.setStartVelocity (velocityX)
                .setFriction (0.1f)
                .start ();

        FlingAnimation flingY = new FlingAnimation (this, DynamicAnimation.TRANSLATION_Y);
        flingY.setStartVelocity (velocityY)
                .setFriction (0.1f)
                .start ();
    }

    private void computeDx () {
        dx = getX () - touchX;
    }

    private void computeDy () {
        dy = getY () - touchY;
    }

    private void deflateDx () {
        if (dxAnimator == null || ! dxAnimator.isRunning ()) {
            dxAnimator = ValueAnimator.ofFloat (dx, -getWidth () / 2);
            dxAnimator.setInterpolator (new LinearInterpolator ());
            dxAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            dxAnimator.addUpdateListener ((animation) -> {
                dx = (float) animation.getAnimatedValue ();

                computeScale ();
                computeStretch ();

                setX (touchX + dx);
            });
            dxAnimator.start ();
        }
    }

    private void deflateDy () {
        if (dyAnimator == null || ! dyAnimator.isRunning ()) {
            dyAnimator = ValueAnimator.ofFloat (dy, -getHeight () / 2);
            dyAnimator.setInterpolator (new LinearInterpolator ());
            dyAnimator.setDuration (getResources ().getInteger (android.R.integer.config_shortAnimTime));
            dyAnimator.addUpdateListener ((animation) -> {
                dy = (float) animation.getAnimatedValue ();

                setY (touchY + dy);

                computeScale ();
                computeStretch ();
            });
            dyAnimator.start ();
        }
    }

    private void stopDxDeflation () {
        if (dxAnimator != null) {
            dxAnimator.cancel ();
            dxAnimator = null;
        }
    }

    private void stopDyDeflation () {
        if (dyAnimator != null) {
            dyAnimator.cancel ();
            dyAnimator = null;
        }
    }

    private OnTouchListener onTouchListener = new OnTouchListener () {
        private float pX;
        private float pY;

        @Override
        public boolean onTouch (View v, MotionEvent event) {
            if (trackViewGestureDetector.onTouchEvent (event)) return true;

            int action = event.getAction ();
            touchX = event.getRawX () - anchorView.getX ();
            touchY = event.getRawY () - anchorView.getY ();

            if (action == MotionEvent.ACTION_DOWN) {
                computeDx ();
                computeDy ();

                deflateDx ();
                deflateDy ();
            }
            else if (action == MotionEvent.ACTION_MOVE) {
                float rawTouchDx = touchX - pX;
                float rawTouchDy = touchY - pY;

                float touchDx = Math.abs (rawTouchDx);
                float touchDy = Math.abs (rawTouchDy);

                float directionX = rawTouchDx != 0 ? rawTouchDx / touchDx : 0;
                float directionY = rawTouchDy != 0 ? rawTouchDy / touchDy : 0;

                if (touchDx < event.getXPrecision ()) directionX = 0;
                if (touchDy < event.getYPrecision ()) directionY = 0;

                if (directionX == -1) {
                    if (dx + getWidth () / 2 > 0) {
                        deflateDx ();
                    }
                    else if (dx + getWidth () / 2 < 0){
                        stopDxDeflation ();
                        computeDx ();
                    }
                }
                else if (directionX == 1) {
                    if (dx + getWidth () / 2 < 0) {
                        deflateDx ();
                    }
                    else if (dx + getWidth () / 2 > 0) {
                        stopDxDeflation ();
                        computeDx ();
                    }
                }

                if (directionY == -1) {
                    if (dy + getHeight () / 2 > 0) {
                        deflateDy ();
                    }
                    else if (dy + getHeight () / 2 < 0) {
                        stopDyDeflation ();
                        computeDy ();
                    }
                }
                else if (directionY == 1) {
                    if (dy + getHeight () / 2 < 0) {
                        deflateDy ();
                    }
                    else if (dy + getHeight () / 2 > 0) {
                        stopDyDeflation ();
                        computeDy ();
                    }
                }

                setX (touchX + dx);
                setY (touchY + dy);

                pX = touchX;
                pY = touchY;

                computeScale ();
                computeStretch ();
                dispatchOnPositionChanged (TrackView.this, getX (), getY (), scaleX, scaleY, stretchX, stretchY, true);
            }
            else if (action == MotionEvent.ACTION_UP) {
                stopDxDeflation ();
                stopDyDeflation ();

                snapToAnchor ();
            }

            return true;
        }
    };

    private GestureDetector.OnGestureListener trackViewOnGestureListener = new GestureDetector.SimpleOnGestureListener () {
        @Override
        public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = velocityX / ViewConfiguration.get (getContext ()).getScaledMaximumFlingVelocity ();
            float y = velocityY / ViewConfiguration.get (getContext ()).getScaledMaximumFlingVelocity ();

            float v = (float) Math.sqrt (x * x + y * y);
            float dx = Math.abs (e1.getRawX () - e2.getRawX ());
            float dy = Math.abs (e1.getRawY () - e2.getRawY ());

            float d = (float) Math.sqrt (dx * dx + dy * dy) / getContext ().getResources ().getDisplayMetrics ().widthPixels;

            if (d >= 0.4 && (e1.getRawX () - e2.getRawX ()) > 0) {
                //flingOut (-ViewConfiguration.get (getContext ()).getScaledMaximumFlingVelocity (), velocityY);

                //return true;
            }

            return false;
        }
    };

    public void computeScale () {
        rawScaleX = (getX () + getWidth () / 2 - anchorView.getWidth () / 2) / (0.5f * anchorView.getWidth ());
        rawScaleY = (getY () + getHeight () / 2 - anchorView.getHeight () / 2) / (0.5f * anchorView.getHeight ());

        scaleX = Math.abs ((getX () + getWidth () / 2 - anchorView.getWidth () / 2) / (0.5f * anchorView.getWidth ()));
        scaleY = Math.abs ((getY () + getHeight () / 2 - anchorView.getHeight () / 2) / (0.5f * anchorView.getHeight ()));
    }

    public void computeStretch () {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator ();

        float _x = rawScaleX / scaleX;
        float _y = rawScaleY / scaleY;

        stretchX = (rawScaleX != 0 ? _x : 0) * decelerateInterpolator.getInterpolation (scaleX);
        stretchY = (rawScaleY != 0 ? _y : 0) * decelerateInterpolator.getInterpolation (scaleY);
    }

    public void setInteractionListener (InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    public void dispatchOnPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down) {
        if (interactionListener != null) interactionListener.onPositionChanged (v, x, y, scaleX, scaleY, stretchX, stretchY, down);
    }

    public interface InteractionListener {
        void onPositionChanged (TrackView v, float x, float y, float scaleX, float scaleY, float stretchX, float stretchY, boolean down);
    }
}
