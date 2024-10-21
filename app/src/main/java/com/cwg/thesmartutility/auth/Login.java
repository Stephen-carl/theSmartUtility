package com.cwg.thesmartutility.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.user.UserDashboard;
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
    RequestQueue requestQueue;
    SharedPreferences validSharedPref;
    SharedPreferences.Editor prefEditor;

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

        //requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();
        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        // declare editor to edit
        prefEditor = validSharedPref.edit();

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPassword.class);
            startActivity(intent);
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
        String loginUrl = "http://192.168.246.60:5050/auth/login";
        Log.d("the Url" , "came to the url");
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("emailPhone", emailPhone);
                jsonObject.put("password", password);
            }catch (JSONException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            Log.d("Looking" , "came to the request");
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonObject, response -> {
                Log.d("the response" , "came to the response");
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        String token = response.getString("token");
                        JSONObject userObject = response.getJSONObject("user");
                        String userid = userObject.getString("userid");
                        String username = userObject.getString("userName");
                        String email = userObject.getString("email");
                        String phone = userObject.getString("phone");
                        String meterID = userObject.getString("meterID");
                        int estateID = userObject.getInt("estateID");
                        String tariff = userObject.getString("tariff");
                        String vat = userObject.getString("vat");
                        String role = userObject.getString("role");
                        String brand = userObject.getString("brand");
                        String vendStatus = userObject.getString("vendStatus");
                        String customerID = userObject.getString("customerID");
                        String estateName = userObject.getString("estateName");
                        String chintUsername = userObject.getString("chintUsername");
                        String chintPassword = userObject.getString("chintPassword");
                        String gatewayStatus = userObject.getString("gatewayStatus");
                        String eVendStatus = userObject.getString("eVendStatus");

                        //write to pref
                        prefEditor.putString("token", token);
                        prefEditor.putString("userid", userid);
                        prefEditor.putString("username", username);
                        prefEditor.putString("email", email);
                        prefEditor.putString("phone", phone);
                        prefEditor.putString("meterID", meterID);
                        prefEditor.putInt("estateID", estateID);
                        prefEditor.putString("customerID", customerID);
                        prefEditor.putString("tariff", tariff);
                        prefEditor.putString("vat", vat);
                        prefEditor.putString("role", role);
                        prefEditor.putString("brand", brand);
                        prefEditor.putString("vendStatus", vendStatus);
                        prefEditor.putString("estateName", estateName);
                        prefEditor.putString("chintUsername", chintUsername);
                        prefEditor.putString("chintPassword", chintPassword);
                        prefEditor.putString("gatewayStatus", gatewayStatus);
                        prefEditor.putString("eVendStatus", eVendStatus);

                        // to know if the user is signed in already
                        prefEditor.putBoolean("isLoggedIn", true);

                        prefEditor.apply();

                        // check what the role is, either user, admin, super
                        if (role.equals("user")){
                            // go to userDashBoard
                            Intent intent = new Intent(this, UserDashboard.class);
                            startActivity(intent);
                            finish();
                        } else if (role.equals("admin")) {
                            Intent estateIntent = new Intent(this, EstateDashboard.class);
                            startActivity(estateIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "Super Admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        emailPhoneLayout.setErrorEnabled(true);
                        emailPhoneLayout.setError("Check your email address.");
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("Check your password");
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });
            Log.d("the request" , "came to the queue");
            VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        } catch (Exception e) {
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