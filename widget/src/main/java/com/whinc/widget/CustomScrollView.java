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
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.whinc.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/21.
 * Future work:
 * <p style="text-decoration:line-through">* Disable log in debug mode </p>
 * <p style="text-decoration:line-through">* Don't store item view size in view's tag </p>
 * <p style="text-decoration:line-through">* Improve performance with view recycler</p>
 * <p style="text-decoration:line-through">* getView() method of Adapter add a argument present current item index</p>
 * <p style="text-decoration:line-through">* improve performance of updateVisibleRange() method avoid finding visible items range from start to end</p>
 */
public class CustomScrollView extends ViewGroup {
    public static final int SCROLL_SPEED_SLOW = 0;
    public static final int SCROLL_SPEED_NORMAL = 1;
    public static final int SCROLL_SPEED_FAST = 2;
    private static final String TAG = CustomScrollView.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int DURATION = 500;        // ms
    private static final int DEFAULT_ITEM_WIDTH = 200;
    private static final int DEFAULT_ITEM_HEIGHT = DEFAULT_ITEM_WIDTH * 4 / 3;
    private static final int DEFAULT_ITEM_MARGIN = 10;
    private static final int DEFAULT_ITEM_LARGE_WIDTH = DEFAULT_ITEM_WIDTH * 2;
    private static final int DEFAULT_ITEM_LARGE_HEIGHT = DEFAULT_ITEM_HEIGHT * 2;
    private static final float DEFAULT_SCROLL_FACTOR = 0.5f;
    private static final int DEFAULT_TOUCH_DIFF = 20;
    private ValueAnimator mScaleAnimator;
    /**
     * scroll action
     */
    private Runnable mScrollRunnable;
    /**
     * Destination index that want scroll to
     */
    private int mDestItemLargeIndex = -1;
    /**
     * Width of ScrollView
     */
    private int mWidth;
    /**
     * Height of ScrollView
     */
    private int mHeight;
    /**
     * Width of item in ScrollView
     */
    private int mItemWidth;
    /**
     * Height of item in ScrollView
     */
    private int mItemHeight;

    /* XML layout attributes */
    /**
     * Spacing between each item in ScrollView
     */
    private int mItemMargin;
    /**
     * Width of large item in ScrollView
     */
    private int mItemLargeWidth;
    /**
     * Height of large item in ScrollView
     */
    private int mItemLargeHeight;
    /**
     * The least distance scroll to next item
     */
    private int mScrollItemLeastDistance;
    /**
     * Affect scroll sensibility ([real scroll distance] = [pointer scroll distance] * [scroll factor] )
     */
    private float mScrollFactor;
    /**
     * Scroll speed, reference to {@link com.whinc.widget.CustomScrollView.ScrollSpeed}
     */
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
    private boolean mInitialized = false;
    private float[] mOldTouchPos = new float[2];
    /**
     * recode view reference
     */
    private View[] mViews = null;
    /**
     * record view position and size
     */
    private Rect[] mViewsRect = null;
    private int mVisibleItemStart = -1;
    private int mVisibleItemEnd = -1;
    private List<View> mRecyclerViews = new LinkedList<>();
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

    /**
     * <p>Get first visible item position</p>
     * @return
     */
    public int getVisibleItemStart() {
        return mVisibleItemStart;
    }

    /**
     * <p>Get the next position of last visible item</p>
     * @return
     */
    public int getVisibleItemEnd() {
        return mVisibleItemEnd;
    }

    public int getItemWidth() {
        return mItemWidth;
    }

    public void setItemWidth(int itemWidth) {
        mItemWidth = itemWidth;
    }

    public int getItemHeight() {
        return mItemHeight;
    }

    public void setItemHeight(int itemHeight) {
        mItemHeight = itemHeight;
    }

    public int getItemMargin() {
        return mItemMargin;
    }

    public void setItemMargin(int itemMargin) {
        mItemMargin = itemMargin;
    }

