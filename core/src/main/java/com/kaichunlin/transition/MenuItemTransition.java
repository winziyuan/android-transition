package com.kaichunlin.transition;

import android.app.Activity;
import android.support.annotation.CheckResult;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.kaichunlin.transition.internal.TransitionController;
import com.kaichunlin.transition.internal.TransitionControllerManager;
import com.kaichunlin.transition.util.TransitionStateLogger;
import com.kaichunlin.transition.util.TransitionUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides transition support to a MenuItem
 * <p>
 * Created by Kai-Chun Lin on 2015/4/18.
 */
public class MenuItemTransition extends AbstractTransition<MenuItemTransition, MenuItemTransition.Setup> {
    private List<TransitionControllerManager> mTransittingMenuItems = new ArrayList<>();
    private final Toolbar mToolbar;
    private boolean mStarted;
    private boolean mSetVisibleOnStartTransition;
    private boolean mInvalidateOptionOnStopTransition;
    private WeakReference<Activity> mActivityRef;
    private int mMenuId;

    public MenuItemTransition(@NonNull Toolbar toolbar) {
        this(null, toolbar, null);
    }

    public MenuItemTransition(@Nullable String id, @NonNull Toolbar toolbar) {
        this(id, toolbar, null);
    }

    public MenuItemTransition(@NonNull Toolbar toolbar, @Nullable View view) {
        this(null, toolbar, view);
    }

    public MenuItemTransition(@Nullable String id, @NonNull Toolbar toolbar, @Nullable View view) {
        super(id);
        this.mToolbar = toolbar;
        this.mTarget = view;
    }

    public MenuItemTransition setMenuId(@IdRes int menuId) {
        mMenuId = menuId;
        return self();
    }

    public int getMenuId() {
        return mMenuId;
    }

    /**
     * @param setVisibleOnStartAnimation should the visibility of the target MenuItem be forcibly set when {@link #startTransition()} is called
     * @return
     */
    public MenuItemTransition setVisibleOnStartAnimation(boolean setVisibleOnStartAnimation) {
        this.mSetVisibleOnStartTransition = setVisibleOnStartAnimation;
        return self();
    }

    @Override
    public MenuItemTransition reverse() {
        super.reverse();
        mTransittingMenuItems.clear();
        return self();
    }

    @Override
    public boolean startTransition() {
        if (mStarted) {
            return false;
        }
        if (mSetVisibleOnStartTransition) {
            mToolbar.getMenu().setGroupVisible(0, true);
        }
        if (mTransittingMenuItems.size() == 0) {
            List<MenuItem> list;
            if (mMenuId == 0) {
                list = TransitionUtil.getVisibleMenuItemList(mToolbar);
            } else { //only apply to a specific MenuItem
                list = new ArrayList<>();
                MenuItem menuItem = TransitionUtil.getMenuItem(mToolbar, mMenuId);
                if (menuItem == null) {
                    return false;
                }
                list.add(menuItem);
            }
            LayoutInflater layoutInflater = LayoutInflater.from(mToolbar.getContext());
            for (int i = 0; i < list.size(); i++) {
                MenuItem menuItem = list.get(i);
                TransitionControllerManager transitionControllerManager = new TransitionControllerManager(getId());
                if (mInterpolator != null) {
                    transitionControllerManager.setInterpolator(mInterpolator);
                }
                mSetup.setupAnimation(menuItem, transitionControllerManager, i, list.size());
                View view;
                if (mTarget == null) {
                    view = layoutInflater.inflate(R.layout.menu_animation, null).findViewById(R.id.menu_animation);
                    ((ImageView) view).setImageDrawable(menuItem.getIcon());
                } else {
                    view = mTarget;
                }
                if (TransitionConfig.isDebug()) {
                    view.setTag(R.id.debug_id, new TransitionStateLogger(getId()));
                }
                if (mReverse) {
                    transitionControllerManager.reverse();
                }
                menuItem.setActionView(view);
                transitionControllerManager.setTarget(view);
                transitionControllerManager.start();
                mTransittingMenuItems.add(transitionControllerManager);
            }
        }
        mStarted = true;
        return true;
    }

    @Override
    public void updateProgress(float progress) {
        final int size = mTransittingMenuItems.size();
        for (int i = 0; i < size; i++) {
            mTransittingMenuItems.get(i).updateProgress(progress);
        }
    }

    @Override
    public void stopTransition() {
        int size = mTransittingMenuItems.size();
        for (int i = 0; i < size; i++) {
            mTransittingMenuItems.get(i).end();
        }
        List<MenuItem> list = TransitionUtil.getVisibleMenuItemList(mToolbar);
        size = list.size();
        for (int i = 0; i < size; i++) {
            list.get(i).setActionView(null);
        }
        mTransittingMenuItems.clear();
        if (mInvalidateOptionOnStopTransition) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
        mStarted = false;
    }

    private Activity getActivity() {
        return mActivityRef == null ? null : mActivityRef.get();
    }

    /**
     * @return should {@link Activity#invalidateOptionsMenu()} be called when transition stops
     */
    public boolean isInvalidateOptionOnStopTransition() {
        return mInvalidateOptionOnStopTransition;
    }

    /**
     * Sets whether or not to call {@link Activity#invalidateOptionsMenu()} after a transition stops.
     *
     * @param activity                        Activity that should have its {@link Activity#invalidateOptionsMenu()} method called, or null if invalidateOptionOnStopAnimation parameter is false
     * @param invalidateOptionOnStopAnimation
     * @return
     */
    public MenuItemTransition setInvalidateOptionOnStopTransition(@NonNull Activity activity, boolean invalidateOptionOnStopAnimation) {
        this.mActivityRef = new WeakReference<>(activity);
        this.mInvalidateOptionOnStopTransition = invalidateOptionOnStopAnimation;
        return self();
    }

    @CheckResult
    @Override
    public MenuItemTransition clone() {
        MenuItemTransition newCopy = (MenuItemTransition) super.clone();
        newCopy.mTransittingMenuItems = new ArrayList<>();
        final int size = mTransittingMenuItems.size();
        for (int i = 0; i < size; i++) {
            newCopy.mTransittingMenuItems.add(mTransittingMenuItems.get(i).clone());
        }
        return newCopy;
    }

    @Override
    protected void invalidate() {

    }

    @Override
    protected MenuItemTransition self() {
        return this;
    }

    /**
     * Represents an object that will create {@link TransitionController} Objects to be added to a {@link TransitionControllerManager}
     */
    public interface Setup extends AbstractTransition.Setup {
        /**
         * Create one or more {@link TransitionController} for each {@link android.view.MenuItem} and add them to mTransitionManager
         *
         * @param mMenuItem
         * @param transitionControllerManager
         * @param itemIndex
         * @param menuCount
         */
        void setupAnimation(MenuItem mMenuItem, TransitionControllerManager transitionControllerManager, int itemIndex, int menuCount);
    }
}
