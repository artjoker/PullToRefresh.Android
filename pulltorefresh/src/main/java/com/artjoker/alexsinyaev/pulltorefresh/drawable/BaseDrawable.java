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
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;

import com.artjoker.alexsinyaev.pulltorefresh.PullToRefreshView;
import com.artjoker.alexsinyaev.pulltorefresh.R;
import com.artjoker.alexsinyaev.pulltorefresh.utils.Constants;
import com.artjoker.alexsinyaev.pulltorefresh.utils.ViewUtils;
import com.artjoker.alexsinyaev.pulltorefresh.views.ChildImageView;

/**
 * Created by dev on 15.09.16.
 */
public class BaseDrawable extends Drawable {

    public static final int BACKGROUND_SPEED = -4;
    public static final int ROTATE_WHEEL_SPEED = 4;
    private float percent;
    private ChildImageView container;
    private PullToRefreshView pullToRefreshView;
    private final Context context;
    private final int backgroundMaxSize;
    private final Paint paint;
    private final Paint fadePaint;
    private int width;
    private Bitmap backBitmapFirst;
    // private Bitmap sunBitmap;
    private float wheelRotateDegree = 0;
    private Matrix matrix = new Matrix();
    private int loopIndex = 1;
    private Bitmap backBitmapSecond;
    private Bitmap motoBitmap;
    private Bitmap leftWheel;
    private Bitmap rightWheel;
    private Bitmap lightBitmap;


    public BaseDrawable(final ChildImageView container, PullToRefreshView pullToRefreshView) {
        super();
        this.container = container;

        this.pullToRefreshView = pullToRefreshView;
        context = container.getContext();
        backgroundMaxSize = ViewUtils.convertDpToPx(context, Constants.DRAG_MAX_DISTANCE);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);


