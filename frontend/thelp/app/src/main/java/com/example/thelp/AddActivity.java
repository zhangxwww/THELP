package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddActivity extends AppCompatActivity implements OnDateSetListener {
    private TimePickerDialog startTimePickerDialog;
    private TimePickerDialog endTimePickerDialog;
    private TextInputLayout startTime;
    private TextInputLayout endTime;
    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        setupDateTimePicker();
        setupToolbar();
    }

    @Override
    public void onDateSet(TimePickerDialog dialog, long count) {
        String text = getDateToString(count);
        if (dialog == startTimePickerDialog) {
            Objects.requireNonNull(startTime.getEditText()).setText(text);
        } else if (dialog == endTimePickerDialog) {
            Objects.requireNonNull(endTime.getEditText()).setText(text);
        }
    }

    public String getDateToString(long time) {
        Date d = new Date(time);
        return sf.format(d);
    }

    private void setupToolbar() {
        Button submitBtn = findViewById(R.id.submit_button);
        submitBtn.setOnClickListener(v -> submit());
        Button navBack = findViewById(R.id.nav_back_button);
        navBack.setOnClickListener(v -> backToMainActivity());
        Toolbar toolbar = findViewById(R.id.app_bar);
        toolbar.setNavigationOnClickListener(v -> backToMainActivity());
    }

    private void setupDateTimePicker() {
        startTime = findViewById(R.id.order_start_time);
        endTime = findViewById(R.id.order_end_time);
        long tenYears = 10L * 365 * 1000 * 60 * 60 * 24L;

        startTimePickerDialog = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确认")
                .setTitleStringId("开始时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.colorAccent))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(16)
                .build();

        endTimePickerDialog = new TimePickerDialog.Builder()
                .setCallBack(this)
                .setCancelStringId("取消")
                .setSureStringId("确认")
                .setTitleStringId("结束时间")
                .setYearText("年")
                .setMonthText("月")
                .setDayText("日")
                .setHourText("时")
                .setMinuteText("分")
                .setCyclic(false)
                .setMinMillseconds(System.currentTimeMillis())
                .setMaxMillseconds(System.currentTimeMillis() + tenYears)
                .setCurrentMillseconds(System.currentTimeMillis())
                .setThemeColor(getResources().getColor(R.color.colorAccent))
                .setType(Type.ALL)
                .setWheelItemTextNormalColor(getResources().getColor(R.color.timetimepicker_default_text_color))
                .setWheelItemTextSelectorColor(getResources().getColor(R.color.timepicker_toolbar_bg))
                .setWheelItemTextSize(16)
                .build();

        Button reviseStartTimeButton = findViewById(R.id.button_revise_start_time);
        Button reviseEndTimeButton = findViewById(R.id.button_revise_end_time);

        reviseStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimePickerDialog.show(getSupportFragmentManager(), "month_day_hour_minute");
            }
        });
        reviseEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimePickerDialog.show(getSupportFragmentManager(), "month_day_hour_minute");
            }
        });
    }

    private void backToMainActivity() {
        finish();
    }

    private void submit() {
        TextInputLayout title = findViewById(R.id.order_title);
        TextInputLayout detail = findViewById(R.id.order_detail);
        TextInputLayout target = findViewById(R.id.order_target_location);
        TextInputLayout type = findViewById(R.id.order_type);
        TextInputLayout reward = findViewById(R.id.order_reward);
        TextInputLayout start = findViewById(R.id.order_start_time);
        TextInputLayout end = findViewById(R.id.order_end_time);

        String order_title = Objects.requireNonNull(title.getEditText()).getEditableText().toString();
        String order_detail = Objects.requireNonNull(detail.getEditText()).getEditableText().toString();
        String order_target = Objects.requireNonNull(target.getEditText()).getEditableText().toString();
        String order_type = Objects.requireNonNull(type.getEditText()).getEditableText().toString();
        String order_reward = Objects.requireNonNull(reward.getEditText()).getEditableText().toString();
        String order_start = Objects.requireNonNull(start.getEditText()).getEditableText().toString();
        String order_end = Objects.requireNonNull(end.getEditText()).getEditableText().toString();

        boolean hasError = false;
        if (order_title.length() == 0) {
            title.setError(getResources().getString(R.string.title_required));
            hasError = true;
        } else if (order_title.length() >= R.integer.title_max_length) {
            hasError = true;
            title.setError(getResources().getString(R.string.title_too_long));
        } else {
            title.setError("");
        }
        if (order_detail.length() == 0) {
            hasError = true;
            detail.setError(getResources().getString(R.string.detail_required));
        } else {
            detail.setError("");
        }
        if (order_target.length() == 0) {
            hasError = true;
            target.setError(getResources().getString(R.string.target_required));
        } else {
            target.setError("");
        }
        if (order_type.length() == 0) {
            hasError = true;
            type.setError(getResources().getString(R.string.type_required));
        } else if (order_type.length() >= R.integer.type_max_length) {
            hasError = true;
            type.setError(getResources().getString(R.string.type_too_long));
        } else {
            type.setError("");
        }
        if (order_reward.length() == 0) {
            hasError = true;
            reward.setError(getResources().getString(R.string.reward_required));
        } else {
            reward.setError("");
        }
        if (order_start.length() == 0) {
            start.setError(getResources().getString(R.string.start_required));
        } else {
            start.setError("");
        }
        if (order_end.length() == 0) {
            hasError = true;
            end.setError(getResources().getString(R.string.end_required));
        } else {
            end.setError("");
        }

        if (hasError) {
            return;
        }

        Map<String, String> order_info = new HashMap<>();
        order_info.put("title", Objects.requireNonNull(order_title));
        order_info.put("description", Objects.requireNonNull(order_detail));
        order_info.put("genre", Objects.requireNonNull(order_type));
        order_info.put("start_time", Objects.requireNonNull(order_start));
        order_info.put("end_time", Objects.requireNonNull(order_end));
        order_info.put("target_location", Objects.requireNonNull(order_target));
        order_info.put("reward", Objects.requireNonNull(order_reward));

        Map<String, Map<String, String>> map = new HashMap<>();
        map.put("order_info", order_info);
        String json = new Gson().toJson(map);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String postUrl = AddActivity.this.getString(R.string.url) + "/order/create";
        JsonObjectRequest addNewActivityRequest = RequestFactory.getRequest(
                Request.Method.POST,
                postUrl,
                jsonObject,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            backToMainActivity();
                        } else {
                            LinearLayout ll = findViewById(R.id.add_activity_background);
                            String error = response.getString("error_msg");
                            Snackbar.make(ll, error, Snackbar.LENGTH_SHORT).show();
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("CREATE", "Fail " + error.getMessage())
        );
        MySingleton.getInstance(this).addToRequestQueue(addNewActivityRequest);
    }
}
