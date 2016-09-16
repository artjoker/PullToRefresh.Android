package com.artjoker.alexsinyaev.pulltorefresh.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.artjoker.alexsinyaev.pulltorefresh.PullToRefreshView;
import com.artjoker.alexsinyaev.pulltorefresh.R;
import com.artjoker.alexsinyaev.pulltorefresh.utils.Constants;
import com.artjoker.alexsinyaev.pulltorefresh.utils.ViewUtils;
import com.artjoker.alexsinyaev.pulltorefresh.views.ChildImageView;

/**
 * Created by dev on 15.09.16.
 */
public class BaseDrawable extends Drawable {

    private float percent;
    private ChildImageView container;
    private PullToRefreshView pullToRefreshView;
    private final Context context;
    private final int backgroundMaxSize;
    private final Paint paint;
    private int width;
    private Bitmap backBitmap;
    private Bitmap sunBitmap;
    private float sunRotateDegree = 0;

    public BaseDrawable(final ChildImageView container, PullToRefreshView pullToRefreshView) {
        super();
        this.container = container;
        this.pullToRefreshView = pullToRefreshView;
        context = container.getContext();
        backgroundMaxSize = ViewUtils.convertDpToPx(context, Constants.DRAG_MAX_DISTANCE);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        initSize(container);

        // paint.setStrokeWidth();

    }

    private void initBitmap() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        backBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.back, options);
        backBitmap = Bitmap.createScaledBitmap(backBitmap, width, backgroundMaxSize, true);

        sunBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sun, options);
        sunBitmap = Bitmap.createScaledBitmap(sunBitmap, width / 4, width / 4, true);
    }

    private void initSize(final View parent) {
        parent.post(new Runnable() {
            @Override
            public void run() {
                width = parent.getMeasuredWidth();
                initBitmap();
            }
        });
    }

    @Override
    public void draw(Canvas canvas) {

        drawBackground(canvas);


        Matrix matrix = new Matrix();
        matrix.reset();
        float dragPercent = percent;
        float offsetX = width - width / 4;
        float offsetY = dragPercent * pullToRefreshView.getTotalDragDistance() - backgroundMaxSize;
        matrix.postTranslate(offsetX, offsetY);


        float scale = 0.8f + 0.2f * percent;
        //  matrix.postScale(scale, scale);
        sunRotateDegree = sunRotateDegree + 2;
        float px = offsetX + sunBitmap.getWidth() / 2;
        float py = offsetY + sunBitmap.getHeight() / 2;
        //matrix.postTranslate(-sunBitmap.getWidth() / 2, -sunBitmap.getHeight() / 2);
        matrix.postRotate(sunRotateDegree, px, py);
        // matrix.postTranslate(px, py);
        canvas.drawBitmap(sunBitmap, matrix, null);
        if (percent > 0.1) {
            invalidateSelf();
        }
    }

    private void drawBackground(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.reset();
        float dragPercent = percent;
        float offsetX = 0;
        float offsetY = dragPercent * pullToRefreshView.getTotalDragDistance() - backgroundMaxSize;
        matrix.postTranslate(offsetX, offsetY);


        float scale = 0.8f + 0.2f * percent;
        matrix.postScale(scale, scale);

        canvas.drawBitmap(backBitmap, matrix, null);
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

    public void setCurrentOffsetTopInPercent(float percent) {
        if (percent > 1)
            percent = 1;
        this.percent = percent;
        Log.e("!!!", "setCurrentOffsetTopInPercent: " + percent);
    }
}
