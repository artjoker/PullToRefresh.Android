package com.artjoker.alexsinyaev.pulltorefresh.utils;

import android.support.annotation.VisibleForTesting;

/**
 * Created by dev on 15.09.16.
 */
public interface Constants {
    @VisibleForTesting
    int CIRCLE_DIAMETER = 40;
    @VisibleForTesting
    int CIRCLE_DIAMETER_LARGE = 56;
    int MAX_ALPHA = 255;
    int SCALE_DOWN_DURATION = 150;
    int ANIMATE_TO_TRIGGER_DURATION = 200;
    int ANIMATE_TO_START_DURATION = 200;
    // Default offset in dips from the top of the view to where the progress spinner should stop
    int DEFAULT_CIRCLE_TARGET = 64;
    float DECELERATE_INTERPOLATION_FACTOR = 2f;
    int INVALID_POINTER = -1;
    float DRAG_RATE = .5f;
}
