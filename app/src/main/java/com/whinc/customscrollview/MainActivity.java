package com.whinc.customscrollview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.whinc.widget.CustomScrollView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    @Bind(R.id.custom_scrollView)
    CustomScrollView mCustomScrollView;
    private boolean mAutoScroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final int count = 3;
        mCustomScrollView.setInterpolator(new OvershootInterpolator());
        mCustomScrollView.setOnItemChangedListener(new CustomScrollView.OnItemChangedListener() {
            @Override
            public void onChanged(final CustomScrollView view, int prev, int cur) {
                Log.i(TAG, String.format("prev:%d, cur:%d", prev, cur));
                if (mAutoScroll) {
                    Log.i(TAG, "auto scroll");
                    if (cur == 0) {
                        // ScrollView is scrolling, so you need delaying call scrollTo()
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.scrollTo(view.getChildCount() - 1);
                            }
                        });
                    }
                    if (cur == view.getChildCount() - 1) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                view.scrollTo(0);
                            }
                        });
                    }
                }
            }
        });
        mCustomScrollView.setAdapter(new MyScrollViewAdapter(this, count));
    }

    @OnClick({R.id.add_3_item_button, R.id.add_8_item_button})
    protected void addItem(Button btn) {
        switch (btn.getId()) {
            case R.id.add_3_item_button:
                showScrollView(3);
                break;
            case R.id.add_8_item_button:
                showScrollView(8);
                break;
        }
    }

    private void showScrollView(final int n) {
        mCustomScrollView.setAdapter(new MyScrollViewAdapter(this, n));
        if (mCustomScrollView.getVisibility() != View.VISIBLE) {
            mCustomScrollView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.hide_button)
    protected void hideScrollView() {
        if (mCustomScrollView.getVisibility() == View.VISIBLE) {
            mCustomScrollView.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.scroll_left_button)
    protected void scrollLeft() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollBy(-1);
        }
    }

    @OnClick(R.id.scroll_right_button)
    protected void scrollRight() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollBy(1);
        }
    }

    @OnClick(R.id.clear_item_button)
    protected void clearItems() {
        mCustomScrollView.clearItems();
    }

    @OnClick(R.id.scroll_to_button)
    protected void scrollTo() {
        if (!mCustomScrollView.isScrolling()) {
            mCustomScrollView.scrollTo(0);
        }
    }

    @OnClick(R.id.stop_scroll_button)
    protected void stopScroll() {
        mCustomScrollView.stopScroll();
    }

    @OnClick(R.id.auto_scroll_button)
    protected void autoScroll() {
        mAutoScroll = true;
        if (mCustomScrollView.getItemLargeIndex() == 0) {
            mCustomScrollView.scrollTo(mCustomScrollView.getChildCount() - 1);
        } else {
            mCustomScrollView.scrollTo(0);
        }
    }

    @OnClick(R.id.stop_auto_scroll_button)
    protected void stopAutoScroll() {
        mAutoScroll = false;
    }

    @OnClick(R.id.goto_second_activity_button)
    protected void startSencodActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }

    static class MyScrollViewAdapter implements CustomScrollView.Adapter {
        private final Context mContext;
        private final int mCount;

        public MyScrollViewAdapter(Context context, int count) {
            mContext = context;
            mCount = count;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public View getView(CustomScrollView parent, int pos) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.scrollview_item, parent, false);
            ImageView imgView = (ImageView)view.findViewById(R.id.imageView);
            imgView.setImageResource(R.drawable.test_image);
            Log.i(TAG, "pos:" + pos);
            Log.i(TAG, "itemW:" + parent.getItemWidth() + ", itemH:" + parent.getItemHeight());
            return view;
        }
    }
}
