package com.whinc.customscrollview;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        final int count = 3;
        mCustomScrollView.setInterpolator(new OvershootInterpolator());
        mCustomScrollView.setOnItemChangedListener(new CustomScrollView.OnItemChangedListener() {
            @Override
            public void onChanged(int prev, int cur) {
                Log.i(TAG, String.format("prev:%d, cur:%d", prev, cur));
            }
        });
        mCustomScrollView.setAdapter(new CustomScrollView.Adapter() {
            @Override
            public int getCount() {
                return count;
            }

            @Override
            public View getView(ViewGroup parent) {
                View view = new ImageView(MainActivity.this);
                view.setBackgroundResource(R.drawable.test_image);
                return view;
            }
        });
    }

    @OnClick({R.id.add_3_item_button, R.id.add_8_item_button})
    protected void addItem(Button btn) {
        int n = 0;
        switch (btn.getId()) {
            case R.id.add_3_item_button:
                n = 3;
                break;
            case R.id.add_8_item_button:
                n = 8;
                break;
        }
//        mCustomScrollView.addItems(n);
        final int finalN = n;
        mCustomScrollView.setAdapter(new CustomScrollView.Adapter() {
            @Override
            public int getCount() {
                return finalN;
            }

            @Override
            public View getView(ViewGroup parent) {
                /** BUG:这里如果附加到parent view中会出现重复添加View的问题 */
                /** NEW:增加ScrollView 滑动事件监听 */
                View view = LayoutInflater.from(MainActivity.this)
                        .inflate(R.layout.scrollview_item, mCustomScrollView, false);
                ImageView imgView = (ImageView)view.findViewById(R.id.imageView);
                imgView.setImageResource(R.drawable.test_image);
                return view;
            }
        });
    }

    @OnClick(R.id.scroll_left_button)
    protected void scrollLeft() {
        if (mCustomScrollView.isScrolling()) {
            mCustomScrollView.stopScroll();
        }
        mCustomScrollView.scrollBy(-1);
    }

    @OnClick(R.id.scroll_right_button)
    protected void scrollRight() {
        if (mCustomScrollView.isScrolling()) {
            mCustomScrollView.stopScroll();
        }
        mCustomScrollView.scrollBy(1);
    }

    @OnClick(R.id.clear_item_button)
    protected void clearItems() {
        mCustomScrollView.clearItems();
    }

    @OnClick(R.id.scroll_to_button)
    protected void scrollTo() {
        mCustomScrollView.scrollTo(0);
    }

    @OnClick(R.id.stop_scroll_button)
    protected void stopScroll() {
        mCustomScrollView.stopScroll();
    }
}
