package com.artjoker.alexsinyaev.pulltorefresh.utils;

import android.support.annotation.NonNull;
import android.view.MotionEvent;

/**
 * Created by dev on 16.09.16.
 */
public class MathUtils {

    public static int getTargetY(float boundedDragPercent, float slingshotDist, float extraMove) {
        return (int) ((slingshotDist * boundedDragPercent) + extraMove);
    }

    public static float getExtraMove(float slingshotDist, float tensionPercent) {
        return (slingshotDist) * tensionPercent / 2;
    }

    public static float getTensionPercent(float tensionSlingshotPercent) {
        return (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
    }

    public static float getTensionSlingPercent(float extraOS, float slingshotDist) {
        return Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
    }

    public static float getExtraOS(float scrollTop, int mTotalDragDistance) {
        return Math.abs(scrollTop) - mTotalDragDistance;
    }

    public static float getBoundedDragPercent(float mCurrentDragPercent) {
        return Math.min(1f, Math.abs(mCurrentDragPercent));
    }

    public static float getScrollTop(@NonNull MotionEvent ev, int pointerIndex, float mInitialMotionY) {
        final float y = ev.getY(pointerIndex);
        final float yDiff = y - mInitialMotionY;
        return yDiff * Constants.DRAG_RATE;
    }
    public static float getDragPercent(float scrollTop, int mTotalDragDistance) {
        return scrollTop / mTotalDragDistance;
    }
    public static float getOverScrollTop(float y, float mInitialMotionY, float dragRate) {
        return (y - mInitialMotionY) * dragRate;
    }
}
