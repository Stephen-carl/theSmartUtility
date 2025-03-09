package com.cwg.thesmartutility.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Register extends AppCompatActivity {

    TextInputLayout mailInput, honeInput, passInput, firmInput, nameInputLayout;
    TextInputEditText emailInput, phoneInput, passwordInput, confirmInput, nameInput;
    String EmailText, PhoneText, PasswordText, ConfirmText, NameText, baseUrl;
    Button registerButton;
    TextView loginText;
    RequestQueue requestQueue;
    SharedPreferences validSharedPref;
    SharedPreferences.Editor prefEditor;
    private PreloaderLogo preloaderLogo;

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

        baseUrl = this.getString(R.string.managementBaseURL);

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
        preloaderLogo = new PreloaderLogo(this);

        //requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        prefEditor = validSharedPref.edit();

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

        // Assign a unique tag to the request
        String REQUEST_TAG = "RegisterRequest";

        // Cancel any existing requests with the same tag
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(REQUEST_TAG);

        preloaderLogo.show();
        registerButton.setEnabled(false);
        String registerUrl = baseUrl+"/auth/signup";
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
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, registerUrl,jsonObject, response -> {
                try {
                    String message = response.getString("message");
                    if(message.equals("success")) {
                        preloaderLogo.dismiss();
                        registerButton.setEnabled(true);
                        // go to login
                        prefEditor.putBoolean("isRegistered", true);
                        prefEditor.apply();
                        showAlertDialog(true);
//                        startActivity(new Intent(this, Login.class));
//                        finish();
                    }
//                    } else{
//                        progressBar.setVisibility(View.GONE);
//                        registerButton.setEnabled(true);
//                        // user already exist
//                        mailInput.setErrorEnabled(true);
//                        mailInput.setError("User Already Exist, Please Login!");
//                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    registerButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
//                if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
//                    progressBar.setVisibility(View.GONE);
//                    registerButton.setEnabled(true);
//                    mailInput.setError("User with this email already exist.");
//                    Toast.makeText(this,"Error: " + error, Toast.LENGTH_SHORT).show();
//                } else{
//                    progressBar.setVisibility(View.GONE);
//                    registerButton.setEnabled(true);
//                    mailInput.setErrorEnabled(true);
//                    mailInput.setError("User with this email already exist.");
//                    Toast.makeText(this,"Error: " + error, Toast.LENGTH_SHORT).show();
//                }
                preloaderLogo.dismiss();
                registerButton.setEnabled(true);
                Toast.makeText(this,"Error: User already exist", Toast.LENGTH_SHORT).show();
            });
            // Set the unique tag to the request
            registerRequest.setTag(REQUEST_TAG);

            // Disable retry mechanism
            registerRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, // Initial timeout duration
                    0, // Max number of retries (set to 0 to disable retries)
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            VolleySingleton.getInstance(this).addToRequestQueue(registerRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            registerButton.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showAlertDialog(boolean isSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.success_failed_alert, null);
        builder.setView(dialogView);

        // Get the Ids of the components
        ImageView statusIcon = dialogView.findViewById(R.id.dialog_status_icon);
        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button actionButton = dialogView.findViewById(R.id.dialog_action_button);

        // set the dialog properties based on success or failed
        if (isSuccess) {
            statusIcon.setImageResource(R.drawable.success_circle);
            title.setText("Registration Successfully");
            message.setText("Kindly check your email and click the verification link");
            actionButton.setText("Ok");
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.primary));
        } else {
            statusIcon.setImageResource(R.drawable.failed_icon);
            title.setText("Registration Failed");
            message.setText("Kindly enter the correct details");
            actionButton.setText("Ok");
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.red));
        }

        // show the dialog
        AlertDialog dialog = builder.create();
        actionButton.setOnClickListener(v -> {
            if (isSuccess) {
                dialog.dismiss();
                // go to the estateDashboard
                startActivity(new Intent(Register.this, Login.class));
                finish();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}