package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.tv.TvContentRating;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;

import org.json.JSONException;

import java.util.Objects;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.avatarimageview.library.AvatarImageView;

public class AssessActivity extends AppCompatActivity {
    RatingBar ratingBar = null;

    @BindView(R.id.submit_button)
    Button submitButton;

    @BindView(R.id.avatar)
    AvatarImageView avatarImageView;

    @BindView(R.id.name)
    TextView nameView;

    @BindView(R.id.signature)
    TextView signatureView;

    @BindView(R.id.score_view)
    TextView scoreView;

    @BindView(R.id.assess_description)
    TextView descView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assess);
        ButterKnife.bind(this);

        int orderId = Objects.requireNonNull(getIntent().getExtras()).getInt("ORDER_ID");
        int handlerId = Objects.requireNonNull(getIntent().getExtras()).getInt("HANDLER_ID");
        setRatingBar();
        bindEvent(orderId);
        new Thread(() -> getHandlerInfo(handlerId)).start();
    }

    private void setRatingBar() {
        ratingBar = findViewById(R.id.assess_star);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                int descId = R.string.assess_l5;
                switch ((int) rating) {
                    case 1:
                        descId = R.string.assess_l1;
                        break;
                    case 2:
                        descId = R.string.assess_l2;
                        break;
                    case 3:
                        descId = R.string.assess_l3;
                        break;
                    case 4:
                        descId = R.string.assess_l4;
                        break;
                    case 5:
                        descId = R.string.assess_l5;
                        break;
                    default:
                        break;
                }
                int finalDescId1 = descId;
                descView.post(() -> descView.setText(getResources().getString(finalDescId1)));
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showHandlerInfo(UserInfo userInfo) {
        Glide
                .with(this)
                .load(userInfo.avatar)
                .centerCrop()
                .into(avatarImageView);
        nameView.post(() -> nameView.setText(userInfo.nickName));
        signatureView.post(() -> signatureView.setText(userInfo.signature));
        scoreView.post(() -> scoreView.setText("评分" + userInfo.score));
    }

    private void getHandlerInfo(int handlerId) {
        JsonObjectRequest req = RequestFactory.getUserInfoRequest(
                handlerId,
                getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            UserInfo userInfo = UserInfo.parseFromJSONResponse(response);
                            showHandlerInfo(userInfo);
                        } else {
                            String error = response.getString("error_msg");
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("Assess", "Fail " + error.getMessage())
        );
        if (req != null) {
            MySingleton.getInstance(this).addToRequestQueue(req);
        }
    }

    private void bindEvent(int orderId) {
        submitButton.setOnClickListener(v -> {
            int rate = (int) ratingBar.getRating();
            JsonObjectRequest req = RequestFactory.getOrderAssessRequest(
                    orderId,
                    rate,
                    getResources().getString(R.string.url),
                    response -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                backToDetailActivity();
                            } else {
                                String error = response.getString("error_msg");
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("Assess", "Fail " + error.getMessage())
            );
            if (req != null) {
                MySingleton.getInstance(this).addToRequestQueue(req);
            }
        });
    }

    private void backToDetailActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
