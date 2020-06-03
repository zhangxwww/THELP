package com.example.thelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import cn.carbs.android.avatarimageview.library.AvatarImageView;

public class HandlerDetailActivity extends AppCompatActivity {
    RelativeLayout bottomSheet;
    BottomSheetBehavior<RelativeLayout> behavior;
    String picUrl = "https://overwatch.nosdn.127.net/2/heroes/Echo/hero-select-portrait.png";
    AvatarImageView aiv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_detail);
        //底部抽屉栏展示地址
        bottomSheet = findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        aiv = (AvatarImageView) this.findViewById(R.id.order_avatar_image);
        setBottomSheet();
        Glide
                .with(this)
                .load(picUrl)
                .centerCrop()
                .into(aiv);
    }

    void setBottomSheet(){
        TextView detailsTv = findViewById(R.id.order_details_tv);
        ViewGroup.LayoutParams lp = detailsTv.getLayoutParams();
        detailsTv.setMovementMethod(ScrollingMovementMethod.getInstance());
        int oldHeight = lp.height;
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, @BottomSheetBehavior.State int newState) {
                String state = "null";
                switch (newState) {
                    case 1:
                        state = "STATE_DRAGGING";//过渡状态此时用户正在向上或者向下拖动bottom sheet
                        break;
                    case 2:
                        state = "STATE_SETTLING"; // 视图从脱离手指自由滑动到最终停下的这一小段时间
                        lp.height = oldHeight;
                        detailsTv.setLayoutParams(lp);
                        break;
                    case 3:
                        state = "STATE_EXPANDED"; //处于完全展开的状态
//                        Log.d("state", "expanded:" );
//                        TextView detailsTv = findViewById(R.id.order_details_tv);
//                        int oldH = detailsTv.getHeight();
//                        Log.d("oldHeight", "height:"+oldH );
//                        Log.d("getLineCount", "height:"+detailsTv.getLineCount() );
//                        Log.d("getLineHeight", "height:"+detailsTv.getLineHeight() );

                        lp.height = oldHeight+300;
                        detailsTv.setLayoutParams(lp);
//                        int newH = detailsTv.getHeight();
//                        Log.d("newHeight", "height:"+newH );
                        break;
                    case 4:
                        state = "STATE_COLLAPSED"; //默认的折叠状态
                        lp.height = oldHeight;
                        detailsTv.setLayoutParams(lp);
                        break;
                    case 5:
                        state = "STATE_HIDDEN"; //下滑动完全隐藏 bottom sheet
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("BottomSheetDemo", "slideOffset:" + slideOffset);
            }
        });

    }
}
