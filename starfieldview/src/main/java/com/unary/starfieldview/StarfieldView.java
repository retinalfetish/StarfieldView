package com.unary.starfieldview;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * A styleable widget that recreates the classic radial starfield effect. It features a number of
 * customizations for the animation and effects.
 *
 * <p><strong>XML attributes</strong></p>
 * <p>The following optional attributes can be used to change the look and feel of the view:</p>
 * <pre>
 *   app:starAlpha="float"    // How quickly the star trails fade
 *   app:starColor="color"    // A simple color or reference
 *   app:starCount="integer"  // Default number of stars is 2000
 *   app:starSize="dimension" // Seed value used for size. Default is "2dp"
 *   app:starSpeed="float"    // Rate of starfield movement (+/-)
 * </pre>
 * <p>See {@link R.styleable#StarfieldView StarfieldView Attributes}, {@link R.styleable#View View Attributes}</p>
 */
public class StarfieldView extends AnimatorView {

    private static final float VIEW_WIDTH = 256; // dp
    private static final float VIEW_HEIGHT = 256; // dp
    private static final float STAR_ALPHA = 0.5f;
    @ColorInt
    private static final int STAR_COLOR = 0x8AFFFFFF;
    private static final int STAR_COUNT = 2000;
    private static final float STAR_SIZE = 2; // dp
    private static final float STAR_SPEED = 8;
    private static final long DELTA_TIME = 20;

    private float mStarAlpha;
    private int mStarCount;
    private float mStarSize;
    private float mStarSpeed;
    private Paint mStarPaint;
    private Rect mDrawingRect;
    private Canvas mStarCanvas;
    private Bitmap mCanvasBitmap;
    private Bitmap mBufferBitmap;
    private CircularStarfield mStarField;
    private long mDeltaTime;

    /**
     * Simple constructor to use when creating the view from code.
     *
     * @param context Context given for the view. This determines the resources and theme.
     */
    public StarfieldView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    /**
     * Constructor that is called when inflating the view from XML.
     *
     * @param context Context given for the view. This determines the resources and theme.
     * @param attrs   The attributes for the inflated XML tag.
     */
    public StarfieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    /**
     * Constructor called when inflating from XML and applying a style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     */
    public StarfieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    /**
     * Constructor that is used when given a default shared style.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     * @param defStyleRes  Default style resource to apply to this view.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StarfieldView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Shared method to initialize the member variables from the XML and create the drawing objects.
     * Input values are checked for sanity.
     *
     * @param context      Context given for the view. This determines the resources and theme.
     * @param attrs        The attributes for the inflated XML tag.
     * @param defStyleAttr Default style attributes to apply to this view.
     * @param defStyleRes  Default style resource to apply to this view.
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray typedArray = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.StarfieldView, defStyleAttr, defStyleRes);

        int starColor;

        try {
            mStarAlpha = typedArray.getFloat(R.styleable.StarfieldView_starAlpha, STAR_ALPHA);
            starColor = typedArray.getColor(R.styleable.StarfieldView_starColor, STAR_COLOR);
            mStarCount = typedArray.getInt(R.styleable.StarfieldView_starCount, STAR_COUNT);
            mStarSize = typedArray.getDimension(R.styleable.StarfieldView_starSize, dpToPixels(context, STAR_SIZE));
            mStarSpeed = typedArray.getFloat(R.styleable.StarfieldView_starSpeed, STAR_SPEED);
        } finally {
            typedArray.recycle();
        }

        // Initialize drawing objects
        mStarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStarPaint.setColor(starColor);

        mDrawingRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return Math.max(super.getSuggestedMinimumWidth(), dpToPixels(getContext(), VIEW_WIDTH));
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return Math.max(super.getSuggestedMinimumHeight(), dpToPixels(getContext(), VIEW_HEIGHT));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int paddingStart = getPaddingLeft();
        int paddingEnd = getPaddingRight();

        // Use RTL if available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (isLtrLayout(this)) {
                paddingStart = getPaddingStart();
                paddingEnd = getPaddingEnd();
            } else {
                paddingStart = getPaddingEnd();
                paddingEnd = getPaddingStart();
            }
        }

        // Similar to getDrawingRect()
        mDrawingRect.set(paddingStart, getPaddingTop(),
                getWidth() - paddingEnd, getHeight() - getPaddingBottom());

        if (mDrawingRect.width() < 1 || mDrawingRect.height() < 1) return;

        // Allocate here for padding
        mCanvasBitmap = Bitmap.createBitmap(mDrawingRect.width(), mDrawingRect.height(), Bitmap.Config.ARGB_8888);
        mStarCanvas = new Canvas(mCanvasBitmap);

        // Don't block the UI with this
        new Thread(new Runnable() {
            @Override
            public void run() {
                mStarField = new CircularStarfield(mDrawingRect.width(), mDrawingRect.height(), mStarCount, mStarSize, mStarPaint);
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBufferBitmap != null) {
            canvas.drawBitmap(mBufferBitmap, mDrawingRect.left, mDrawingRect.top, null);
        }
    }

    @Override
    public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
        mDeltaTime += deltaTime;

        if (mStarField == null || mDeltaTime < DELTA_TIME) return;

        mDeltaTime = 0;

        // Fade out the star trails
        mStarCanvas.drawColor((int) (mStarAlpha * 255) << 24, PorterDuff.Mode.DST_OUT);
        mStarField.draw(mStarCanvas, mStarSpeed);

        mBufferBitmap = Bitmap.createBitmap(mCanvasBitmap);

        if (getHandler() != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    /**
     * Check if the layout direction for the given view or configuration is left-to-right.
     *
     * @param view View to check.
     * @return True if likely LTR.
     */
    protected static boolean isLtrLayout(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (view.isLayoutDirectionResolved()) {
                return view.getLayoutDirection() == LAYOUT_DIRECTION_LTR;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return view.getResources().getConfiguration().getLayoutDirection() == LAYOUT_DIRECTION_LTR;
        }

        return true;
    }

    /**
     * Utility method to find the pixel resolution of a density pixel (dp) value.
     *
     * @param context Context given for the metrics.
     * @param dp      Density pixels to convert.
     * @return The pixel resolution.
     */
    private static int dpToPixels(@NonNull Context context, @Dimension float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * Utility method to find the density pixel (dp) value of a pixel resolution.
     *
     * @param context Context given for the metrics.
     * @param px      Pixel resolution to convert.
     * @return The density pixels.
     */
    @Dimension
    private static float pixelsToDp(@NonNull Context context, int px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    /**
     * Utility method to find the preferred measurements of this view for the view parent.
     *
     * @param defaultSize Default size of the view.
     * @param measureSpec Constraints imposed by the parent.
     * @return Preferred size for this view.
     * @see View#getDefaultSize(int, int)
     */
    public static int getDefaultSize(int defaultSize, int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.AT_MOST:
                return Math.min(size, defaultSize);
            case MeasureSpec.UNSPECIFIED:
            default:
                return defaultSize;
        }
    }

    /**
     * Get the opacity used for the trailing star effect. The range is from 0 to 1 for none.
     *
     * @return Star trail alpha.
     */
    public float getStarAlpha() {
        return mStarAlpha;
    }

    /**
     * Get the opacity used for the trailing star effect. The range is from 0 to 1 for none.
     *
     * @param starAlpha Star trail alpha.
     */
    public void setStarAlpha(float starAlpha) {
        mStarAlpha = starAlpha;
    }

    /**
     * Get the star color. This gets the equivalent property in the star paint object.
     *
     * @return Star color used.
     */
    @ColorInt
    public int getStarColor() {
        return mStarPaint.getColor();
    }

    /**
     * Set the star color. This sets the equivalent property in the star paint object.
     *
     * @param color Star color used.
     */
    public void setStarColor(@ColorInt int color) {
        mStarPaint.setColor(color);
    }

    /**
     * Get the number of stars created for the starfield effect. This eats resources.
     *
     * @return Number of stars.
     */
    public int getStarCount() {
        return mStarCount;
    }

    /**
     * Set the number of stars created for the starfield effect. This eats resources.
     *
     * @param starCount Number of stars.
     */
    public void setStarCount(int starCount) {
        mStarCount = starCount;
        requestLayout();
    }

    /**
     * Get the value used to determine largest possible size when generating stars.
     *
     * @return Maximum star size.
     */
    public float getStarSize() {
        return mStarSize;
    }

    /**
     * Set the value used to determine largest possible size when generating stars.
     *
     * @param starSize Maximum star size.
     */
    public void setStarSize(float starSize) {
        mStarSize = starSize;
        requestLayout();
    }

    /**
     * Get the rate of speed used when animating the starfield. This can be a negative number.
     *
     * @return Rate of speed.
     */
    public float getStarSpeed() {
        return mStarSpeed;
    }

    /**
     * Set the rate of speed used when animating the starfield. This can be a negative number.
     *
     * @param starSpeed Rate of speed.
     */
    public void setStarSpeed(float starSpeed) {
        mStarSpeed = starSpeed;
    }

    /**
     * Get the star paint. It can be used to get other properties not available directly.
     *
     * @return Paint used for stars.
     */
    public Paint getStarPaint() {
        return mStarPaint;
    }

    /**
     * Set the star paint. It can be used to set other properties not available directly.
     *
     * @param starPaint Paint used for stars.
     */
    public void setStarPaint(Paint starPaint) {
        mStarPaint = starPaint;
        requestLayout();
    }
}