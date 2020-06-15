package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PersonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        TextView modifyPhoneButton = findViewById(R.id.modify_phone_button);
        TextView modifyPasswordButton = findViewById(R.id.modify_password_button);
        TextView modifySignatureButton = findViewById(R.id.modify_signature_button);

        ImageButton navBack = findViewById(R.id.button_back);
        navBack.setOnClickListener(v -> backToMainActivity());

        Context ctx = this;

        modifyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ctx)
                        .setTitle("修改手机号")
                        .setMessage("请输入修改后的内容并点击确定")
                        .setView(R.layout.input_modify)
                        .setNegativeButton("确定", null)
                        .setPositiveButton("取消", null)
                        .show();
            }
        });

        modifySignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ctx)
                        .setTitle("修改签名")
                        .setMessage("请输入修改后的内容并点击确定")
                        .setView(R.layout.input_modify)
                        .setNegativeButton("确定", null)
                        .setPositiveButton("取消", null)
                        .show();
            }
        });

        modifyPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(ctx)
                        .setTitle("修改密码")
                        .setMessage("请输入修改后的内容并点击确定")
                        .setView(R.layout.input_modify)
                        .setNegativeButton("确定", null)
                        .setPositiveButton("取消", null)
                        .show();
            }
        });
    }

    private void backToMainActivity() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
