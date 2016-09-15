package com.artjoker.alexsinyaev.pulltorefresh.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

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
}
