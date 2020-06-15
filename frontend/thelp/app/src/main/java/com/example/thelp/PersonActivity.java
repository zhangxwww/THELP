package com.example.thelp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.avatarimageview.library.AvatarImageView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.data.Order;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.example.util.SHA;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Signature;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PersonActivity extends AppCompatActivity {

    @BindView(R.id.avatar)
    AvatarImageView avatarView;

    @BindView(R.id.person_name)
    TextView nameView;

    @BindView(R.id.person_score)
    TextView scoreView;

    @BindView(R.id.name)
    TextView smallNameView;

    @BindView(R.id.phone)
    TextView phoneView;

    @BindView(R.id.signature)
    TextView signatureView;

    @BindView(R.id.password)
    TextView passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);

        TextView modifyNameButton = findViewById(R.id.modify_name_button);
        TextView modifyPhoneButton = findViewById(R.id.modify_phone_button);
        TextView modifyPasswordButton = findViewById(R.id.modify_password_button);
        TextView modifySignatureButton = findViewById(R.id.modify_signature_button);

        ImageButton navBack = findViewById(R.id.button_back);
        navBack.setOnClickListener(v -> backToMainActivity());

        Context ctx = this;

        showUserInfo();

        modifyNameButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx)
                    .setTitle("修改用户名")
                    .setMessage("请输入修改后的内容并点击确定");
            View view = getLayoutInflater().inflate(R.layout.input_modify, null);
            builder.setView(view);
            builder.setNegativeButton("确定", (dialog, which) -> {
                TextInputEditText textInputEditText = view.findViewById(R.id.modify_text);
                String name = Objects.requireNonNull(textInputEditText.getText()).toString();
                CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                if (name.length() >= 11) {
                    dialog.cancel();
                    Snackbar.make(cl, getResources().getString(R.string.helper_text), Snackbar.LENGTH_LONG).show();
                } else if (name.length() == 0) {
                    dialog.cancel();
                    Snackbar.make(cl, getResources().getString(R.string.name_required), Snackbar.LENGTH_LONG).show();
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("nickname", name);
                    JsonObjectRequest req = RequestFactory.getUserEditRequest(
                            map, getResources().getString(R.string.url),
                            response -> {
                                try {
                                    boolean success = response.getBoolean("success");
                                    if (success) {
                                        nameView.setText(name);
                                        smallNameView.setText(name);
                                        dialog.dismiss();
                                        Snackbar.make(cl, getResources().getString(R.string.edit_succeed), Snackbar.LENGTH_LONG).show();
                                    } else {
                                        String error = response.getString("error_msg");
                                        Snackbar.make(cl, error, Snackbar.LENGTH_LONG).show();
                                        Log.d("Error Msg", error);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> Log.d("PersonEdit", "Fail " + error.getMessage())
                    );
                    MySingleton.getInstance(PersonActivity.this).addToRequestQueue(req);
                }
            })
                    .setPositiveButton("取消", null)
                    .show();
        });


        modifyPhoneButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx)
                    .setTitle("修改手机号")
                    .setMessage("请输入修改后的内容并点击确定");

            View view = getLayoutInflater().inflate(R.layout.input_modify, null);
            builder.setView(view);
            builder.setNegativeButton("确定", (dialog, which) -> {
                TextInputEditText textInputEditText = view.findViewById(R.id.modify_text);
                String phone = Objects.requireNonNull(textInputEditText.getText()).toString();
                CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                if (phone.length() != 11) {
                    dialog.cancel();
                    Snackbar.make(cl, getResources().getString(R.string.helper_text), Snackbar.LENGTH_LONG).show();
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("phone", phone);
                    JsonObjectRequest req = RequestFactory.getUserEditRequest(
                            map, getResources().getString(R.string.url),
                            response -> {
                                try {
                                    boolean success = response.getBoolean("success");
                                    if (success) {
                                        phoneView.setText(phone);
                                        dialog.dismiss();
                                        Snackbar.make(cl, getResources().getString(R.string.edit_succeed), Snackbar.LENGTH_LONG).show();
                                    } else {
                                        String error = response.getString("error_msg");
                                        Snackbar.make(cl, error, Snackbar.LENGTH_LONG).show();
                                        Log.d("Error Msg", error);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            },
                            error -> Log.d("PersonEdit", "Fail " + error.getMessage())
                    );
                    MySingleton.getInstance(PersonActivity.this).addToRequestQueue(req);
                }
            })
                    .setPositiveButton("取消", null)
                    .show();
        });

        modifySignatureButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx)
                    .setTitle("修改签名")
                    .setMessage("请输入修改后的内容并点击确定");

            View view = getLayoutInflater().inflate(R.layout.input_modify, null);
            builder.setView(view);
            builder.setNegativeButton("确定", (dialog, which) -> {
                TextInputEditText textInputEditText = view.findViewById(R.id.modify_text);
                String signature = Objects.requireNonNull(textInputEditText.getText()).toString();
                CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                HashMap<String, String> map = new HashMap<>();
                map.put("signature", signature);
                JsonObjectRequest req = RequestFactory.getUserEditRequest(
                        map, getResources().getString(R.string.url),
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    signatureView.setText(signature);
                                    dialog.dismiss();
                                    Snackbar.make(cl, getResources().getString(R.string.edit_succeed), Snackbar.LENGTH_LONG).show();
                                } else {
                                    String error = response.getString("error_msg");
                                    Snackbar.make(cl, error, Snackbar.LENGTH_LONG).show();
                                    Log.d("Error Msg", error);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.d("PersonEdit", "Fail " + error.getMessage())
                );
                MySingleton.getInstance(PersonActivity.this).addToRequestQueue(req);
            })
                    .setPositiveButton("取消", null)
                    .show();
        });

        modifyPasswordButton.setOnClickListener(v -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx)
                    .setTitle("修改密码")
                    .setMessage("请输入修改前后的密码并点击确定");

            View view = getLayoutInflater().inflate(R.layout.password_modify, null);
            builder.setView(view);
            builder.setNegativeButton("确定", (dialog, which) -> {
                TextInputEditText oldText = view.findViewById(R.id.old_text);
                TextInputEditText newText = view.findViewById(R.id.new_text);
                String oldPassword = Objects.requireNonNull(oldText.getText()).toString();
                String newPassword = Objects.requireNonNull(newText.getText()).toString();
                CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                if (oldPassword.length() == 0) {
                    Snackbar.make(cl, getResources().getString(R.string.modify_password_old_hint), Snackbar.LENGTH_LONG).show();
                }
                if (newPassword.length() == 0) {
                    Snackbar.make(cl, getResources().getString(R.string.modify_password_new_hint), Snackbar.LENGTH_LONG).show();
                }
                HashMap<String, String> map = new HashMap<>();
                map.put("password_old", SHA.parse(oldPassword));
                map.put("password_new", SHA.parse(newPassword));
                JsonObjectRequest req = RequestFactory.getUserEditRequest(
                        map, getResources().getString(R.string.url),
                        response -> {
                            try {
                                boolean success = response.getBoolean("success");
                                if (success) {
                                    dialog.dismiss();
                                    Snackbar.make(cl, getResources().getString(R.string.edit_succeed), Snackbar.LENGTH_LONG).show();
                                } else {
                                    String error = response.getString("error_msg");
                                    Snackbar.make(cl, error, Snackbar.LENGTH_LONG).show();
                                    Log.d("Error Msg", error);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> Log.d("PersonEdit", "Fail " + error.getMessage())
                );
                MySingleton.getInstance(PersonActivity.this).addToRequestQueue(req);
            })
                    .setPositiveButton("取消", null)
                    .show();
        });
    }

    private void showUserInfo() {
        UserInfo userInfo = ((myApplication) getApplicationContext()).getUserInfo();
        Glide
                .with(this)
                .load(userInfo.avatar)
                .centerCrop()
                .into(avatarView);
        nameView.setText(userInfo.nickName);
        smallNameView.setText(userInfo.nickName);
        scoreView.setText(String.valueOf(userInfo.score));
        phoneView.setText(userInfo.phone);
        signatureView.setText(userInfo.signature);
    }

    private void backToMainActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
