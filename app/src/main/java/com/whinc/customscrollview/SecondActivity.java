package com.whinc.customscrollview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.whinc.widget.CustomScrollView;

public class SecondActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* Create CustomScrollView with code */
        CustomScrollView customScrollView = new CustomScrollView(this);
        customScrollView.setBackgroundColor(Color.BLACK);
        customScrollView.setItemWidth(200);
        customScrollView.setItemHeight(240);
        customScrollView.setItemLargeWidth(350);
        customScrollView.setItemLargeHeight(450);
        customScrollView.setScrollFactor(0.5f);
        customScrollView.setScrollSpeed(CustomScrollView.SCROLL_SPEED_FAST);
        customScrollView.setScrollItemLeastDistance(10);
        customScrollView.setInterpolator(new OvershootInterpolator());

        setContentView(customScrollView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        final int count = 3;
        customScrollView.setOnItemChangedListener(new CustomScrollView.OnItemChangedListener() {
            @Override
            public void onChanged(final CustomScrollView view, int prev, int cur) {
//                Log.i(TAG, String.format("prev:%d, cur:%d", prev, cur));
            }
        });
        customScrollView.setAdapter(new CustomScrollView.Adapter() {
            @Override
            public int getCount() {
                return count;
            }

            @Override
            public View getView(ViewGroup parent, int pos) {
                View view = new ImageView(SecondActivity.this);
                view.setBackgroundResource(R.drawable.test_image);
                return view;
            }
        });
    }
}