    public int getItemLargeWidth() {
        return mItemLargeWidth;
    }

    public void setItemLargeWidth(int itemLargeWidth) {
        mItemLargeWidth = itemLargeWidth;
    }

    public int getItemLargeHeight() {
        return mItemLargeHeight;
    }

    public void setItemLargeHeight(int itemLargeHeight) {
        mItemLargeHeight = itemLargeHeight;
    }

    public float getScrollFactor() {
        return mScrollFactor;
    }

    public void setScrollFactor(float scrollFactor) {
        mScrollFactor = scrollFactor;
    }

    public int getScrollItemLeastDistance() {
        return mScrollItemLeastDistance;
    }

    public void setScrollItemLeastDistance(int scrollItemLeastDistance) {
        mScrollItemLeastDistance = scrollItemLeastDistance;
    }

    public int getScrollSpeed() {
        return mScrollSpeed;
    }

    public void setScrollSpeed(@ScrollSpeed int scrollSpeed) {
        mScrollSpeed = scrollSpeed;
    }

    /**
     * Get current large item index.
     */
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
        // set flag to uninitialized
        mInitialized = false;

        if (adapter == null) {
            mAdapter = null;
            return;
        }

        mAdapter = adapter;

        int count = adapter.getCount();
        mViews = new View[count];
        mViewsRect = new Rect[count];

        int visibleCount = getMaxVisibleItemCountOnScreen();
        if (visibleCount < count) {
            mVisibleItemStart = count / 2 - visibleCount / 2;
            mVisibleItemEnd = mVisibleItemStart + visibleCount;
        } else {
            mVisibleItemStart = 0;
            mVisibleItemEnd = mVisibleItemStart + count;
        }
        Log.i(TAG, String.format("count:%d, visible count:%d, active range:[%d, %d)", count, visibleCount, mVisibleItemStart, mVisibleItemEnd));
        for (int i = mVisibleItemStart; i < mVisibleItemEnd; ++i) {
            createItem(i);
        }

