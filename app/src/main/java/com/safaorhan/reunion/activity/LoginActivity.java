package com.safaorhan.reunion.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.safaorhan.reunion.R;

public class LoginActivity extends AppCompatActivity {
    private static final int PASSWORD_MIN_LENGTH = 6;
    EditText emailEdit;
    EditText passwordEdit;
    Button loginButton;
    boolean isTryingToLogin = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo networkInfo = getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    Boolean emailValidation = isEmailValid(emailEdit);
                    Boolean passwValidation = isPasswValid(passwordEdit);
                    if (emailValidation && passwValidation)
                        if (!isTryingToLogin) {
                            tryToLogIn();
                        }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.noInternetConnectionError), Toast.LENGTH_SHORT).show();
                }
            }
        });
        emailEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                emailEdit.setError(null);
                return false;
            }
        });
        passwordEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                passwordEdit.setError(null);
                return false;
            }
        });
    }

    private boolean isPasswValid(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(getString(R.string.loginActivityEmptyPasswordError));
            return false;
        }
        if (editText.getText().length() < PASSWORD_MIN_LENGTH) {
            editText.setError(getString(R.string.loginActivityNotValidPasswordError));
            return false;
        }
        return true;
    }

    private boolean isEmailValid(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(getString(R.string.loginActivityEmptyEmailError));
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editText.getText().toString()).matches()) {
            editText.setError(getString(R.string.loginActivityNotValidEmailError));
            return false;
        }
        return true;
    }

    private void tryToLogIn() {
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        isTryingToLogin = true;
        FirebaseAuth
                .getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, ConversationsActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.loginActivityBadCredentialsError), Toast.LENGTH_SHORT).show();
                        }
                        isTryingToLogin = false;
                    }
                });
    }

    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            return connectivityManager.getActiveNetworkInfo();
        } else {
            return null;
        }
    }
}