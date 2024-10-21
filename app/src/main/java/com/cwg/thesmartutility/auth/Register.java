package com.cwg.thesmartutility.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Register extends AppCompatActivity {

    TextInputLayout mailInput, honeInput, passInput, firmInput, nameInputLayout;
    TextInputEditText emailInput, phoneInput, passwordInput, confirmInput, nameInput;
    String EmailText, PhoneText, PasswordText, ConfirmText, NameText;
    Button registerButton;
    TextView loginText;
    RequestQueue requestQueue;
    SharedPreferences validSharedPref;

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
        nameInput = findViewById(R.id.nameInput);
        nameInputLayout = findViewById(R.id.nameInputLayout);

        //requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

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
        NameText = Objects.requireNonNull(nameInput.getText()).toString();
        if (EmailText.isEmpty() || PhoneText.isEmpty() || PasswordText.isEmpty() || ConfirmText.isEmpty()) {
            checkFields(emailInput, mailInput);
            checkFields(phoneInput, honeInput);
            checkFields(passwordInput, passInput);
            checkFields(confirmInput, firmInput);
            checkFields(nameInput, nameInputLayout);
        } else if (!ConfirmText.equals(PasswordText)) {
            firmInput.setErrorEnabled(true);
            firmInput.setError("Re-type your password");
        } else {
            firmInput.setErrorEnabled(false);
            register(EmailText, PhoneText, PasswordText, NameText);
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

    private void register(String email, String phone, String password, String name) {
        String registerUrl = "http://192.168.246.60:5050/auth/signup";
        String role = "user";
        //get the data from the pref
        String meter = validSharedPref.getString("meterID", null);
        int estateID = validSharedPref.getInt("estateID", 0);
        String tariff = validSharedPref.getString("tariff", null);

        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("email", email);
                jsonObject.put("phone", phone);
                jsonObject.put("password", password);
                jsonObject.put("meterID", meter);
                jsonObject.put("estateID", estateID);
                jsonObject.put("tariff", tariff);
                jsonObject.put("role", role);
            }catch (JSONException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, registerUrl,jsonObject, response -> {
                try {
                    String message = response.getString("message");
                    if(message.equals("success")) {
                        // go to login
                        startActivity(new Intent(this, Login.class));
                        finish();
                    } else {
                        // user already exist
                        mailInput.setErrorEnabled(true);
                        mailInput.setError("User Already Exist, Please Login!");
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });
            VolleySingleton.getInstance(this).addToRequestQueue(registerRequest);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}