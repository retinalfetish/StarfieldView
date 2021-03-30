package com.unary.starfieldview;

import android.animation.TimeAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.RequiresApi;

/**
 * Abstract class for the animator view. This places the animator and callback on a new thread.
 * Implementations should post results to the UI thread.
 */
public abstract class AnimatorView extends View implements TimeAnimator.TimeListener {

    private AnimatorThread mAnimatorThread;

    /**
     * Simple constructor to use when creating the view from code.
     *
     * @param context Context given for the view. This determines the resources and theme.
     */
    public AnimatorView(Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context Context given for the view. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public AnimatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor called when inflating from XML and applying a style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    public AnimatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Constructor that is used when given a default shared style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     * @param defStyleRes  Default style resource to apply to this view.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AnimatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAnimatorThread = new AnimatorThread(this);
        mAnimatorThread.start();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if (visibility == VISIBLE) {
            mAnimatorThread.startAnimator();
        } else {
            mAnimatorThread.cancelAnimator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAnimatorThread.interrupt();
    }

    /**
     * Start the animator.
     */
    public void start() {
        mAnimatorThread.startAnimator();
    }

    /**
     * Stop the animator.
     */
    public void stop() {
        mAnimatorThread.cancelAnimator();
    }
}