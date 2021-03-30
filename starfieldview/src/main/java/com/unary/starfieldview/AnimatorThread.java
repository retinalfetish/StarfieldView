package com.unary.starfieldview;

import android.animation.TimeAnimator;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

/**
 * Custom thread with a looper and a handler to manage the lifecycle of a time animator.
 */
public class AnimatorThread extends Thread {

    private Handler mHandler;
    private TimeAnimator mTimeAnimator;
    private TimeAnimator.TimeListener mTimeListener;
    private Runnable mStartRunnable;
    private Runnable mCancelRunnable;
    private boolean mStart;

    /**
     * Constructor for the animator thread with a reference to a time animator listener.
     *
     * @param timeListener Time animator listener.
     */
    public AnimatorThread(@Nullable TimeAnimator.TimeListener timeListener) {
        super();

        mTimeListener = timeListener;

        // Reusable runnable
        mStartRunnable = new Runnable() {
            @Override
            public void run() {
                mTimeAnimator.start();
            }
        };

        // Reusable runnable
        mCancelRunnable = new Runnable() {
            @Override
            public void run() {
                mTimeAnimator.cancel();
            }
        };
    }

    @CallSuper
    @Override
    public void run() {
        Looper.prepare();

        // Provision the animator
        mHandler = new Handler();
        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setTimeListener(mTimeListener);

        // Catch up on off-thread request
        if (mStart) {
            mTimeAnimator.start();
        }

        Looper.loop();
    }

    @CallSuper
    @Override
    public void interrupt() {
        super.interrupt();

        // Cleanup before quitting
        if (mTimeAnimator != null) {
            mTimeAnimator.removeAllListeners();
            cancelAnimator();
        }

        if (mHandler != null) {
            mHandler.getLooper().quit();
        }
    }

    /**
     * Composition method to post on the animator thread and start the animator.
     */
    public void startAnimator() {
        mStart = true;

        if (mHandler != null && mTimeAnimator != null) {
            mHandler.post(mStartRunnable);
        }
    }

    /**
     * Composition method to post on the animator thread and cancel the animator.
     */
    public void cancelAnimator() {
        mStart = false;

        if (mHandler != null && mTimeAnimator != null) {
            mHandler.post(mCancelRunnable);
        }
    }
}