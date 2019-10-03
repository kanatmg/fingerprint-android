package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText emailText;
    private EditText passwordText;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setSupportActionBar(toolbar);

        btnLogin = (Button) findViewById(R.id.login);
        emailText = (EditText) findViewById(R.id.emailField);
        passwordText = (EditText) findViewById(R.id.passwordField);

        session = new Session(this);

        System.out.println("Sessionsdadasdasasdsasaddsds: "+session.getOrganizationId());

        if(session.getLoggedStatus()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                if(TextUtils.isEmpty(email)){
                    emailText.setError("Введите E-mail");
                    return;
                }
                if(!isValidEmail(email)){
                    emailText.setError("Не валидный E-mail");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    passwordText.setError("Введите Пароль");
                    return;
                }
                if(password.length() < 5) {
                    passwordText.setError("Пароль должен состоять минимум из 6 символов");
                    return;
                }
                emailText.setError(null);
                passwordText.setError(null);
                loginRequest(email,password);
            }
        });
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    private void loginRequest(String email, String password) {
        Retrofit retrofit = null;

        retrofit = NetworkClient.getRetrofitClient(this);

        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        //RequestBody name1 = RequestBody.create(okhttp3.FormBody, name);
        Call<Organization> call = uploadAPIs.login(email,password);
        call.enqueue(new Callback<Organization>() {
            @Override
            public void onResponse(Call<Organization> call,
                                   Response<Organization> response) {
                Toast toast = null;
                if(response.body().getId() != null) {

                    Integer organizationId = response.body().getId();
                    String organizationName = response.body().getName();
                    String  organizationIdText = Integer.toString(organizationId);
                    session.setOrganizationId(organizationIdText);
                    session.setOrganizationName(organizationName);
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(mainIntent);
                    session.setLoggedIn(true);
                    finish();
                }else{
                    toast = Toast.makeText(getApplicationContext(),
                            "Неверный логин или пароль", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
            @Override
            public void onFailure(Call<Organization> call, Throwable t) {
                Log.e("Upload error:", t.getMessage());
            }
        });
    }
    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

}
