
### CustomScrollView

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
    compile 'com.whinc.widget:customscrollview:1.0.0'
}
```

### How to use

Use in xml layout file:

```
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    >
    <com.whinc.widget.CustomScrollView
        android:id="@+id/custom_scrollView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:cs_item_width="100"
        app:cs_item_height="200"
        app:cs_item_large_width="300"
        app:cs_item_large_height="600"
        app:cs_item_margin="20"
        app:cs_item_scroll_factor="0.5"
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

[1]:https://bintray.com/whinc/maven/customscrollview/view
