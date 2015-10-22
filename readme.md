    @Override
    public void onScrollChanged(HorizontalScrollView hScrollView, int h, int v, int preH, int preV) {
        // 滑动方向
        @SCROLL_DIRECTION
        int scroll_direction = SCROLL_NONE;
        if (h > preH) {
            scroll_direction = SCROLL_LEFT;
        } else if (h < preH) {
            scroll_direction = SCROLL_RIGHT;
        } else {
            scroll_direction = SCROLL_NONE;
        }

        // 滑动距离，单位px
        float scrollFactor = 1.0f;
        float deltaX = Math.abs(h - preH) * scrollFactor;

        if (getItemCount() <= 1) {
            Log.i(TAG, "Only one item");
            return;
        }
        if (!mCanChange) {
            Log.i(TAG, "Can not change");
            mCanChange = !mCanChange;       // 下次才可以进行大小变化
            return;
        }

        int activeItemIndex = mMiddleItemIndex;
        View middleView = mLayout.getChildAt(activeItemIndex);
        ViewGroup.LayoutParams lpm = middleView.getLayoutParams();

        View firstView = mLayout.getChildAt(mFillCount / 2 + 0);
        View lastView = mLayout.getChildAt(mFillCount / 2 + getItemCount() - 1);
        int[] loc = new int[2];
        firstView.getLocationOnScreen(loc);
        int firstItemCenterX = loc[0] + lpm.width/2;
        lastView.getLocationOnScreen(loc);
        int lastItemCenterX = loc[0] + lpm.width/2;
        middleView.getLocationOnScreen(loc);
        int middleItemCenterX = loc[0] + lpm.width/2;
        int screenCenterX = getScreenWidth() / 2;

        if (firstItemCenterX >= screenCenterX
                || lastItemCenterX <= screenCenterX) {
            Log.i(TAG, "Not in valid range");
            return;
        }

        if (scroll_direction == SCROLL_LEFT) {
            lpm.width -= deltaX;
            lpm.height -= (mItemContentHeight * 1.0f / mItemContentWidth * deltaX);
            // 一旦Item的宽度小于正常宽度时，修正大小，更新当前处于激活状态的Item索引
            if (lpm.width < mItemContentWidth || lpm.height < mItemContentHeight) {
                lpm.width = mItemContentWidth;
                lpm.height = mItemContentHeight;
                if (isValidItem(activeItemIndex + 1)) {
                    mMiddleItemIndex = activeItemIndex + 1;      // 指向新的item
                    Log.i(TAG, "new active item index:" + mMiddleItemIndex);
                }
            }
            middleView.setLayoutParams(lpm);

            // 改变当前激活状态Item右侧Item的大小，二位的大小总和始终保持不变
            int sideViewIndex;
            if (middleItemCenterX > screenCenterX) {
                sideViewIndex = activeItemIndex - 1;
            } else if (middleItemCenterX < screenCenterX) {
                sideViewIndex = activeItemIndex + 1;
            } else {
                return;
            }
            View sideView = mLayout.getChildAt(activeItemIndex + 1);
            ViewGroup.LayoutParams lps = sideView.getLayoutParams();
            lps.width = (mItemContentWidth + mLargeItemContentWidth) - lpm.width;
            lps.height = (mItemContentHeight + mLargeItemContentHeight) - lpm.height;
            sideView.setLayoutParams(lps);

            Log.i(TAG, String.format("middle view:index=%d, l=%.0f, r=%.0f, w=%d",
                    activeItemIndex,
                    middleView.getX() - mItemLeftMargin,
                    middleView.getX() + lpm.width + mItemRightMargin,
                    lpm.width
            ));

            Log.i(TAG, String.format("right view:index=%d, l=%.0f, r=%.0f, w=%d",
                    activeItemIndex + 1,
                    sideView.getX() - mItemLeftMargin,
                    sideView.getX() + lps.width + mItemRightMargin,
                    lps.width
            ));
//            mLayout.setX(mLayout.getX() - deltaX);
        } else if (scroll_direction == SCROLL_RIGHT) {
            lpm.width -= deltaX;
            lpm.height -= (mItemContentHeight * 1.0f / mItemContentWidth * deltaX);
            // 一旦Item的宽度小于正常宽度时，修正大小，更新当前处于激活状态的Item索引
            if (lpm.width < mItemContentWidth || lpm.height < mItemContentHeight) {
                lpm.width = mItemContentWidth;
                lpm.height = mItemContentHeight;
                if (isValidItem(activeItemIndex - 1)) {
                    mMiddleItemIndex = activeItemIndex - 1;      // 指向新的item
                    Log.i(TAG, "new active item index:" + mMiddleItemIndex);
                }
            }
            middleView.setLayoutParams(lpm);

            // 改变当前激活状态Item右侧Item的大小，二位的大小总和始终保持不变
            int sideViewIndex;
            if (middleItemCenterX > screenCenterX) {
                sideViewIndex = activeItemIndex - 1;
            } else if (middleItemCenterX < screenCenterX) {
                sideViewIndex = activeItemIndex + 1;
            } else {
                return;
            }
            View sideView = mLayout.getChildAt(activeItemIndex + 1);
            ViewGroup.LayoutParams lps = sideView.getLayoutParams();
            lps.width = (mItemContentWidth + mLargeItemContentWidth) - lpm.width;
            lps.height = (mItemContentHeight + mLargeItemContentHeight) - lpm.height;
            sideView.setLayoutParams(lps);

            Log.i(TAG, String.format("middle view:index=%d, l=%.0f, r=%.0f, w=%d",
                    activeItemIndex,
                    middleView.getX() - mItemLeftMargin,
                    middleView.getX() + lpm.width + mItemRightMargin,
                    lpm.width
            ));

            Log.i(TAG, String.format("left view:index=%d, l=%.0f, r=%.0f, w=%d",
                    activeItemIndex - 1,
                    sideView.getX() - mItemLeftMargin,
                    sideView.getX() + lps.width + mItemRightMargin,
                    lps.width
            ));
//            mLayout.setX(mLayout.getX() + deltaX);
        }

//        View middleView = mLayout.getChildAt(mMiddleItemIndex);
//        ViewGroup.LayoutParams lpm = middleView.getLayoutParams();
//        View rightView = mLayout.getChildAt(mMiddleItemIndex + 1);
//        ViewGroup.LayoutParams lpr = rightView.getLayoutParams();
//        Log.i(TAG, String.format("middle view:index=%d, l=%.0f, r=%.0f", mMiddleItemIndex,
//                middleView.getX() - mItemLeftMargin, middleView.getX() + lpm.width + mItemRightMargin));
//        Log.i(TAG, String.format("right view: l=%.0f, r=%.0f",
//                rightView.getX() - mItemLeftMargin, rightView.getX() + lpr.width + mItemRightMargin));
//
//        if (h > preH) {                     // deltaX left
//            if (isValidItem(mMiddleItemIndex + 1)) {
//                int[] loc = new int[2];
//                middleView.getLocationOnScreen(loc);
//                if (loc[0] + lpm.width/2 > getScreenWidth()/2) {
//                    // do nothing
//                } else {
//                    lpm.width -= deltaX;
//                    float heightDelta = mItemContentHeight * 1.0f / mItemContentWidth * deltaX;
//                    lpm.height -= heightDelta;
//                    if (lpm.width <= mItemContentWidth) {
//                        lpm.width = mItemContentWidth;
//                        lpm.height = mItemContentHeight;
//                        if (isValidItem(mMiddleItemIndex + 1)) {
//                            mMiddleItemIndex += 1;      // 指向新的item
//                            Log.i(TAG, "middle item index:" + mMiddleItemIndex);
//                        }
//                    }
//                    lpr.width = (mItemContentWidth + mLargeItemContentWidth) - lpm.width;
//                    lpr.height = (mItemContentHeight + mLargeItemContentHeight) - lpm.height;
//
//                    if (mFromUser) {
////                    mLayout.setX(mLayout.getX() - deltaX);
//                    } else {
//                        mFromUser = true;
//                    }
//                }
//            }
//        } else if (h < preH){               // deltaX right
//
//            int[] loc = new int[2];
//            middleView.getLocationOnScreen(loc);
//            if (loc[0] + lpm.width/2 < getScreenWidth()/2) {
//                // do nothing
//            } else {
//                lpm.width += deltaX;
//                float heightDelta = mItemContentHeight * 1.0f / mItemContentWidth * deltaX;
//                lpm.height += heightDelta;
//                if (lpm.width >= mLargeItemContentWidth) {
//                    lpm.width = mLargeItemContentWidth;
//                    lpm.height = mLargeItemContentHeight;
//                    if (isValidItem(mMiddleItemIndex - 1)) {
//                        mMiddleItemIndex -= 1;      // 指向新的item
//                        Log.i(TAG, "middle item index:" + mMiddleItemIndex);
//                    }
//                }
//                lpr.width = (mItemContentWidth + mLargeItemContentWidth) - lpm.width;
//                lpr.height = (mItemContentHeight + mLargeItemContentHeight) - lpm.height;
//                if (mFromUser) {
//                    if (isValidItem(mMiddleItemIndex + 1)) {
////                    mLayout.setX(mLayout.getX() + deltaX);
//                    }
//                } else {
//                    mFromUser = true;
//                }
//            }
//        } else {                            // no deltaX
//            return;
//        }
//
//        middleView.setLayoutParams(lpm);
//        rightView.setLayoutParams(lpr);
    }

