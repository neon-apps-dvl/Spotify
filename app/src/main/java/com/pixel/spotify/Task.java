package com.pixel.spotify;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// FIXME: migrate to standard components lib
public abstract class Task {
    private ScheduledExecutorService mExecutor;
    private Runnable mRunnable;

    public Task () {
        mExecutor = Executors.newSingleThreadScheduledExecutor ();
    }

    public void startOneTime () {
        mRunnable = () -> doWork ();
        mExecutor.execute (mRunnable);
        mExecutor.shutdown ();
    }

    public void startPeriodic (long period) {
        mExecutor = Executors.newSingleThreadScheduledExecutor ();
        mRunnable = () -> {
            doWork ();
        };
        mExecutor.scheduleAtFixedRate (mRunnable, 0, period, TimeUnit.MILLISECONDS);
    }

    public void startPeriodic (long period, int count) {
        mExecutor = Executors.newSingleThreadScheduledExecutor ();
        mRunnable = new Runnable () {
            int c = 0;

            @Override
            public void run () {
                doWork ();
                c += 1;

                if (c == count) mExecutor.shutdown ();
            }
        };
        mExecutor.scheduleAtFixedRate (mRunnable, 0, period, TimeUnit.MILLISECONDS);
    }

    public void startListenableOneTime () {
        StateListener mStateListener = new StateListener () {
            @Override
            public void onCompleted () {
                mExecutor.shutdown ();
            }

            @Override
            public void onFailed () {

            }
        };

        mRunnable = () -> doWork (mStateListener);
        mExecutor.execute (mRunnable);
    }

    public void startListenablePeriodic (long period) {
        StateListener mStateListener = new StateListener () {
            @Override
            public void onCompleted () {
                mExecutor.shutdown ();
            }

            @Override
            public void onFailed () {

            }
        };

        mExecutor = Executors.newSingleThreadScheduledExecutor ();
        mRunnable = () -> doWork (mStateListener);

        mExecutor.scheduleAtFixedRate (mRunnable, 0, period, TimeUnit.MILLISECONDS);
    }

    @Deprecated
    public void startListenablePeriodic (long period, int repeat) {
        StateListener mStateListener = new StateListener () {
            @Override
            public void onCompleted () {
                mExecutor.shutdown ();
            }

            @Override
            public void onFailed () {

            }
        };

        mExecutor = Executors.newSingleThreadScheduledExecutor ();
        mRunnable = () -> {
            doWork (mStateListener);
        };

        mExecutor.scheduleAtFixedRate (mRunnable, 0, period, TimeUnit.MILLISECONDS);
    }

    public void stop () {
        mExecutor.shutdownNow ();
    }

    public void doWork () {}

    public void doWork (StateListener l) {}
}
