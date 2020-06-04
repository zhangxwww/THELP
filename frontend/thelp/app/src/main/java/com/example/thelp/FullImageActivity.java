package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.data.FullImageInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullImageActivity extends AppCompatActivity {

    @BindView(R.id.full_image)
    ImageView fullImage;

    @BindView(R.id.full_layout)
    LinearLayout fullLayout;

    private int left;
    private int top;
    private float scaleX;
    private float scaleY;
    private Drawable background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_full_image);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onDataSyncEvent(final FullImageInfo fullImageInfo) {
        final int locationX = fullImageInfo.getLocationX();
        final int locationY = fullImageInfo.getLocationY();
        final int width = fullImageInfo.getWidth();
        final int height = fullImageInfo.getHeight();
        background = new ColorDrawable(Color.BLACK);
        fullLayout.setBackground(background);
        fullImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                fullImage.getViewTreeObserver().removeOnPreDrawListener(this);
                int[] location = new int[2];
                fullImage.getLocationOnScreen(location);
                left = locationX - location[0];
                top = locationY - location[1];
                scaleX = width * 1.0f / fullImage.getWidth();
                scaleY = height * 1.0f / fullImage.getHeight();
                showActivityEnterAnimation();
                return true;
            }
        });
        Glide.with(this).load(fullImageInfo.getImageUrl()).into(fullImage);
    }

    private void showActivityEnterAnimation() {
        fullImage.setPivotX(0);
        fullImage.setPivotY(0);
        fullImage.setScaleX(scaleX);
        fullImage.setScaleY(scaleY);
        fullImage.setTranslationX(left);
        fullImage.setTranslationY(top);
        fullImage.animate().scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(background, "alpha", 0, 255);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showActivityExitAnimation(Runnable runnable) {
        fullImage.setPivotX(0);
        fullImage.setPivotY(0);
        fullImage.animate().scaleX(scaleX).scaleY(scaleY)
                .translationX(left).translationY(top)
                .withEndAction(runnable)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(background, "alpha", 255, 0);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.setDuration(500);
        objectAnimator.start();
    }

    @OnClick(R.id.full_image)
    public void onFullImageClicked() {
        finish();
        showActivityExitAnimation(() -> {
            finish();
            overridePendingTransition(0, 0);
        });
    }
}
