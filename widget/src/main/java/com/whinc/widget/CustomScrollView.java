package com.whinc.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/10/21.
 */
public class CustomScrollView extends FrameLayout{
    private static final String TAG = CustomScrollView.class.getSimpleName();
    private static final int DURATION = 500;        // ms
    private static final int DEFAULT_ITEM_WIDTH = 200;
    private static final int DEFAULT_ITEM_HEIGHT = DEFAULT_ITEM_WIDTH * 4 / 3;
    private static final int DEFAULT_ITEM_MARGIN = 10;
    private static final int DEFAULT_ITEM_LARGE_WIDTH = DEFAULT_ITEM_WIDTH * 2;
    private static final int DEFAULT_ITEM_LARGE_HEIGHT = DEFAULT_ITEM_HEIGHT * 2;
    private static final float DEFAULT_SCROLL_FACTOR = 0.5f;
    private static final int DEFAULT_TOUCH_DIFF = 20;
    private static final boolean DEBUG = true;

    /* XML layout attributes */
    /** Width of ScrollView */
    private int mWidth;
    /** Height of ScrollView */
    private int mHeight;
    /** Width of item in ScrollView */
    private int mItemWidth;
    /** Height of item in ScrollView */
    private int mItemHeight;
    /** Spacing between each item in ScrollView */
    private int mItemMargin;
    /** Width of large item in ScrollView */
    private int mItemLargeWidth;
    /** Height of large item in ScrollView */
    private int mItemLargeHeight;
    /** When touch up if the distance the center large item offset center line large then this value,
     * ScrollView will scroll to next item automatically */
    private int mTouchDiff;
    /** Affect the scroll speed, the more large this value scroll more faster */
    private float mScrollFactor;

    private int mItemLargeIndex = -1;
    private Interpolator mInterpolator = null;
    private Context mContext;
    private int mTranslationX = 0;
    private GestureDetector mGestureDetector;
    private ValueAnimator mAnimator;
    private Adapter mAdapter;

    public CustomScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        // remove items
        clearItems();

        if (adapter == null) {
            mAdapter = null;
            return;
        }

        mAdapter = adapter;

        int count = adapter.getCount();
        for (int i = 0; i < count; ++i) {
            View view = adapter.getView(this);
            if (view != null) {
                // Rect in view tag used to recode size and coordinate of view
                view.setTag(new Rect(0, 0, 0, 0));
                addView(view);
            }
        }