        // If setAdapter() is called before scroll view can show call initialize() on time in onMeasure() of
        // scroll view, otherwise call initialize() immediately.
        if (isShown()) {
            initialize();
        }
    }

    /**
     * <p>Get max visible item count</p>
     *
     * @return
     */
    private int getMaxVisibleItemCountOnScreen() {
        int width = mWidth;
        int visibleCount = 1;
        width -= getLargeItemWM();
        while (width > 0) {
            visibleCount += 1;
            width -= getItemWM();
        }
        return visibleCount;
    }

    /**
     * Scroll to specified position
     */
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
        getHandler().postDelayed(mScrollRunnable, delay);
    }

    /**
     * <p>Scroll left or right by specified items. If ScrollView is scrolling this call will be
     * ignored.</p>
     *
     * @param itemCount item count need to scroll by. If it large than "scroll item count" -1 or small
     *                  than 0, it will be limited to [0, "scroll item count" - 1].
     */
    public void scrollBy(int itemCount) {
        if (isScrolling()) {
            Log.i(TAG, "Is scrolling");
            return;
        }
        mIsScrolling = true;

        // limit index range
        mDestItemLargeIndex = Math.min(Math.max(mItemLargeIndex + itemCount, 0), getItemCount() - 1);
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
        Log.i(TAG, String.format("Start scrolling, cur index:%d, dest index:%d, speed:%d, delay:%d",
                mItemLargeIndex, mDestItemLargeIndex, scrollDistanceX, scrollDelay));

        scrollBy(itemCount, mDestItemLargeIndex, scrollDistanceX, scrollDelay);
    }

    /**
     * Return true is ScrollView is scrolling, otherwise return false.
     */
    public boolean isScrolling() {
        return mIsScrolling;
    }

    /**
     * Stop scrolling
     */
    public void stopScroll() {
        getHandler().removeCallbacks(mScrollRunnable);
        mScrollRunnable = null;
        mIsScrolling = false;
        Log.i(TAG, "stop scroll");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (isInEditMode()) {
            return;
        }

        // update with and height
        if (mWidth != getMeasuredWidth()) {
            mWidth = getMeasuredWidth();
            if (mInitialized && getVisibleItemCount() > 0) {
                adjustTranslation(mItemLargeIndex);
                updateVisibleItemRange();
            }
        }
        if (mHeight != getMeasuredHeight()) {
            mHeight = getMeasuredHeight();
        }

        if (!mInitialized) {
            initialize();
        }

        // Measure children
        if (getVisibleItemCount() > 0) {
            for (int i = mVisibleItemStart; i < mVisibleItemEnd; ++i) {
                View child = getItem(i) == null ? createItem(i) : getItem(i);
                Rect rect = getItemRect(i);
                int widthSpec = MeasureSpec.makeMeasureSpec(rect.width(), MeasureSpec.EXACTLY);
                int heightSpec = MeasureSpec.makeMeasureSpec(rect.height(), MeasureSpec.EXACTLY);
                measureChild(child, widthSpec, heightSpec);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (isInEditMode()) {
            return;
        }

        if (getVisibleItemCount() > 0) {
            for (int i = mVisibleItemStart; i < mVisibleItemEnd; ++i) {
                View view = getItem(i) == null ? createItem(i) : getItem(i);
                Rect rect = getItemRect(i);
                view.layout(mTranslationX + rect.left, rect.top, mTranslationX + rect.right, rect.bottom);
                Log.i(TAG, String.format("onLayout:%d->%d, changed:%b", mVisibleItemStart, mVisibleItemEnd, changed));
            }
        }
    }

    /**
     * Called when scroll current view
     */
    private boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (Math.abs(distanceX) < 1) {
            return false;
        }

        distanceX *= mScrollFactor;
        mTranslationX -= distanceX;
        float ratio = Math.abs(distanceX / getItemWM());
        Rect rect = getItemRect(mItemLargeIndex);
        int itemCenterX = mTranslationX + rect.left + rect.width() / 2;
        int centerLine = mWidth / 2;
        if (itemCenterX < centerLine) {          // locate in left side of CustomScrollView
            if (mItemLargeIndex < getItemCount() - 1) {
                if (distanceX > 0) {            // scroll left
                    // 中间item，左边固定，向左下方扩展
                    Rect rect1 = getItemRect(mItemLargeIndex);
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right -= Math.round(deltaX);
                    rect1.top += Math.round(deltaY);

                    // 右侧item，右边固定，向左上方扩展
                    Rect rect2 = getItemRect(mItemLargeIndex + 1);
                    rect2.left -= Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);

                    // 向左滑动一个Item后更新当前Large item指向
                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex += 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);

                        if (mItemChangedListener != null) {
                            final int prev = mItemLargeIndex - 1;
                            final int cur = mItemLargeIndex;
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    mItemChangedListener.onChanged(CustomScrollView.this, prev, cur);
                                }
                            });
                        }
                    }
                } else if (distanceX < 0) {     // scroll right
                    // 中间item，左边固定，向右上方扩展
                    Rect rect1 = getItemRect(mItemLargeIndex);
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.right += Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);

                    // 右侧item，右边固定，向右下方扩展
                    Rect rect2 = getItemRect(mItemLargeIndex + 1);
                    rect2.left += Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
                }
            }
        } else if (itemCenterX > centerLine) {      // locate in right side of CustomScrollView
            if (mItemLargeIndex > 0) {
                if (distanceX > 0) {                // scroll left
                    // 中间item，右边固定，向左上方扩展
                    Rect rect1 = getItemRect(mItemLargeIndex);
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left -= Math.round(deltaX);
                    rect1.top -= Math.round(deltaY);

                    // 左侧item，左边固定，向左下方扩展
                    Rect rect2 = getItemRect(mItemLargeIndex - 1);
                    rect2.right -= Math.round(deltaX);
                    rect2.top += Math.round(deltaY);
                } else if (distanceX < 0) {     // scroll right
                    // 中间item，右边固定，向右下方扩展
                    Rect rect1 = getItemRect(mItemLargeIndex);
                    float deltaX = (mItemLargeWidth - mItemWidth) * ratio;
                    float deltaY = getItemWHRatio() * deltaX;
                    rect1.left += Math.round(deltaX);
                    rect1.top += Math.round(deltaY);

                    // 左侧item，左边固定，向右上方扩展
                    Rect rect2 = getItemRect(mItemLargeIndex - 1);
                    rect2.right += Math.round(deltaX);
                    rect2.top -= Math.round(deltaY);

                    if (rect2.width() >= mItemLargeWidth ||
                            rect2.height() >= mItemLargeHeight) {
                        mItemLargeIndex -= 1;
                        // 修正大小
                        mTranslationX = getTranslateX(mItemLargeIndex);
                        adjustItemSize(mItemLargeIndex);

                        if (mItemChangedListener != null) {
                            final int prev = mItemLargeIndex + 1;
                            final int cur = mItemLargeIndex;
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    mItemChangedListener.onChanged(CustomScrollView.this, prev, cur);
                                }
                            });
                        }
                    }
                }
            }
        }
        updateVisibleItemRange();
        requestLayout();
        return false;
    }


    private boolean isItemVisible(int pos) {
        Rect rect = mViewsRect[pos];
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        boolean r = (rect != null
                && (rect.right + mTranslationX) >= 0
                && (rect.left + mTranslationX) <= screenWidth);
        Log.i(TAG, String.format("Item at %d left:%d right:%d %s",
                pos,
                rect.left + mTranslationX,
                rect.right + mTranslationX,
                (r ? "visible" : "invisible")
        ));
        return r;
    }

    private float getItemWHRatio() {
        return 1.0f * mItemHeight / mItemWidth;
    }

    private float getItemLargeWHRation() {
        return 1.0f * mItemLargeHeight / mItemLargeWidth;
    }

    /**
     * Set the end animator TimeInterpolator, if set null it will use the default
     * {@link android.view.animation.LinearInterpolator}
     *
     * @param interpolator reference to subclass of {@link Interpolator}
     */
    public void setInterpolator(@Nullable Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    private void init(Context context, AttributeSet attrs) {
        Log.enable(true);
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
            Log.i(TAG, "item width:" + mItemWidth + " px");
            Log.i(TAG, "touch diff:" + mScrollItemLeastDistance + " px");
            typedArray.recycle();
        }

        mContext = context;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (getItemCount() <= 0) {
                    Log.i(TAG, "Has no child view!");
                    return false;
                } else {
                    return CustomScrollView.this.onScroll(e1, e2, distanceX, distanceY);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        onTouchEvent(ev);

        // if user scroll view should not dispatch touch event to child views
        // which can avoid triggering touch event of child views(like OnClick etc.).
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOldTouchPos[0] = ev.getRawX();
                mOldTouchPos[1] = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(ev.getRawX() - mOldTouchPos[0]);
                float deltaY = Math.abs(ev.getRawY() - mOldTouchPos[1]);
                float delta = (float) Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
                if (delta >= ViewConfiguration.getTouchSlop()) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
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

    /**
     * Reset position and size of each ScrollView item with smooth animation
     */
    private void smoothReset() {
        if (getItemCount() <= 0) {
            return;
        }

        // 手指离开屏幕时，将当前最大的Item设置为中心Item
        final int oldIndex = mItemLargeIndex;
        int newIndex = mItemLargeIndex;
        Rect centerRect = mViewsRect[mItemLargeIndex];
        if (mItemLargeIndex > 0) {
            Rect leftRect = mViewsRect[mItemLargeIndex - 1];
            if (centerRect.width() < (mItemLargeWidth - mScrollItemLeastDistance)
                    && leftRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex - 1;
            }
        }
        if (mItemLargeIndex < getItemCount() - 1) {
            Rect rightRect = mViewsRect[mItemLargeIndex + 1];
            if (centerRect.width() < (mItemLargeWidth - mScrollItemLeastDistance)
                    && rightRect.width() > mItemWidth) {
                newIndex = mItemLargeIndex + 1;
            }
        }
        mItemLargeIndex = newIndex;

        final int scrollDirection = (newIndex - oldIndex);
        if (scrollDirection == 0) {
            adjustItemSize(newIndex);
        } else {
            // Scroll item scale animator
            final Rect newRect = getItemRect(newIndex);
            final Rect oldRect = getItemRect(oldIndex);
            int startW = newRect.width();
            final int startH = newRect.height();
            mScaleAnimator = ValueAnimator.ofInt(startW, getItemLargeWidth());
            mScaleAnimator.setDuration(DURATION / 2);
            mScaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int width = (Integer) animation.getAnimatedValue();
                    int height = (int) (width * getItemLargeWHRation());
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
        mTranslationAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                adjustItemSize(mItemLargeIndex);
                updateVisibleItemRange();

                if (mItemChangedListener != null && mItemLargeIndex != oldIndex) {
                    final int prev = oldIndex;
                    final int cur = mItemLargeIndex;
                    mItemChangedListener.onChanged(CustomScrollView.this, prev, cur);
                }
            }
        });
        mTranslationAnimator.start();
    }

    /**
     * Stop running animations on scrollview items
     */
    private void stopAnimation() {
        if (mTranslationAnimator != null && mTranslationAnimator.isRunning()) {
            mTranslationAnimator.cancel();
        }
        if (mScaleAnimator != null && mScaleAnimator.isRunning()) {
            mScaleAnimator.cancel();
            ;
        }
    }

    private void setViewRect(int pos, Rect rect) {
        mViewsRect[pos] = rect;
    }

    /**
     * Remove all child items
     */
    public void clearItems() {
        if (getItemCount() > 0) {
            mItemLargeIndex = -1;
            mViewsRect = null;
            mViews = null;
            mVisibleItemStart = -1;
            mVisibleItemEnd = -1;
            removeAllViewsInLayout();
            invalidate();
        }
    }

    /**
     * <p>Get total count of item in scrollview </p>
     */
    public int getItemCount() {
        return mViews == null ? 0 : mViews.length;
    }

    public int getVisibleItemCount() {
        return mVisibleItemEnd - mVisibleItemStart;
    }

    /**
     * <p>Get item in specified position. Create and return a new item if the item doesn't exist.</p>
     */
    private View createItem(int pos) {
        View view = mViews[pos];
        if (view == null) {
            View convertView = null;
            if (!mRecyclerViews.isEmpty()) {
                convertView = mRecyclerViews.remove(0);
                Log.i(TAG, "reuse item at " + pos);
            } else {
                Log.i(TAG, "create a new item at " + pos);
            }
            view = mAdapter.getView(pos, convertView, this);
            addView(view);
            mViews[pos] = view;
        }
        return view;
    }

    /**
     * <p>Get visible item in specified position. Return null if the item doesn't exist</p>
     */
    public View getItem(int pos) {
        return mViews[pos];
    }

    private void removeItem(int pos) {
        View view = mViews[pos];
        if (view != null) {
            mRecyclerViews.add(view);
            removeViewInLayout(view);
            mViews[pos] = null;
            Log.i(TAG, "remove item at " + pos, 2);
        }
    }

    private void updateVisibleItemRange() {

        // reduce update range to improve the search performance
        int rangeStart = Math.max(0, mItemLargeIndex - (getMaxVisibleItemCountOnScreen() / 2 + 1));
        int rangeEnd = Math.min(getItemCount(), mItemLargeIndex + (getMaxVisibleItemCountOnScreen() / 2 + 1) + 1);
        Log.i(TAG, String.format("search range:[%d, %d)", rangeStart, rangeEnd));

        boolean findFirstVisible = false;
        boolean findLastVisible = false;
        for (int i = rangeStart; i < rangeEnd; ++i) {
            if (isItemVisible(i)) {
                createItem(i);
                if (!findFirstVisible) {
                    mVisibleItemStart = i;
                    findFirstVisible = true;
                }
            } else {
                removeItem(i);
                if (findFirstVisible && !findLastVisible) {
                    mVisibleItemEnd = i;
                    findLastVisible = true;
                }
            }
        }

        if (findFirstVisible && !findLastVisible) {
            mVisibleItemEnd = rangeEnd;
        }

        Log.i(TAG, String.format("visible range:[%d, %d), large item pos:%d",
                mVisibleItemStart, mVisibleItemEnd, mItemLargeIndex));
    }

    /**
     * Get the rect presents view's layout position and size
     */
    private Rect getItemRect(int pos) {
        if (mViewsRect[pos] == null) {
            mViewsRect[pos] = new Rect(0, 0, 0, 0);
        }
        return mViewsRect[pos];
    }

    /**
     * <p>Reset item position,size and translationX. the result is the large item locates in
     * the center of ScrollView, items whose index small than large item locate in left
     * of large item, items whose index large than large item locate in right of large item </p>
     *
     * @param itemLargeIndex index of large item. if it large than "scroll item count" -1 or small
     *                       than 0, it will be limited to [0, "scroll item count" - 1].
     */
    private void initialize(int itemLargeIndex) {
        mItemLargeIndex = Math.min(Math.max(itemLargeIndex, 0), getItemCount() - 1);
        adjustTranslation(mItemLargeIndex);
        adjustItemSize(mItemLargeIndex);
        updateVisibleItemRange();

        if (mItemChangedListener != null) {
            mItemChangedListener.onChanged(this, mItemLargeIndex, mItemLargeIndex);
        }

        mInitialized = true;
    }

    /**
     * Reset item position,size and translationX, the result is the large item locates in
     * the center of ScrollView and other item locate in two side of the large item equally
     */
    private void initialize() {
        if (getItemCount() > 0) {
            int itemLargeIndex = getItemCount() / 2;
            initialize(itemLargeIndex);
        }
    }

    /**
     * When the nth item is Large Item and center in screen,
     * return the first item startX position
     */
    private int getTranslateX(int n) {
        if (n < 0 || n > getItemCount() - 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid argument n: %d ,n must be in [0, %d]", n, getItemCount() - 1));
        }
        float startX = mWidth / 2.0f;
        startX -= (getLargeItemWM() / 2.0f);
        if (n > 0) {
            startX -= getItemWM() * n;
        }
        return Math.round(startX);
    }

    /**
     * Get item width with left and right margin
     */
    private int getItemWM() {
        return mItemWidth + mItemMargin * 2;
    }

    private int getLargeItemWM() {
        return mItemLargeWidth + mItemMargin * 2;
    }

    /**
     * Adjust all item size to standard size
     */
    private void adjustItemSize(int largeItemIndex) {
        int n = getItemCount();
        for (int i = 0; i < n; ++i) {
            int itemLeft = i * getItemWM() + mItemMargin;
            Rect rect = getItemRect(i);
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

    private void adjustTranslation(int itemLargeIndex) {
        if (itemLargeIndex >= 0) {
            mTranslationX = getTranslateX(itemLargeIndex);
        }
    }

    @IntDef({SCROLL_SPEED_SLOW, SCROLL_SPEED_NORMAL, SCROLL_SPEED_FAST})
    private @interface ScrollSpeed {
    }

    public interface Adapter {
        int getCount();

        View getView(int position, View convertView, CustomScrollView parent);
    }

    public interface OnItemChangedListener {
        void onChanged(CustomScrollView parent, int prev, int cur);
    }
}
