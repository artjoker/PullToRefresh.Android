package com.artjoker.alexsinyaev.pulltorefresh.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by dev on 15.09.16.
 */
public class BaseDrawable extends Drawable {
    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
