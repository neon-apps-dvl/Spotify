package com.pixel.spotify;

// FIXME: migrate to standard components lib
public abstract class StateListener {
    public void completed () {
        onCompleted ();
    }

    public void failed () {
        onFailed ();
    }

    public abstract void onCompleted ();
    public abstract void onFailed ();
}
