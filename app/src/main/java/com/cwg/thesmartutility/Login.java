package com.cwg.thesmartutility;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextView forgotPassword, signUpText;
    Button loginButton;
    TextInputEditText emailPhoneInput, passwordInputText;
    TextInputLayout emailPhoneLayout, passwordLayout;
    String EmailPhone, Pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ids
        forgotPassword = findViewById(R.id.forgotText);
        signUpText = findViewById(R.id.loginText);
        loginButton = findViewById(R.id.loginButton);
        emailPhoneInput = findViewById(R.id.loginEmailInput);
        passwordInputText = findViewById(R.id.loginPasswordInput);
        emailPhoneLayout = findViewById(R.id.loginEmailInputLayout);
        passwordLayout = findViewById(R.id.loginPasswordInputLayout);

        forgotPassword.setOnClickListener(v -> {

        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(this, Register.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            init();
        });


    }

    private void init() {
        EmailPhone = Objects.requireNonNull(emailPhoneInput.getText()).toString().trim();
        Pass = Objects.requireNonNull(passwordInputText.getText()).toString().trim();
        if (EmailPhone.isEmpty() || Pass.isEmpty()) {
            checkFields(emailPhoneInput, emailPhoneLayout);
            checkFields(passwordInputText, passwordLayout);
        } else {
            login(EmailPhone, Pass);
        }
    }

    private void login(String emailPhone, String password) {
        String loginUrl = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", emailPhone);
            jsonObject.put("phone", emailPhone);
            jsonObject.put("password", password);
        }catch (JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFields(TextInputEditText editText, TextInputLayout layout) {
        String text = Objects.requireNonNull(editText.getText()).toString().trim();
        if (text.isEmpty()) {
            // Set error message on the TextInputLayout if field is empty
            layout.setErrorEnabled(true);
            layout.setError("This field cannot be empty");
        } else {
            // Clear the error if not empty
            layout.setError(null);
        }
    }
}