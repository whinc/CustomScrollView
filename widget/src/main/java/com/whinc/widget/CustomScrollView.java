package com.whinc.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;


/**
 * Created by Administrator on 2015/10/21.
 * Future work:
 * <p>* Scroll animation on touch release </p>
 * <p>* Don't store item view size in its tag </p>
 * <p>* Expose getter and setter methods</p>
 * <p>* Improve performance with view recycler</p>
 */
public class CustomScrollView extends FrameLayout{
    private static final String TAG = CustomScrollView.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final int DURATION = 500;        // ms
    private static final int DEFAULT_ITEM_WIDTH = 200;
    private static final int DEFAULT_ITEM_HEIGHT = DEFAULT_ITEM_WIDTH * 4 / 3;
    private static final int DEFAULT_ITEM_MARGIN = 10;
    private static final int DEFAULT_ITEM_LARGE_WIDTH = DEFAULT_ITEM_WIDTH * 2;
    private static final int DEFAULT_ITEM_LARGE_HEIGHT = DEFAULT_ITEM_HEIGHT * 2;
    private static final float DEFAULT_SCROLL_FACTOR = 0.5f;
    private static final int DEFAULT_TOUCH_DIFF = 20;

    public static final int SCROLL_SPEED_SLOW = 0;
    public static final int SCROLL_SPEED_NORMAL = 1;
    public static final int SCROLL_SPEED_FAST = 2;
    private ValueAnimator mScaleAnimator;

    @IntDef({SCROLL_SPEED_SLOW, SCROLL_SPEED_NORMAL, SCROLL_SPEED_FAST})
    private @interface ScrollSpeed {}

    /** scroll action */
    private Runnable mScrollRunnable;
    /** Destination index that want scroll to */
    private int mDestItemLargeIndex = -1;
    /** Width of ScrollView */
    private int mWidth;

    /* XML layout attributes */
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
    /** The least distance scroll to next item */
    private int mScrollItemLeastDistance;
    /** Affect scroll sensibility ([real scroll distance] = [pointer scroll distance] * [scroll factor] ) */
    private float mScrollFactor;
    /** Scroll speed, reference to {@link com.whinc.widget.CustomScrollView.ScrollSpeed} */
    private int mScrollSpeed;
    private OnItemChangedListener mItemChangedListener;
    private int mItemLargeIndex = -1;
    private Interpolator mInterpolator = null;
    private Context mContext;
    private int mTranslationX = 0;
    private GestureDetector mGestureDetector;
    private ValueAnimator mTranslationAnimator;
    private Adapter mAdapter;
    private boolean mIsScrolling = false;       // true if ScrollView is scrolling, otherwise is false
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

    /** Get current large item index. */
    public int getItemLargeIndex() {
        return mItemLargeIndex;
    }

    public void setOnItemChangedListener(OnItemChangedListener itemChangedListener) {
        mItemChangedListener = itemChangedListener;
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

        // If setAdapter() is called before scroll view can show call reset() on time in onMeasure() of
        // scroll view, otherwise call reset() immediately.
        if (isShown()) {
            reset();
        }
    }

    /** Scroll to specified position */
    public void scrollTo(int index) {
        scrollBy(index - mItemLargeIndex);
    }

    private void scrollBy(final int itemCount, final int destItemLargeIndex, final int scrollDistanceX, final int scrollDelay) {
        if (destItemLargeIndex == mItemLargeIndex) {
            stopScroll();
            return;
        }

        if (!isScrolling()) {
            return;
        }

        int delay = scrollDelay;
        // first time into recursion
        if (mScrollRunnable == null) {
            mScrollRunnable = new Runnable() {
                @Override
                public void run() {
                    onScroll(null, null, scrollDistanceX, 0);
                    scrollBy(itemCount, destItemLargeIndex, scrollDistanceX, scrollDelay);
                }
            };

            // first time don't need delay
            delay = 0;
        }

        // post scroll event until scroll to destination
        getHandler().postDelayed(mScrollRunnable, scrollDelay);
    }

    /**
     * <p>Scroll left or right by specified items. If ScrollView is scrolling this call will be
     * ignored.</p>
     * @param itemCount item count need to scroll by. If it large than "scroll item count" -1 or small
     *                       than 0, it will be limited to [0, "scroll item count" - 1].
     */
    public void scrollBy(int itemCount) {
        if (isScrolling()) {
            log("Is scrolling");
            return;
        }
        mIsScrolling = true;

        // limit index range
        mDestItemLargeIndex = Math.min(Math.max(mItemLargeIndex + itemCount, 0), getChildCount() - 1);
        int scrollDelay = Integer.MAX_VALUE;
        int scrollDistanceX = 10 * (itemCount > 0 ? 1 : -1);

        switch (mScrollSpeed) {
            case SCROLL_SPEED_SLOW:
                scrollDelay = 60;
                break;
            case SCROLL_SPEED_NORMAL:
                scrollDelay = 25;
                break;
            case SCROLL_SPEED_FAST:
                scrollDistanceX = (int) (scrollDistanceX * 2.0f);
                scrollDelay = 10;
                break;
            default:
                break;
        }
        log(String.format("Start scrolling, cur index:%d, dest index:%d, speed:%d, delay:%d",
                mItemLargeIndex, mDestItemLargeIndex, scrollDistanceX, scrollDelay));

        scrollBy(itemCount, mDestItemLargeIndex, scrollDistanceX, scrollDelay);
    }

