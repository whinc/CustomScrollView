package com.whinc.customscrollview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.whinc.util.Log;
import com.whinc.widget.CustomScrollView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SecondActivity extends AppCompatActivity {
    private static final String TAG = SecondActivity.class.getSimpleName();
    private static final int ANIM_DURATION = 700;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.container_layout)
    ViewGroup mContainerLayout;
    @Bind(R.id.customScrollView)
    CustomScrollView mCustomScrollView;
    @Bind(R.id.tips_tv)
    TextView mTipsTxtView;

    private AnimatorSet mAnimatorSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.mipmap.ic_launcher);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private AnimationData mAnimData = new AnimationData();

    @OnClick({R.id.green_iv, R.id.blue_iv, R.id.red_iv})
    protected void showScrollView(final View view) {
        mCustomScrollView.setAdapter(new ScrollAdapter(3, this));

        // initialize
        mCustomScrollView.setPivotX(0.5f);
        mCustomScrollView.setPivotY(0.5f);
        // keep scrollview size same as the large item's size at begin
        ViewGroup.LayoutParams lp = mCustomScrollView.getLayoutParams();
        lp.width = mCustomScrollView.getItemLargeWidth();
        lp.height = mCustomScrollView.getItemLargeHeight();
        mCustomScrollView.setLayoutParams(lp);

        mContainerLayout.setVisibility(View.VISIBLE);

        // make sure scrollview has been measured
        mContainerLayout.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup parent = (ViewGroup) mCustomScrollView.getParent();
                int parentW = parent.getWidth();
                int parentH = parent.getHeight();
                int viewW = view.getWidth();
                int viewH = view.getHeight();
                int scrollViewW = mCustomScrollView.getWidth();
                int scrollViewH = mCustomScrollView.getHeight();

                // scale to specified size and translate to specified position simultaneously
                int[] loc = new int[2];
                view.getLocationOnScreen(loc);
                int startX = loc[0];
                int startY = loc[1];
                mCustomScrollView.getLocationOnScreen(loc);
                int endX = loc[0];
                int endY = loc[1];
                mAnimData.fromTranslationX = startX - endX;
                mAnimData.toTranslationX = 0.0f;
                mAnimData.fromTranslationY = startY - endY;
                mAnimData.toTranslationY = 0.0f;
                ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "translationX", mAnimData.fromTranslationX, mAnimData.toTranslationX);
                ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "translationY", mAnimData.fromTranslationY, mAnimData.toTranslationY);

                mAnimData.fromScaleX = viewW * 1.0f / scrollViewW;
                mAnimData.toScaleX = 1.0f;
                ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "scaleX", mAnimData.fromScaleX, mAnimData.toScaleX);

                mAnimData.fromScaleY = viewH * 1.0f / scrollViewH;
                mAnimData.toScaleY = 1.0f;
                ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "scaleY", mAnimData.fromScaleY, mAnimData.toScaleY);

                mAnimData.duration1 = ANIM_DURATION;
                AnimatorSet stage1 = new AnimatorSet();
                stage1.playTogether(translateXAnimator, translateYAnimator, scaleXAnimator, scaleYAnimator);
                stage1.setDuration(mAnimData.duration1);
                stage1.setInterpolator(new AccelerateDecelerateInterpolator());
                stage1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mCustomScrollView.setVisibility(View.VISIBLE);
                    }
                });

                // unfold animation
                mAnimData.fromWidth = mCustomScrollView.getItemLargeWidth();
                mAnimData.toWidth = parentW;
                ObjectAnimator unfoldXAnimator = ObjectAnimator.ofInt(mCustomScrollView, new Property<CustomScrollView, Integer>(Integer.class, "") {
                    @Override
                    public Integer get(CustomScrollView object) {
                        return mCustomScrollView.getLayoutParams().width;
                    }

                    @Override
                    public void set(CustomScrollView object, Integer value) {
                        ViewGroup.LayoutParams lp = mCustomScrollView.getLayoutParams();
                        lp.width = value;
                        mCustomScrollView.setLayoutParams(lp);
                    }
                }, mAnimData.fromWidth, mAnimData.toWidth);

                mAnimData.fromAlpha = 0.0f;
                mAnimData.toAlpha = 1.0f;
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTipsTxtView, "alpha", mAnimData.fromAlpha, mAnimData.toAlpha);

                mAnimData.duration2 = ANIM_DURATION;
                AnimatorSet stage2 = new AnimatorSet();
                stage2.playTogether(unfoldXAnimator, alphaAnimator);
                stage2.setDuration(mAnimData.duration2);
                stage2.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mTipsTxtView.setVisibility(View.VISIBLE);
                    }
                });

                mAnimatorSet = new AnimatorSet();
                mAnimatorSet.play(stage1).before(stage2);
                mAnimatorSet.start();
            }
        });
    }

    @OnClick(R.id.container_layout)
    protected void hideScrollView() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.end();
        }

        // reverse animation data
        Log.i(TAG, mAnimData.toString());
        mAnimData = mAnimData.reverse();
        Log.i(TAG, mAnimData.toString());

        // fold animation
        ObjectAnimator unfoldXAnimator = ObjectAnimator.ofInt(mCustomScrollView, new Property<CustomScrollView, Integer>(Integer.class, "") {
            @Override
            public Integer get(CustomScrollView object) {
                return mCustomScrollView.getLayoutParams().width;
            }

            @Override
            public void set(CustomScrollView object, Integer value) {
                ViewGroup.LayoutParams lp = mCustomScrollView.getLayoutParams();
                lp.width = value;
                mCustomScrollView.setLayoutParams(lp);
            }
        }, mAnimData.fromWidth, mAnimData.toWidth);

        // alpha animation
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTipsTxtView, "alpha", mAnimData.fromAlpha, mAnimData.toAlpha);

        AnimatorSet stage1 = new AnimatorSet();
        stage1.playTogether(unfoldXAnimator, alphaAnimator);
        stage1.setDuration(mAnimData.duration1);
        stage1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mTipsTxtView.setVisibility(View.VISIBLE);
            }
        });

        ObjectAnimator translateXAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "translationX", mAnimData.fromTranslationX, mAnimData.toTranslationX);
        ObjectAnimator translateYAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "translationY", mAnimData.fromTranslationY, mAnimData.toTranslationY);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "scaleX", mAnimData.fromScaleX, mAnimData.toScaleX);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(mCustomScrollView, "scaleY", mAnimData.fromScaleY, mAnimData.toScaleY);

        AnimatorSet stage2 = new AnimatorSet();
        stage2.playTogether(translateXAnimator, translateYAnimator, scaleXAnimator, scaleYAnimator);
        stage2.setDuration(mAnimData.duration2);
        stage2.setInterpolator(new AccelerateDecelerateInterpolator());
        stage2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mContainerLayout.setVisibility(View.GONE);
                mCustomScrollView.setVisibility(View.GONE);

                // reset
                mCustomScrollView.setTranslationX(0);
                mCustomScrollView.setTranslationY(0);
                mCustomScrollView.setScaleX(0);
                mCustomScrollView.setScaleY(0);
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(stage1).before(stage2);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTipsTxtView.setVisibility(View.GONE);
            }
        });
        mAnimatorSet.start();

    }


    static class ScrollAdapter implements CustomScrollView.Adapter {
        private final int mCount;
        private final Context mContext;

        public ScrollAdapter(int count, Context context) {
            mCount = count;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mCount * 5;    // 模拟出更多的Item，这样可以左右滑动很久
        }

        @Override
        public View getView(int position, View convertView, CustomScrollView parent) {
            View view = convertView;

            // create new view if no recycle view, otherwise reuse
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.scrollview_item, parent, false);
                ViewHolder viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
                Log.i(TAG, "create a new view");
            } else {
                Log.i(TAG, "reuse view");
            }

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.imgView.setImageResource(R.drawable.img1);

            return view;
        }

        static class ViewHolder {
            View rootView;
            @Bind(R.id.imageView)
            ImageView imgView;

            public ViewHolder(View view) {
                rootView = view;
                ButterKnife.bind(this, view);
            }
        }
    }

    static class AnimationData {
        public float fromTranslationX;
        public float toTranslationX;
        public float fromTranslationY;
        public float toTranslationY;
        public float fromScaleX;
        public float toScaleX;
        public float fromScaleY;
        public float toScaleY;
        public float fromAlpha;
        public float toAlpha;
        public int fromWidth;
        public int toWidth;
        public int duration1;
        public int duration2;

        // reverse animation data
        public AnimationData reverse() {
            AnimationData animData = new AnimationData();
            animData.fromTranslationX = toTranslationX;
            animData.toTranslationX = fromTranslationX;
            animData.fromTranslationY = toTranslationY;
            animData.toTranslationY = fromTranslationY;
            animData.fromScaleX = toScaleX;
            animData.toScaleX = fromScaleX;
            animData.fromScaleY = toScaleY;
            animData.toScaleY = fromScaleY;
            animData.fromAlpha = toAlpha;
            animData.toAlpha = fromAlpha;
            animData.fromWidth = toWidth;
            animData.toWidth = fromWidth;
            animData.duration2 = duration1;
            animData.duration1 = duration2;
            return animData;
        }

        @Override
        public String toString() {
            return String.format("alpha(%.0f->%.0f), width(%d->%d)", fromAlpha, toAlpha, fromWidth, toWidth);
        }
    }
}

