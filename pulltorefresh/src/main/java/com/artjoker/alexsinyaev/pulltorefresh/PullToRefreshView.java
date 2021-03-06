package com.artjoker.alexsinyaev.pulltorefresh;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.artjoker.alexsinyaev.pulltorefresh.drawable.BaseDrawable;
import com.artjoker.alexsinyaev.pulltorefresh.utils.Constants;
import com.artjoker.alexsinyaev.pulltorefresh.utils.MathUtils;
import com.artjoker.alexsinyaev.pulltorefresh.utils.ViewSize;
import com.artjoker.alexsinyaev.pulltorefresh.utils.ViewUtils;


/**
 * Created by dev on 13.09.16.
 */
public class PullToRefreshView extends ViewGroup  {


    private static int DRAG_MAX_DISTANCE;
    private View mTarget;
    private ImageView mRefreshView;
    private Interpolator mDecelerateInterpolator;
    private int mTouchSlop;
    private int mTotalDragDistance;

    private float mCurrentDragPercent;
    private int mCurrentOffsetTop;
    private boolean mRefreshing;
    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private float mInitialMotionY;
    private int mFrom;
    private float mFromDragPercent;
    private boolean mNotify;
    private OnRefreshListener mListener;

    private int mTargetPaddingTop;
    private int mTargetPaddingBottom;
    private int mTargetPaddingRight;
    private int mTargetPaddingLeft;
    private BaseDrawable animatedDrawable;
    private ViewSize viewSize;

    public PullToRefreshView(Context context) {
        this(context, null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDecelerateInterpolator = new DecelerateInterpolator(Constants.DECELERATE_INTERPOLATION_FACTOR);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        DRAG_MAX_DISTANCE = height / 4;
        mTotalDragDistance = DRAG_MAX_DISTANCE;

        mRefreshView = new ImageView(context);


        animatedDrawable = new BaseDrawable(mRefreshView, this, DRAG_MAX_DISTANCE);
        mRefreshView.setImageDrawable(animatedDrawable);
        addView(mRefreshView);

        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
        viewSize = new ViewSize();
    }




    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTarget();
        if (mTarget == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    private void ensureTarget() {
        if (mTarget != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mRefreshView) {
                    mTarget = child;
                    mTargetPaddingBottom = mTarget.getPaddingBottom();
                    mTargetPaddingLeft = mTarget.getPaddingLeft();
                    mTargetPaddingRight = mTarget.getPaddingRight();
                    mTargetPaddingTop = mTarget.getPaddingTop();
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!isEnabled() || ViewUtils.canChildScrollUp(mTarget) || mRefreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == Constants.INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = Constants.INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }

                final float scrollTop = MathUtils.getScrollTop(ev, pointerIndex, mInitialMotionY);
                mCurrentDragPercent = MathUtils.getDragPercent(scrollTop, mTotalDragDistance);
                if (mCurrentDragPercent < 0) {
                    return false;
                }
                int offset = getOffset(scrollTop);

                animatedDrawable.setCurrentOffsetTopInPercent(mCurrentDragPercent);
                animatedDrawable.invalidateSelf();

                setTargetOffsetTop(offset, true);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = ev.getPointerId(index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == Constants.INVALID_POINTER) {
                    return false;
                }
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                final float y = ev.getY(pointerIndex);
                final float overScrollTop = MathUtils.getOverScrollTop(y, mInitialMotionY, Constants.DRAG_RATE);
                mIsBeingDragged = false;
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    mRefreshing = false;
                    animateOffsetToStartPosition();
                }
                mActivePointerId = Constants.INVALID_POINTER;
                return false;
            }
        }

        return true;
    }


    private int getOffset(float scrollTop) {
        float boundedDragPercent = MathUtils.getBoundedDragPercent(mCurrentDragPercent);
        float extraOS = MathUtils.getExtraOS(scrollTop, mTotalDragDistance);

        float tensionSlingshotPercent = MathUtils.getTensionSlingPercent(extraOS, mTotalDragDistance);
        float tensionPercent = MathUtils.getTensionPercent(tensionSlingshotPercent);
        float extraMove = MathUtils.getExtraMove(mTotalDragDistance, tensionPercent);
        int targetY = MathUtils.getTargetY(boundedDragPercent, mTotalDragDistance, extraMove);


        return targetY - mCurrentOffsetTop;
    }


    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math.abs((long) (Constants.MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(Constants.MAX_OFFSET_ANIMATION_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        mRefreshView.clearAnimation();
        mRefreshView.startAnimation(mAnimateToCorrectPosition);

        if (mRefreshing) {

            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
        } else {

            animateOffsetToStartPosition();
        }
        mCurrentOffsetTop = mTarget.getTop();
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = mTotalDragDistance;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
            animatedDrawable.setCurrentOffsetTopInPercent(mCurrentDragPercent);

            setTargetOffsetTop(offset, false /* requires update */);
        }
    };

    private void moveToStart(float interpolatedTime) {
        int targetTop = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        int offset = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;

        animatedDrawable.setCurrentOffsetTopInPercent(mCurrentDragPercent);
        animatedDrawable.invalidateSelf();

        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop);
        setTargetOffsetTop(offset, false);
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false );
        }
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {

                animatedDrawable.setCurrentOffsetTopInPercent(1);
                animatedDrawable.invalidateSelf();

                animateOffsetToCorrectPosition();
            } else {
                animateOffsetToStartPosition();
            }
        }
    }

    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {

            mCurrentOffsetTop = mTarget.getTop();
        }
    };

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = ev.findPointerIndex(activePointerId);
        if (index < 0) {
            return -1;
        }
        return ev.getY(index);
    }

    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
        mTarget.offsetTopAndBottom(offset);

        mCurrentOffsetTop = mTarget.getTop();
        if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        ensureTarget();
        if (mTarget == null)
            return;

        viewSize = layoutViewSize(viewSize);
        onLayoutTarget(viewSize);
        onLayoutRefreshView(viewSize);
    }

    private void onLayoutRefreshView(ViewSize viewSize) {
        int refreshLeft = viewSize.getLeft();
        int refreshTop = viewSize.getTop();
        int refreshRight = viewSize.getLeft() + viewSize.getWidth() - viewSize.getRight();
        int refreshBottom = viewSize.getTop() + viewSize.getHeight() - viewSize.getBottom();
        mRefreshView.layout(refreshLeft, refreshTop, refreshRight, refreshBottom);
    }

    private void onLayoutTarget(ViewSize viewSize) {
        int targetCurrentTop = viewSize.getTop() + mCurrentOffsetTop;
        int targetRight = viewSize.getLeft() + viewSize.getWidth() - viewSize.getRight();
        int targetCurrentBottom = viewSize.getTop() + viewSize.getHeight() - viewSize.getBottom() + mCurrentOffsetTop;
        int targetLeft = viewSize.getLeft();
        mTarget.layout(targetLeft, targetCurrentTop, targetRight, targetCurrentBottom);
    }

    private ViewSize layoutViewSize(ViewSize viewSize) {
        viewSize.setHeight(getMeasuredHeight());
        viewSize.setWidth(getMeasuredWidth());
        viewSize.setLeft(getPaddingLeft());
        viewSize.setTop(getPaddingTop());
        viewSize.setRight(getPaddingRight());
        viewSize.setBottom(getPaddingBottom());
        return viewSize;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}