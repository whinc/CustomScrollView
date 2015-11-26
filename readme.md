
### CustomScrollView

![screenshot](./screenshot1.jpg)

### Integration （集成）

Add dependency to your `build.gradle` file like below:

```
repositories {
    maven { url "https://jitpack.io" }
 }

dependencies {
    ...
    compile 'com.github.whinc:CustomScrollView:1.2.1'
}
```

### How to use （如何使用）

Create a CustomScrollView with xml layout:

```
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    >
    <com.whinc.widget.CustomScrollView
        android:id="@+id/custom_scrollView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:cs_item_width="200px"
        app:cs_item_height="240px"
        app:cs_item_large_width="350px"
        app:cs_item_large_height="450px"
        app:cs_item_margin="20px"
        app:cs_scroll_factor="0.5"
        app:cs_scroll_item_least_distance="40px"
        app:cs_scroll_speed="fast"
        />
```

Create a CustomScrollView with java code:

```
CustomScrollView customScrollView = new CustomScrollView(this);
customScrollView.setItemWidth(200);
customScrollView.setItemHeight(240);
customScrollView.setItemLargeWidth(350);
customScrollView.setItemLargeHeight(450);
customScrollView.setScrollFactor(0.5f);
customScrollView.setScrollSpeed(CustomScrollView.SCROLL_SPEED_FAST);
customScrollView.setScrollItemLeastDistance(10);
customScrollView.setInterpolator(new OvershootInterpolator());
customScrollView.setOnItemChangedListener(new CustomScrollView.OnItemChangedListener() {
    @Override
    public void onChanged(final CustomScrollView view, int prev, int cur) {
        Log.i(TAG, String.format("prev:%d, cur:%d", prev, cur));
    }
});
final int count = 3;
customScrollView.setAdapter(new CustomScrollView.Adapter() {
    @Override
    public int getCount() {
        return count;
    }

    @Override
    public View getView(CustomScrollView parent, int pos) {
        View view = new ImageView(SecondActivity.this);
        view.setBackgroundResource(R.drawable.test_image);
        return view;
    }
});
```

### Introduce to custom attributes （自定义属性说明）

* app:cs_item_width [dimension default:200px]
* app:cs_item_height [dimension default:200px * 4/3]
* app:cs_item_large_width [dimension default:two times of cs_item_width]
* app:cs_item_large_height [dimension default:two times of cs_item_height]
* app:cs_item_margin [dimension default:10px]
* app:cs_scroll_factor [float default:0.5f] --> Affect scroll sensibility ([real scroll distance] = [pointer scroll distance] * [scroll factor] )
* app:cs_scroll_item_least_distance [dimension default:40px] --> The least distance scroll to next item
* app:cs_scroll_speed [enum("fast", "normal", "slow") default:normal] 

### The MIT License (MIT)

E-mail:xiaohui_hubei@163.com

Copyright (c) 2015 WuHui.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

[1]:https://bintray.com/whinc/maven/customscrollview/view