    /** Return true is ScrollView is scrolling, otherwise return false. */
    public boolean isScrolling() {
        return mIsScrolling;
    }

    /** Stop scrolling */
    public void stopScroll() {
        getHandler().removeCallbacks(mScrollRunnable);
        mScrollRunnable = null;
        mIsScrolling = false;
        log("stop scroll");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isInEditMode()) {
            return;
        }

        if (mWidth <= 0) {      // store measured size
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            reset();
        }

        // Measure children
        for (int i = 0; i < getChildCount(); ++i) {
            View child = getChildAt(i);
            Rect rect = (Rect) child.getTag();
            int widthSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY);
            measureChild(child, widthSpec, heightSpec);
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
                    // 中间item，左边固定，向左下方扩展
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right -= Math.round(deltaX);
                    rect1.top += Math.round(deltaY);
//                    print("center", rect1);

                    // 右侧item，右边固定，向左上方扩展
                    View view2 = getChildAt(mItemLargeIndex + 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.left -= Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);
//                    print("right", rect2);

                    // 向左滑动一个Item后更新当前Large item指向
                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex += 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);

                        if (mItemChangedListener != null) {
                            mItemChangedListener.onChanged(this, mItemLargeIndex - 1, mItemLargeIndex);
                        }
                    }
                } else if (distanceX < 0) {     // scroll right
                    // 中间item，左边固定，向右上方扩展
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right += Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);
//                    print("center", rect1);

                    // 右侧item，右边固定，向右下方扩展
                    View view2 = getChildAt(mItemLargeIndex + 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.left += Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
//                    print("right", rect2);
                }
            }
        } else if (itemCenterX > centerLine) {      // locate in right side of CustomScrollView
            if (mItemLargeIndex > 0) {
                if (distanceX > 0) {                // scroll left
                    // 中间item，右边固定，向左上方扩展
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left -= Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);
//                        Log.i(TAG, String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
//                    print("center", rect1);

                    // 左侧item，左边固定，向左下方扩展
                    View view2 = getChildAt(mItemLargeIndex - 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.right -= Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
//                    print("left", rect2);
                } else if (distanceX < 0) {     // scroll right
                    // 中间item，右边固定，向右下方扩展
                    View view1 = getChildAt(mItemLargeIndex);
                    Rect rect1 = (Rect) view1.getTag();
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left += Math.round(deltaX);
                    rect1.top += Math.round(deltaY);
//                    log(String.format("deltaX=%d, deltaY=%d", Math.round(deltaX), Math.round(deltaY)));
//                    print("center", rect1);

                    // 左侧item，左边固定，向右上方扩展
                    View view2 = getChildAt(mItemLargeIndex - 1);
                    Rect rect2 = (Rect) view2.getTag();
                    rect2.right += Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);
//                    print("left", rect2);

                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex -= 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);

                        if (mItemChangedListener != null) {
                            mItemChangedListener.onChanged(this, mItemLargeIndex + 1, mItemLargeIndex);
                        }
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
            mScrollItemLeastDistance = DEFAULT_TOUCH_DIFF;
            mScrollSpeed = SCROLL_SPEED_NORMAL;
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomScrollView);
            mItemWidth = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_width, DEFAULT_ITEM_WIDTH);
            mItemHeight = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_height, DEFAULT_ITEM_HEIGHT);
            mItemMargin = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_margin, DEFAULT_ITEM_MARGIN);
            mItemLargeWidth = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_large_width, DEFAULT_ITEM_LARGE_WIDTH);
            mItemLargeHeight = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_item_large_height, DEFAULT_ITEM_LARGE_HEIGHT);
            mScrollFactor = typedArray.getFloat(R.styleable.CustomScrollView_cs_scroll_factor, DEFAULT_SCROLL_FACTOR);
            mScrollItemLeastDistance = typedArray.getDimensionPixelSize(R.styleable.CustomScrollView_cs_scroll_item_least_distance, DEFAULT_TOUCH_DIFF);
            mScrollItemLeastDistance = Math.min(mScrollItemLeastDistance, mItemLargeWidth - mItemWidth);
            mScrollSpeed = typedArray.getInteger(R.styleable.CustomScrollView_cs_scroll_speed, SCROLL_SPEED_NORMAL);
            log("item width:" + mItemWidth + " px");
            log("touch diff:" + mScrollItemLeastDistance + " px");
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
                smoothReset();
                break;
        }
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /** Reset position and size of each ScrollView item with smooth animation */
    private void smoothReset() {
        if (getChildCount() <= 0) {
            return;
        }

        // 手指离开屏幕时，将当前最大的Item设置为中心Item
        int oldIndex = mItemLargeIndex, newIndex = oldIndex;
        Rect centerRect = (Rect)getChildAt(mItemLargeIndex).getTag();
        if (mItemLargeIndex > 0) {
            Rect leftRect = (Rect)getChildAt(mItemLargeIndex - 1).getTag();
            if (centerRect.width() < (mItemLargeWidth - mScrollItemLeastDistance)
                    && leftRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex - 1;
            }
        }
        if (mItemLargeIndex < getChildCount() - 1) {
            Rect rightRect = (Rect)getChildAt(mItemLargeIndex + 1).getTag();
            if (centerRect.width() < (mItemLargeWidth - mScrollItemLeastDistance)
                    && rightRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex + 1;
            }
        }
        if (mItemChangedListener != null && mItemLargeIndex != newIndex) {
            mItemChangedListener.onChanged(this, mItemLargeIndex, newIndex);
        }
        mItemLargeIndex = newIndex;

//        log("item large index:" + newIndex);
        final int scrollDirection = (newIndex - oldIndex);
        if (scrollDirection == 0) {
            adjustItemSize(newIndex);
        } else {
            // Scroll item scale animator
            final Rect newRect = getViewRect(newIndex);
            final Rect oldRect = getViewRect(oldIndex);
            mScaleAnimator = ValueAnimator.ofInt(newRect.width(), mItemLargeWidth);
            mScaleAnimator.setDuration(DURATION / 2);
            mScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int width = (Integer) animation.getAnimatedValue();
                    int height = (int) (width * getItemWHRatio());
                    if (scrollDirection > 0) {      // scroll left
                        // newIndex item 右边固定，向左上方扩展
                        newRect.left = newRect.right - width;
                        newRect.top = mHeight - height;

                        // oldIndex item 左边固定，向左下方扩展
                        oldRect.right = oldRect.left + (mItemLargeWidth + mItemWidth - newRect.width());
                        oldRect.top = mHeight - (mItemLargeHeight + mItemHeight - height);
                    } else {                        // scroll right
                        // newIndex item 左边固定，向右上方扩展
                        newRect.right = newRect.left + width;
                        newRect.top = mHeight - height;

                        // oldIndex item 右边固定，向右下方扩展
                        oldRect.left = oldRect.right - (mItemLargeWidth + mItemWidth - newRect.width());
                        oldRect.top = mHeight - (mItemHeight + mItemLargeHeight - height);
                    }
                }
            });
            mScaleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    adjustItemSize(mItemLargeIndex);
                }
            });
            mScaleAnimator.start();
        }

        // Scroll item translation animator
        final int destTranslationX = getTranslateX(newIndex);
        final int srcTranslationX = mTranslationX;
        mTranslationAnimator = ValueAnimator.ofInt(srcTranslationX, destTranslationX);
        mTranslationAnimator.setDuration(DURATION);
        mTranslationAnimator.setInterpolator(mInterpolator);
        mTranslationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mTranslationX = (Integer) animation.getAnimatedValue();
                requestLayout();
            }
        });
        mTranslationAnimator.start();
    }

    /** Stop running animations on scrollview items */
    private void stopAnimation() {
        if (mTranslationAnimator != null && mTranslationAnimator.isRunning()) {
            mTranslationAnimator.cancel();
        }
        if (mScaleAnimator != null && mScaleAnimator.isRunning()) {
            mScaleAnimator.cancel();;
        }
    }

    /** Get the rect presents view's layout position and size */
    private Rect getViewRect(int index) {
        return (Rect) getChildAt(index).getTag();
    }

    private void setViewRect(int index, Rect rect) {
        getChildAt(index).setTag(rect);
    }

    /** Remove all child items  */
    public void clearItems() {
        if (getChildCount() > 0) {
            mItemLargeIndex = -1;
            removeAllViewsInLayout();
            invalidate();
        }
    }

    /** <p>Reset item position,size and translationX. the result is the large item locates in
     * the center of ScrollView, items whose index small than large item locate in left
     * of large item, items whose index large than large item locate in right of large item </p>
     *
     * @param itemLargeIndex index of large item. if it large than "scroll item count" -1 or small
     *                       than 0, it will be limited to [0, "scroll item count" - 1].
     * */
    private void reset(int itemLargeIndex) {
        mItemLargeIndex = Math.min(Math.max(itemLargeIndex, 0), getChildCount() - 1);

        mTranslationX = getTranslateX(mItemLargeIndex);
        // adjust item size on get scrollview measured size
        adjustItemSize(mItemLargeIndex);
        requestLayout();

        if (mItemChangedListener != null) {
            mItemChangedListener.onChanged(this, mItemLargeIndex, mItemLargeIndex);
        }
    }

    /** Reset item position,size and translationX, the result is the large item locates in
     * the center of ScrollView and other item locate in two side of the large item equally */
    private void reset() {
        reset(getChildCount() / 2);
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

    /** Adjust all item size to standard size */
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

    public interface OnItemChangedListener {
        void onChanged(CustomScrollView view, int prev, int cur);
    }
}
