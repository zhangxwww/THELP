package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.request.MySingleton;
import com.example.request.RequestFactory;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout usernameEdit;
    private TextInputLayout passwordEdit;

    private String sessionid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button signupButton = findViewById(R.id.signupButton);
        Button signinButton = findViewById(R.id.signinButton);
        usernameEdit = findViewById(R.id.usernameTextField);
        passwordEdit = findViewById(R.id.passwordTextField);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSignupButton();
            }
        });
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSigninButton();
            }
        });
    }

    private void onClickSignupButton() {
        // Sending http request and wait for response
        String postUrl = LoginActivity.this.getString(R.string.url) + "/user/signup";
        Map<String, String> map = new HashMap<>();
        map.put("phone", Objects.requireNonNull(usernameEdit.getEditText()).getText().toString());
        map.put("password", sha(Objects.requireNonNull(passwordEdit.getEditText()).getText().toString()));


        if (usernameEdit.getEditText().getText().length() != 11) {
            usernameEdit.setError(getResources().getString(R.string.helper_text));
            return;
        } else {
            usernameEdit.setError("");
        }
        if (passwordEdit.getEditText().getText().length() > 20) {
            passwordEdit.setError(getResources().getString(R.string.password_helper_text));
            return;
        } else {
            passwordEdit.setError("");
        }
        String json = new Gson().toJson(map);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest signupRequest = RequestFactory.getRequest(
                Request.Method.POST,
                postUrl,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SIGN", String.valueOf(response));
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                onClickSigninButton();
                            } else {
                                ConstraintLayout cl = findViewById(R.id.login_background);
                                String error = response.getString("error_msg");
                                Snackbar.make(cl, error, Snackbar.LENGTH_SHORT).show();
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.d("SIGN", "Fail " + e.getMessage());
                    }
                }
        );
        MySingleton.getInstance(this).addToRequestQueue(signupRequest);
    }

    private void onClickSigninButton() {
        // Sending http request and wait for response
        String postUrl = LoginActivity.this.getString(R.string.url) + "/user/login";
        Map<String, String> map = new HashMap<>();
        map.put("phone", Objects.requireNonNull(usernameEdit.getEditText()).getText().toString());
        map.put("password", sha(Objects.requireNonNull(passwordEdit.getEditText()).getText().toString()));
        if (usernameEdit.getEditText().getText().length() != 11) {
            usernameEdit.setError(getResources().getString(R.string.helper_text));
            return;
        } else {
            usernameEdit.setError("");
        }
        if (passwordEdit.getEditText().getText().length() > 20) {
            passwordEdit.setError(getResources().getString(R.string.password_helper_text));
            return;
        } else {
            System.out.println("<=20");
            passwordEdit.setError("");
        }
        String json = new Gson().toJson(map);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest signinRequest = RequestFactory.getRequest(
                Request.Method.POST,
                postUrl,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SIGN", String.valueOf(response));
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                ConstraintLayout cl = findViewById(R.id.login_background);
                                String error = response.getString("error_msg");
                                Snackbar.make(cl, error, Snackbar.LENGTH_SHORT).show();
                                Log.d("Error Msg", error);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        Log.d("SIGN", "Fail " + e.getMessage());
                    }
                }
        );
        MySingleton.getInstance(this).addToRequestQueue(signinRequest);
    }

    private String sha(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(s.getBytes());
            return new BigInteger(md.digest()).toString(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
