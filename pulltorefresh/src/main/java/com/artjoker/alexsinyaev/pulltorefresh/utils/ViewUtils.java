package com.artjoker.alexsinyaev.pulltorefresh.utils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.artjoker.alexsinyaev.pulltorefresh.views.PullingChildView;

/**
 * Created by dev on 14.09.16.
 */
public class ViewUtils {

    public static View ensureTarget(ViewGroup parent) {

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (!isNestedRefreshView(child)) {
                return child;
            }
        }
        return null;
    }

    public static boolean isNestedRefreshView(View child) {
        return child instanceof PullingChildView;
    }
    public static int convertDpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    public static boolean canChildScrollUp(View mTarget) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }
}
