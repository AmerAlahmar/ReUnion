package com.safaorhan.reunion.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.safaorhan.reunion.R;
import com.safaorhan.reunion.model.NewUser;

public class SignUpActivity extends AppCompatActivity {
    private static final int PASSWORD_MIN_LENGTH = 6;
    EditText emailEdit;
    EditText firstNameEdit;
    EditText lastNameEdit;
    EditText passwordEdit;
    Button signUpButton;
    boolean isTryingToSignUp = false;
    private static final String TAG = SignUpActivity.class.getSimpleName();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        emailEdit = findViewById(R.id.emailEdit);
        firstNameEdit = findViewById(R.id.firstNameEdit);
        lastNameEdit = findViewById(R.id.lastNameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkInfo networkInfo = getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    boolean emailValidation = isEmailValid(emailEdit);
                    boolean passwValidation = isPasswValid(passwordEdit);
                    boolean fNameValidation = isNamesValid(firstNameEdit);
                    boolean lNameValidation = isNamesValid(lastNameEdit);
                    if (emailValidation && passwValidation && fNameValidation && lNameValidation)
                        if (!isTryingToSignUp) {
                            tryToSignUp();
                        }
                } else {
                    Toast.makeText(SignUpActivity.this, getString(R.string.noInternetConnectionError), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(SignUpActivity.this, ConversationsActivity.class);
            startActivity(intent);
            finish();
        }
        super.onStart();
    }

    private boolean isNamesValid(EditText editText) {
        if (editText.getText().toString().isEmpty()) {
            editText.setError(getString(R.string.signUpActivityEmptyNameError));
            return false;
        }
        if (editText.getText().toString().length() < 3) {
            editText.setError(getString(R.string.signUpActivityTooShortNameError));
            return false;
        }

        if (editText.getText().toString().contains(" ")) {
            editText.setError(getString(R.string.signUpActivityHaveSpaceNameError));
            return false;
        }
        return true;
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

    private void tryToSignUp() {
        final String email = emailEdit.getText().toString();
        final String password = passwordEdit.getText().toString();
        final String fName = firstNameEdit.getText().toString();
        final String lName = lastNameEdit.getText().toString();
        isTryingToSignUp = true;
        FirebaseAuth
                .getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            NewUser user = new NewUser();
                            user.setEmail(email);
                            user.setName(fName);
                            user.setSurname(lName);
                            FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignUpActivity.this, ConversationsActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, getString(R.string.ErrorWhileTryingToSignUp), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(SignUpActivity.this, getString(R.string.ErrorWhileTryingToSignUp), Toast.LENGTH_SHORT).show();
                        }
                        isTryingToSignUp = false;
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