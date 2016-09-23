package com.artjoker.alexsinyaev.pulltorefresh.utils;

import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by Alex Sinyaev  on 14.09.16.
 */
public class ViewUtils {


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
