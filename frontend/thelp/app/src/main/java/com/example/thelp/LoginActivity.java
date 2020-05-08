package com.example.thelp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.request.MySingleton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText usernameEdit;
    private TextInputEditText passwordEdit;

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
        map.put("phone", Objects.requireNonNull(usernameEdit.getText()).toString());
        map.put("password", Objects.requireNonNull(passwordEdit.getText()).toString());
        String json = new Gson().toJson(map);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest signupRequest = new JsonObjectRequest(
                Request.Method.POST,
                postUrl,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SIGN", String.valueOf(response));
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
        map.put("phone", Objects.requireNonNull(usernameEdit.getText()).toString());
        map.put("password", Objects.requireNonNull(passwordEdit.getText()).toString());
        String json = new Gson().toJson(map);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest signupRequest = new JsonObjectRequest(
                Request.Method.POST,
                postUrl,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("SIGN", String.valueOf(response));
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
}
