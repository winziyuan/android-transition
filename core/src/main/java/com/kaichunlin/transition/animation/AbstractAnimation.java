package com.kaichunlin.transition.animation;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.UiThread;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Kai on 2015/7/12.
 */
public abstract class AbstractAnimation implements Animation {
    protected final Runnable mStartAnimation = new Runnable() {
        @Override
        public void run() {
            startAnimation();
        }
    };

    private final Set<AnimationListener> mAnimationListenerSet = new HashSet<>();
    protected Handler mHandler;
    private int mDuration = -1;
    private boolean mReverse;
    private boolean mAnimating;

    /**
     * @param animationListener
     */
    public void addAnimationListener(AnimationListener animationListener) {
        mAnimationListenerSet.add(animationListener);
    }

    /**
     * @param animationListener
     */
    public void removeAnimationListener(AnimationListener animationListener) {
        mAnimationListenerSet.remove(animationListener);
    }

    public void setDuration(@IntRange(from = 0) int duration) {
        mDuration = duration;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setReverseAnimation(boolean reverse) {
        mReverse = reverse;
    }

    /**
     * @return
     */
    public boolean isReverseAnimation() {
        return mReverse;
    }

    /**
     * Starts the animation with the default duration (300 ms)
     */
    @UiThread
    public abstract void startAnimation();

    /**
     * Starts the animation with the specified duration
     *
     * @param duration
     */
    @UiThread
    public abstract void startAnimation(@IntRange(from = 0) int duration);

    @Override
    public void startAnimationDelayed(@IntRange(from = 0) int delay) {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(mStartAnimation, delay);
    }

    public void startAnimationDelayed(@IntRange(from = 0) final int duration, @IntRange(from = 0) int delay) {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startAnimation(duration);
            }
        }, delay);
    }

    protected void setAnimating(boolean animating) {
        mAnimating = animating;
    }

    public boolean isAnimating() {
        return mAnimating;
    }

    protected void notifyAnimationStart() {
        for (AnimationListener listener : mAnimationListenerSet) {
            listener.onAnimationStart(this);
        }
    }

    protected void notifyAnimationEnd() {
        for (AnimationListener listener : mAnimationListenerSet) {
            listener.onAnimationEnd(this);
        }
    }

    protected void notifyAnimationCancel() {
        for (AnimationListener listener : mAnimationListenerSet) {
            listener.onAnimationCancel(this);
        }
    }

    protected void notifyAnimationReset() {
        for (AnimationListener listener : mAnimationListenerSet) {
            listener.onAnimationReset(this);
        }
    }
}