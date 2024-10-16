package com.cwg.thesmartutility;

import android.os.Bundle;
import android.widget.Button;
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

public class ForgotPassword extends AppCompatActivity {

    TextInputLayout textInputLayout;
    TextInputEditText emailEditText;
    Button forgotButton;
    private String Email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ids
        textInputLayout = findViewById(R.id.forgotEmailInputLayout);
        emailEditText = findViewById(R.id.forgotEmailInput);
        forgotButton = findViewById(R.id.forgotButton);

        forgotButton.setOnClickListener(v -> {
            Email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
            if (Email.isEmpty()) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("Cannot be empty");
            } else {
                textInputLayout.setErrorEnabled(false);
                checkEmail(Email);
            }
        });
    }

    private void checkEmail(String email) {
        String checkUrl = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        }catch (JSONException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}