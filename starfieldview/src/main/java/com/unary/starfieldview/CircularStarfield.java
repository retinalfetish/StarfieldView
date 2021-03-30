package com.unary.starfieldview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import androidx.annotation.NonNull;

/**
 * Create and animate a cluster of stars in a radial starfield. This moves each star based on how
 * close it appears in the field.
 */
public class CircularStarfield {

    private int mCx;
    private int mCy;
    private float mRadius;
    private float mSize;
    private CircularStar[] mStars;
    private Paint mPaint;

    /**
     * Constructor to create a random cluster of stars for the given width and height.
     *
     * @param width  The canvas width.
     * @param height The canvas height.
     * @param count  Number of stars to generate.
     * @param size   Star size.
     * @param paint  Paint to draw with.
     */
    public CircularStarfield(int width, int height, int count, float size, @NonNull Paint paint) {
        mCx = width / 2;
        mCy = height / 2;
        mRadius = getOuterRadius(width, height);
        mSize = size;

        mStars = new CircularStar[count];

        for (int i = 0; i < mStars.length; i++) {
            mStars[i] = generate(new CircularStar());
        }

        mPaint = paint;
    }

    /**
     * Draw a starfield on the given canvas while advancing the stars forward radially.
     *
     * @param canvas Canvas to draw on.
     * @param speed  Rate of speed.
     */
    public void draw(@NonNull Canvas canvas, float speed) {
        for (int i = 0; i < mStars.length; i++) {
            CircularStar star = mStars[i];
            PointF point = getXYPoint(star.point, star.theta);

            canvas.drawCircle(point.x + mCx, point.y + mCy, star.radius, mPaint);

            star.point += star.delta * speed;
            star.radius = star.delta * star.point / mRadius * mSize;

            if (star.point < 0 || star.point > mRadius) {
                generate(star);
            }
        }
    }

    /**
     * (Re)initialize an individual star with a random starting point and radius.
     *
     * @param star The given star.
     * @return A star reborn.
     */
    private CircularStar generate(@NonNull CircularStar star) {
        star.point = (float) (mRadius * Math.sqrt(Math.random()));
        star.theta = (float) (2 * Math.PI * Math.random());
        star.delta = (float) Math.sqrt(Math.random());
        star.radius = star.delta * star.point / mRadius * mSize;

        return star;
    }

    /**
     * Utility method to find the outer radius of a given rectangle.
     *
     * @param width  The width.
     * @param height The height
     * @return Outer radius.
     */
    protected static float getOuterRadius(int width, int height) {
        // r = sqrt(w^2 + h^2) / 2
        return (float) (Math.sqrt(width * width + height * height) / 2);
    }

    /**
     * Utility method to find the coordinates of a point along an angle.
     *
     * @param point Point on the angle.
     * @param angle Angle in radians.
     * @return The X and Y axis.
     */
    protected static PointF getXYPoint(float point, float angle) {
        // x = r * cos(t), y = r * sin(t)
        return new PointF((float) (point * Math.cos(angle)), (float) (point * Math.sin(angle)));
    }
}