        if (isShown()) {
            update();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Measure children
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            Rect rect = (Rect) child.getTag();
            int widthSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY);
            measureChild(child, widthSpec, heightSpec);
        }

        if (mWidth <= 0) {      // store measured size
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();

            if (!isInEditMode()) {
                update();
            }
        }
    }

    /** Called when scroll current view */
    private boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) < 1) {
            return true;
        }

        distanceX *= mScrollFactor;
        mTranslationX -= distanceX;
        float ratio = Math.abs(distanceX / getItemWM());
        int childCount = getChildCount();
        View activeView = getChildAt(mItemLargeIndex);
        Rect rect = (Rect) activeView.getTag();
        int itemCenterX = mTranslationX + rect.left + rect.width()/2;
        int centerLine = mWidth / 2;
        if (itemCenterX < centerLine) {          // locate in left side of CustomScrollView
            if (mItemLargeIndex < childCount - 1) {
                if (distanceX > 0) {            // scroll left
                    // 中间item向左下方扩展，左边固定
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right -= Math.round(deltaX);
                    rect1.top += Math.round(deltaY);
                    print("center", rect1);

                    // 右侧item向左上方扩展，右边固定
                    View view2 = getChildAt(mItemLargeIndex + 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.left -= Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);
                    print("right", rect2);

                    // 向左滑动一个Item后更新当前Large item指向
                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex += 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);
                    }
                } else if (distanceX < 0) {     // scroll right
                    // 中间item向右上方扩展，左边固定
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right += Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);
                    print("center", rect1);

                    // 右侧item向右下方扩展， 右边固定
                    View view2 = getChildAt(mItemLargeIndex + 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.left += Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
                    print("right", rect2);
                }
            }
        } else if (itemCenterX > centerLine) {      // locate in right side of CustomScrollView
            if (mItemLargeIndex > 0) {
                if (distanceX > 0) {                // scroll left
                    // 中间item向左上方扩展，右边固定
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left -= Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);
//                        Log.i(TAG, String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
                    print("center", rect1);

                    // 左侧item向左下方扩展，左边固定
                    View view2 = getChildAt(mItemLargeIndex - 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.right -= Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
                    print("left", rect2);
                } else if (distanceX < 0) {     // scroll right
                    // 中间item向右下方扩展，右边固定
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left += Math.round(deltaX);
                    rect1.top += Math.round(deltaY);
                    log(String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
                    print("center", rect1);

                    // 左侧item向右上方扩展，左边固定
                    View view2 = getChildAt(mItemLargeIndex - 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.right += Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);
                    print("left", rect2);

                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex -= 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);
                    }
                }
            }
        }
        requestLayout();
        return true;
    }

    private float getItemWHRatio() {
        return 1.0f * mItemHeight / mItemWidth;
    }

    /** Set the end animator TimeInterpolator, if set null it will use the default
     * {@link android.view.animation.LinearInterpolator}
     * @param interpolator reference to subclass of {@link Interpolator}*/
    public void setInterpolator(@Nullable Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            TextView textView = new TextView(context);
            textView.setText(TAG);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(20);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER
            );
            addView(textView, params);
            setBackgroundColor(Color.GRAY);
            return;
        }

        if (attrs == null) {
            mItemWidth = DEFAULT_ITEM_WIDTH;
            mItemHeight = DEFAULT_ITEM_HEIGHT;
            mItemMargin = DEFAULT_ITEM_MARGIN;
            mItemLargeWidth = DEFAULT_ITEM_LARGE_WIDTH;
            mItemLargeHeight = DEFAULT_ITEM_LARGE_HEIGHT;
            mScrollFactor = DEFAULT_SCROLL_FACTOR;
            mTouchDiff = DEFAULT_TOUCH_DIFF;
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomScrollView);
            mItemWidth = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_width, DEFAULT_ITEM_WIDTH);
            mItemHeight = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_height, DEFAULT_ITEM_HEIGHT);
            mItemMargin = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_margin, DEFAULT_ITEM_MARGIN);
            mItemLargeWidth = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_large_width, DEFAULT_ITEM_LARGE_WIDTH);
            mItemLargeHeight = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_large_height, DEFAULT_ITEM_LARGE_HEIGHT);
            mScrollFactor = typedArray.getFloat(R.styleable.CustomScrollView_cs_item_scroll_factor, DEFAULT_SCROLL_FACTOR);
            mTouchDiff = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_touch_diff, DEFAULT_TOUCH_DIFF);
            mTouchDiff = Math.min(mTouchDiff, mItemLargeWidth - mItemWidth);
            log("item width:" + mItemWidth + " px");
            log("touch diff:" + mTouchDiff + " px");
            typedArray.recycle();
        }

        mContext = context;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (getChildCount() <= 0) {
                    log("Has no child view!");
                    return true;
                } else {
                    return CustomScrollView.this.onScroll(e1, e2, distanceX, distanceY);
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAnimation();
                break;
            case MotionEvent.ACTION_UP:
                playAnimation();
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private void playAnimation() {
        if (getChildCount() <= 0) {
            return;
        }

        // 手指离开屏幕时，将当前最大的Item设置为中心Item
        int newIndex = mItemLargeIndex;
        Rect centerRect = (Rect)getChildAt(mItemLargeIndex).getTag();
        if (mItemLargeIndex > 0) {
            Rect leftRect = (Rect)getChildAt(mItemLargeIndex - 1).getTag();
            if (centerRect.width() < (mItemLargeWidth - mTouchDiff)
                    && leftRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex - 1;
            }
        }
        if (mItemLargeIndex < getChildCount() - 1) {
            Rect rightRect = (Rect)getChildAt(mItemLargeIndex + 1).getTag();
            if (centerRect.width() < (mItemLargeWidth - mTouchDiff)
                    && rightRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex + 1;
            }
        }
        mItemLargeIndex = newIndex;

        log("item large index:" + mItemLargeIndex);
        adjustItemSize(mItemLargeIndex);

        final int destTranslationX = getTranslateX(mItemLargeIndex);
        final int srcTranslationX = mTranslationX;

        mAnimator = ValueAnimator.ofInt(srcTranslationX, destTranslationX);
        mAnimator.setDuration(DURATION);
        mAnimator.setInterpolator(mInterpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mTranslationX = (int) (srcTranslationX + (destTranslationX - srcTranslationX) * fraction);
//                Log.i(TAG, "fraction:" + fraction);
                requestLayout();
            }
        });
        mAnimator.start();
    }

    private void stopAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    /** Remove all child items  */
    public void clearItems() {
        if (getChildCount() > 0) {
            mItemLargeIndex = -1;
            removeAllViewsInLayout();
            invalidate();
        }
    }

    private void update() {
        if (getChildCount() > 0) {
            mItemLargeIndex = getChildCount() / 2;
            mTranslationX = getTranslateX(mItemLargeIndex);
            // adjust item size on get scrollview measured size
            adjustItemSize(mItemLargeIndex);
            requestLayout();
        }
    }

    /** When the nth item is Large Item and center in screen,
     * return the first item startX position */
    private int getTranslateX(int n) {
        if (n < 0 || n > getChildCount() - 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid argument n: %d ,n must be in [0, %d]", n, getChildCount()-1));
        }
        int startX = mWidth / 2;
        startX -= getLargeItemWM()/2;
        if (n > 0) {
            startX -= (getItemWM() * n);
        }
        return startX;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (isInEditMode()) {
            return;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            View view = getChildAt(i);
            Rect rect = (Rect) view.getTag();
            view.layout(mTranslationX + rect.left, rect.top, mTranslationX + rect.right, rect.bottom);
        }
    }

    /** Get item width with left and right margin */
    private int getItemWM() {
        return mItemWidth + mItemMargin * 2;
    }

    private int getLargeItemWM() {
        return mItemLargeWidth +  mItemMargin * 2;
    }

    private void print(String msg, Rect rect) {
        log(String.format("%s, l=%d, r=%d, w=%d, h=%d", msg,
                rect.left, rect.right, rect.width(), rect.height()));
    }

    /** Adjust all items size because scroll will lead precision loss. */
    private void adjustItemSize(int largeItemIndex) {
        int n = getChildCount();
        for (int i = 0; i < n; ++i) {
            int itemLeft = i * getItemWM() + mItemMargin;
            Rect rect = (Rect) getChildAt(i).getTag();
            if (i < largeItemIndex) {
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else if (i > largeItemIndex) {
                itemLeft = (i - 1) * getItemWM() + getLargeItemWM() + mItemMargin;
                rect.set(itemLeft, mHeight - mItemHeight, itemLeft + mItemWidth, mHeight);
            } else {
                rect.set(itemLeft, mHeight - mItemLargeHeight, itemLeft + mItemLargeWidth, mHeight);
            }
        }
    }

    private void log(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }

    public interface Adapter {
        int getCount();
        View getView(ViewGroup parent);
    }
}
