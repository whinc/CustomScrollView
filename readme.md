
### CustomScrollView

![screenshot](screenshot.png)

### Integration （集成）

I host this lib in [jcenter][1] but it has not been published yet.

You can use in gradle like below:

```
repositories {
    maven {
        url 'https://dl.bintray.com/whinc/maven'
    }
}

dependencies {
    ...
    compile 'com.whinc.widget:customscrollview:1.0.4'
}
```

### How to use （如何使用）

Use in xml layout file:

```
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    >
    <com.whinc.widget.CustomScrollView
        android:id="@+id/custom_scrollView"
        android:layout_width="675px"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:cs_item_width="200px"
        app:cs_item_height="240px"
        app:cs_item_large_width="350px"
        app:cs_item_large_height="450px"
        app:cs_item_margin="20px"
        app:cs_item_scroll_factor="0.5"
        app:cs_item_touch_diff="40px"
        />
```

```
mCustomScrollView = (CustomScrollView) findViewById(R.id.custom_scrollView);
mCustomScrollView.setInterpolator(new OvershootInterpolator());
mCustomScrollView.setAdapter(new CustomScrollView.Adapter() {
    @Override
    public int getCount() {
        return finalN;
    }

    @Override
    public View getView(ViewGroup parent) {
        View view = new ImageView(MainActivity.this);
        view.setBackgroundResource(R.drawable.image);
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
* app:cs_item_scroll_factor [float default:0.5f] --> Affect the scroll speed, the more large this value scroll more faster
* app:cs_item_touch_diff [dimension default:40px] --> When touch up if the distance the center large item offset center line large then this value,
     * ScrollView will scroll to next item automatically

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
