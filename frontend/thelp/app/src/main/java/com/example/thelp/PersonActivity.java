package com.example.thelp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbs.android.avatarimageview.library.AvatarImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.data.GlideEngine;
import com.example.data.Message;
import com.example.data.Order;
import com.example.data.UserInfo;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.example.util.SHA;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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

        int userIdentification = Objects.requireNonNull(
                getIntent().getExtras()).getInt(UserInfo.USER_IDENTIFICATION);

        setModifyButtons(userIdentification);
        setContactButtons(userIdentification);
        showUserInfo(userIdentification);
        setNavButton();




    }

    private void setNavButton() {
        ImageButton navBack = findViewById(R.id.button_back);
        navBack.setOnClickListener(v -> {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void setContactButtons(int userIdentification) {
        ImageButton textButton = findViewById(R.id.button_text);
        ImageButton callButton = findViewById(R.id.button_phone);
        if (userIdentification == UserInfo.USER_SELF) {
            textButton.setVisibility(View.INVISIBLE);
            callButton.setVisibility(View.INVISIBLE);
        } else if (userIdentification == UserInfo.USER_OTHERS) {
            textButton.setOnClickListener(v -> {
                Intent intent = new Intent(PersonActivity.this, ChatActivity.class);
                UserInfo otherUserInfo = (UserInfo) Objects.requireNonNull(
                        getIntent().getSerializableExtra(UserInfo.USER_INFO));
                intent.putExtra(UserInfo.USER_INFO, otherUserInfo);
                startActivity(intent);
            });
        }
    }

    private void setModifyButtons(int userIdentification) {
        Context ctx = this;
        TextView modifyNameButton = findViewById(R.id.modify_name_button);
        TextView modifyPhoneButton = findViewById(R.id.modify_phone_button);
        TextView modifyPasswordButton = findViewById(R.id.modify_password_button);
        TextView modifySignatureButton = findViewById(R.id.modify_signature_button);

        if (userIdentification == UserInfo.USER_OTHERS) {
            modifyNameButton.setVisibility(View.INVISIBLE);
            modifyPhoneButton.setVisibility(View.INVISIBLE);
            modifyPasswordButton.setVisibility(View.INVISIBLE);
            modifySignatureButton.setVisibility(View.INVISIBLE);
            LinearLayout password = findViewById(R.id.password_layout);
            password.setVisibility(View.INVISIBLE);
        } else if (userIdentification == UserInfo.USER_SELF) {
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
                    if (name.length() >= 9) {
                        dialog.cancel();
                        Snackbar.make(cl, getResources().getString(R.string.username_helper_text), Snackbar.LENGTH_LONG).show();
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
                                            ((myApplication) getApplicationContext()).getUserInfo().setNickName(name);
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
                                            ((myApplication) getApplicationContext()).getUserInfo().setPhone(phone);
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
                                        ((myApplication) getApplicationContext()).getUserInfo().setSignature(signature);
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

            avatarView.setOnClickListener(v -> {
                PictureSelector.create(this)
                        .openGallery(PictureMimeType.ofAll())
                        .loadImageEngine(GlideEngine.createGlideEngine())
                        .maxSelectNum(1)
                        .minSelectNum(1)
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                if (result.size() > 0) {
                                    CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                                    String photoPath = result.get(0).getRealPath();

                                    new Thread(() -> {
                                        File file = new File(photoPath);
                                        RequestFactory.uploadFile(
                                                file,
                                                getResources().getString(R.string.url),
                                                "/user",
                                                new Callback() {
                                                    @Override
                                                    public void onFailure(Call call, IOException e) {
                                                        e.printStackTrace();
                                                        Snackbar.make(cl, getResources().getString(R.string.upload_failed), Snackbar.LENGTH_LONG).show();
                                                    }

                                                    @Override
                                                    public void onResponse(Call call, Response response) throws IOException {
                                                        Snackbar.make(cl, getResources().getString(R.string.upload_succeed), Snackbar.LENGTH_LONG).show();
                                                        updateUserInfo();
                                                    }
                                                });
                                    }).start();
                                }
                            }

                            @Override
                            public void onCancel() {
                                // onCancel Callback
                            }
                        });
            });
        }
    }

    private void showUserInfo(int userIdentification) {
        UserInfo userInfo = null;
        if (userIdentification == UserInfo.USER_SELF) {
            userInfo = ((myApplication) getApplicationContext()).getUserInfo();
        } else if (userIdentification == UserInfo.USER_OTHERS) {
            userInfo = (UserInfo) Objects.requireNonNull(
                    getIntent().getSerializableExtra(UserInfo.USER_INFO));
        }

        Glide
                .with(this)
                .load(userInfo.avatar)
                .centerCrop()
                .into(avatarView);
        nameView.setText(userInfo.nickName);
        smallNameView.setText(userInfo.nickName);
        scoreView.setText("评分" + userInfo.score);
        phoneView.setText(userInfo.phone);
        signatureView.setText(userInfo.signature);
    }

    private void updateUserInfo() {
        JsonObjectRequest infoRequest = RequestFactory.getUserInfoRequest(
                null,
                getResources().getString(R.string.url),
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            myApplication myApp = (myApplication) getApplicationContext();
                            UserInfo userInfo = UserInfo.parseFromJSONResponse(response);
                            myApp.setUserInfo(userInfo);
                            showUserInfo(UserInfo.USER_SELF);
                        } else {
                            CoordinatorLayout cl = findViewById(R.id.person_activity_bg);
                            String error = response.getString("error_msg");
                            Snackbar.make(cl, error, Snackbar.LENGTH_SHORT).show();
                            Log.d("Error Msg", error);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.d("INFO", "Fail " + error.getMessage())
        );

        if (infoRequest != null) {
            MySingleton.getInstance(this).addToRequestQueue(infoRequest);
        }
    }

    private void backToMainActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
