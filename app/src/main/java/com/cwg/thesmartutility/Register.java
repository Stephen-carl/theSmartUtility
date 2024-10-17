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

import com.android.volley.RequestQueue;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Register extends AppCompatActivity {

    TextInputLayout mailInput, honeInput, passInput, firmInput;
    TextInputEditText emailInput, phoneInput, passwordInput, confirmInput;
    String EmailText, PhoneText, PasswordText, ConfirmText;
    Button registerButton;
    TextView loginText;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // id
        passInput = findViewById(R.id.passwordInputLayout);
        mailInput = findViewById(R.id.mailInputLayout);
        honeInput = findViewById(R.id.phoneInputLayout);
        firmInput = findViewById(R.id.confirmInputLayout);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmInput = findViewById(R.id.confirmInput);
        registerButton = findViewById(R.id.createButton);
        loginText = findViewById(R.id.signText);

        requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v -> {
            init();
        });

    }

    private void init(){
        // to string
        EmailText = Objects.requireNonNull(emailInput.getText()).toString();
        PhoneText = Objects.requireNonNull(phoneInput.getText()).toString();
        PasswordText = Objects.requireNonNull(passwordInput.getText()).toString();
        ConfirmText = Objects.requireNonNull(confirmInput.getText()).toString();
        if (EmailText.isEmpty() || PhoneText.isEmpty() || PasswordText.isEmpty() || ConfirmText.isEmpty()) {
            checkFields(emailInput, mailInput);
            checkFields(phoneInput, honeInput);
            checkFields(passwordInput, passInput);
            checkFields(confirmInput, firmInput);
        } else if (!ConfirmText.equals(PasswordText)) {
            firmInput.setErrorEnabled(true);
            firmInput.setError("Re-type your password");
        } else {
            firmInput.setErrorEnabled(false);
            register(EmailText, PhoneText, PasswordText);
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

    private void register(String email, String phone, String password) {
        String registerUrl = "";
        String role = "user";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
            jsonObject.put("phone", phone);
            jsonObject.put("password", password);
            jsonObject.put("role", role);
        }catch (JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}