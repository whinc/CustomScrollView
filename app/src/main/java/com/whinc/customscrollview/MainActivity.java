package com.whinc.customscrollview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.whinc.widget.CustomScrollView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.custom_scrollView)
    CustomScrollView mCustomScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.add_1_item_button, R.id.add_2_item_button, R.id.add_3_item_button, R.id.add_8_item_button})
    protected void addItem(Button btn) {
        int n = 0;
        switch (btn.getId()) {
            case R.id.add_1_item_button:
                n = 1;
                break;
            case R.id.add_2_item_button:
                n = 2;
                break;
            case R.id.add_3_item_button:
                n = 3;
                break;
            case R.id.add_8_item_button:
                n = 8;
                break;
        }
        mCustomScrollView.clearItems();
        mCustomScrollView.setInterpolator(new OvershootInterpolator());
        mCustomScrollView.addItem(n);
    }
}