        fadePaint = new Paint();
        fadePaint.setStyle(Paint.Style.FILL);
        initSize(container);


    }

    private void initBitmap() {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        backBitmapFirst = BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
        int height = backBitmapFirst.getHeight();

        float heightScaleFactor = (float) height / (float) backgroundMaxSize;
        int backWidth = (int) (backBitmapFirst.getWidth() / heightScaleFactor);
        backBitmapFirst = Bitmap.createScaledBitmap(backBitmapFirst, backWidth, backgroundMaxSize, true);
        backBitmapSecond = Bitmap.createBitmap(backBitmapFirst);

        motoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.moto, options);
        int motoScale = 2;
        int motoWidth = (int) (motoBitmap.getWidth() / (heightScaleFactor * motoScale));
        int motoHeight = (int) (motoBitmap.getHeight() / (heightScaleFactor * motoScale));
        motoBitmap = Bitmap.createScaledBitmap(motoBitmap, motoWidth, motoHeight, true);
        Bitmap wheel = BitmapFactory.decodeResource(context.getResources(), R.drawable.wheel, options);

        int wheelWidth = (int) (wheel.getWidth() / (heightScaleFactor * motoScale));
        int wheelHeight = (int) (wheel.getHeight() / (heightScaleFactor * motoScale));
        leftWheel = Bitmap.createScaledBitmap(wheel, wheelWidth, wheelHeight, true);
        rightWheel = Bitmap.createBitmap(leftWheel);

        lightBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.light, options);

        int lightWidth = (int) (lightBitmap.getWidth() / (heightScaleFactor * motoScale));
        int lightHeight = (int) (lightBitmap.getHeight() / (heightScaleFactor * motoScale));
        lightBitmap = Bitmap.createScaledBitmap(lightBitmap, lightWidth, lightHeight, true);

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
    public void draw(@NonNull Canvas canvas) {

        drawBackground(canvas);
        drawMoto(canvas);

        if (percent > 0.1) {
            invalidateSelf();
        }
    }

    private void drawMoto(Canvas canvas) {
        matrix.reset();
        float offsetX = width / 2 - motoBitmap.getWidth() / 2;
        float bottomOffset = backgroundMaxSize / 5 + motoBitmap.getHeight();
        if (percent < 1) {
            float offsetY = percent * pullToRefreshView.getTotalDragDistance() - (backgroundMaxSize * (1 - percent) + bottomOffset);
            matrix.postTranslate(offsetX, offsetY);
            float scale = getScale(percent);
            matrix.postScale(scale, scale);
            canvas.drawBitmap(motoBitmap, matrix, null);
            scaleLeftWheel(canvas, bottomOffset, offsetY, scale);
            scaleRightWheel(canvas, bottomOffset, offsetY, scale);

        } else {
            float offsetY = getDragOffsetY(bottomOffset);
            matrix.postTranslate(offsetX, offsetY);

            canvas.drawBitmap(motoBitmap, matrix, null);

            float bottomWheelOffset = getWheelBottomOffset(bottomOffset);
            drawLeftWheel(canvas, bottomWheelOffset);
            drawRightWheel(canvas, bottomWheelOffset);

            matrix.reset();
            float dx = width / 2 + (int) (motoBitmap.getWidth() / 3);
            float dy = bottomOffset + motoBitmap.getHeight() / 5;
            matrix.postTranslate(dx, dy);
            float speedFactor = 1800;
            float arg = (loopIndex / speedFactor) % 90;
            double sin = Math.cos(Math.toDegrees(arg));

            int minAlpha = 120;
            int alpha = (int) (Math.abs(sin) * 255) + minAlpha;
            Log.e("!!!", "drawMoto: " + alpha);
            fadePaint.setAlpha(alpha);
            canvas.drawBitmap(lightBitmap, matrix, fadePaint);

        }


    }

    private void scaleRightWheel(Canvas canvas, float bottomOffset, float offsetY, float scale) {
        matrix.reset();
        int offsetXRightWheel = getRightWheelOffset();
        matrix.postTranslate(offsetXRightWheel, offsetY + getWheelBottomOffset(bottomOffset));
        matrix.postScale(scale, scale);
        canvas.drawBitmap(rightWheel, matrix, null);
    }

    private void scaleLeftWheel(Canvas canvas, float bottomOffset, float offsetY, float scale) {
        matrix.reset();
        int offsetXleftWheel = getLeftWheelOffset();
        matrix.postTranslate(offsetXleftWheel, offsetY + getWheelBottomOffset(bottomOffset));
        matrix.postScale(scale, scale);
        canvas.drawBitmap(leftWheel, matrix, null);
    }

    private float getWheelBottomOffset(float bottomOffset) {
        return bottomOffset - motoBitmap.getHeight() + leftWheel.getHeight() / 2;
    }

    private int getLeftWheelOffset() {
        return width / 2 - (int) (motoBitmap.getWidth() / 2.5);
    }

    private void drawRightWheel(Canvas canvas, float bottomWheelOffset) {
        matrix.reset();
        int offsetX = getRightWheelOffset();
        float offsetY = getDragOffsetY(bottomWheelOffset);
        matrix.postTranslate(offsetX, offsetY);
        float px = offsetX + leftWheel.getWidth() / 2;
        float py = offsetY + leftWheel.getHeight() / 2;
        matrix.postRotate(wheelRotateDegree, px, py);
        canvas.drawBitmap(rightWheel, matrix, null);
    }

    private int getRightWheelOffset() {
        return width / 2 + (int) (motoBitmap.getWidth() / 3.5);
    }

    private void drawLeftWheel(Canvas canvas, float bottomWheelOffset) {
        matrix.reset();
        int offsetX = getLeftWheelOffset();
        float offsetY = getDragOffsetY(bottomWheelOffset);
        matrix.postTranslate(offsetX, offsetY);
        wheelRotateDegree = wheelRotateDegree + ROTATE_WHEEL_SPEED;
        float px = offsetX + leftWheel.getWidth() / 2;
        float py = offsetY + leftWheel.getHeight() / 2;
        matrix.postRotate(wheelRotateDegree, px, py);
        canvas.drawBitmap(leftWheel, matrix, null);
    }

    private float getDragOffsetY(float bottomOffset) {
        int dragOffset = (int) (Math.sin(loopIndex % 15) * 2);
        return backgroundMaxSize - bottomOffset - dragOffset;
    }

    private float getScale(float percent) {
        return 0.8f + 0.2f * percent;
    }

    private void drawBackground(Canvas canvas) {

        matrix.reset();
        float dragPercent = percent;
        if (dragPercent < 1) {
            loopIndex = 1;
            scaleBackground(canvas, dragPercent);
        } else {
            int backSpeed = BACKGROUND_SPEED;
            loopBackground(canvas, backSpeed);
        }


        loopIndex++;
    }

    private void loopBackground(Canvas canvas, int backSpeed) {
        int backXoffset = backSpeed * loopIndex;

        int dxFirstBack = backXoffset;
        int twoBackWidth = backBitmapFirst.getWidth() + backBitmapSecond.getWidth();
        if (Math.abs(dxFirstBack) >= Math.abs(twoBackWidth - width)) {
            dxFirstBack = backXoffset + twoBackWidth;
            if (Math.abs(backXoffset) >= Math.abs(twoBackWidth)) {
                backXoffset = 0;
                loopIndex = 1;
            }
        }

        matrix.postTranslate(dxFirstBack, 0);
        canvas.drawBitmap(backBitmapFirst, matrix, null);

        matrix.reset();
        int dxSecondBack = backXoffset + backBitmapFirst.getWidth();
        matrix.postTranslate(dxSecondBack, 0);
        canvas.drawBitmap(backBitmapSecond, matrix, null);
    }

    private void scaleBackground(Canvas canvas, float dragPercent) {
        float offsetX = 0;
        float offsetY = dragPercent * pullToRefreshView.getTotalDragDistance() - backgroundMaxSize;
        matrix.postTranslate(offsetX, offsetY);
        float scale = getScale(percent);
        matrix.postScale(scale, scale);
        canvas.drawBitmap(backBitmapFirst, matrix, null);
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

    }
}
