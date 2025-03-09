package com.cwg.thesmartutility.auth;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaos.view.PinView;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ResetEmailPin extends AppCompatActivity {

    private String Email;
    String Pin, baseUrl;
    PinView pinEditText;
    Button submitButton;
    private PreloaderLogo preloaderLogo;
    RelativeLayout resendTextView;
    ImageView imageView;
    TextView resetText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.reset_email_pin_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        pinEditText = findViewById(R.id.resetPinIn);
        submitButton = findViewById(R.id.resetPinButton);
        resendTextView = findViewById(R.id.resetRelative);
        imageView = findViewById(R.id.resetBackImage);
        resetText = findViewById(R.id.resetText);
        preloaderLogo = new PreloaderLogo(this);

        resetText.setPaintFlags(resetText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Force focus and show keyboard
        pinEditText.setFocusableInTouchMode(true);
        pinEditText.requestFocus();
        submitButton.setEnabled(false);
        submitButton.setBackgroundColor(getResources().getColor(R.color.disabledColor));

        // Show keyboard explicitly
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(pinEditText, InputMethodManager.SHOW_IMPLICIT);


        submitButton.setEnabled(false);

        Intent intent = getIntent();
        Email = intent.getStringExtra("userEmail");
        if (Email == null) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ResetEmailPin.this, ForgotPassword.class));
            finish();
        }

        // set a text watcher and set the button to be enabled
        pinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 0; i < pinEditText.getItemCount(); i++) {
                    if (i < s.length()) {
                        pinEditText.setLineColor(getResources().getColor(R.color.lineFilled)); // Filled fields
                    } else {
                        pinEditText.setLineColor(getResources().getColor(R.color.lineEmpty)); // Empty fields
                    }
                }

                if (s.length() == pinEditText.getItemCount()) {
                    submitButton.setEnabled(true);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.primary)); // Enabled state
                } else {
                    submitButton.setEnabled(false);
                    submitButton.setBackgroundColor(getResources().getColor(R.color.disabledColor)); // Disabled state
                }
//                submitButton.setEnabled(s.length() == 6);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // button
        submitButton.setOnClickListener(v -> checks());
        resendTextView.setOnClickListener(v -> checkEmail(Email));
        imageView.setOnClickListener(v -> finish());
    }

    private void checks() {
        Pin = Objects.requireNonNull(pinEditText.getText()).toString().trim();
        if (Pin.isEmpty()) {
            Toast.makeText(this, "Kindly enter your pin", Toast.LENGTH_LONG).show();
        } else if (Pin.length() < 6) {
            Toast.makeText(this, "Kindly enter your pin", Toast.LENGTH_LONG).show();
        } else {
            verifyPin(Pin);
        }
    }

    private void verifyPin(String pin) {
        preloaderLogo.show();

        //String checkUrl = "http://41.78.157.215:4173/auth/verifyResetPin";
        String checkUrl = baseUrl+"/auth/verifyResetPin";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", Email);
            jsonObject.put("pin", pin);
        } catch (JSONException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, checkUrl, jsonObject, response -> {
            // check if the message is success
            try {
                if (response.getString("message").equals("success")) {
                    Toast.makeText(this, "Pin Verified", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetEmailPin.this, ChangePassword.class);
                    intent.putExtra("email", Email);
                    startActivity(intent);
                    finish();
                } else if (response.getString("message").contains("Invalid")) {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Invalid Pin", Toast.LENGTH_LONG).show();
                } else {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Error: " + response.getString("message"), Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Pin has expired", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ResetEmailPin.this, ForgotPassword.class));
                    finish();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            // check for error status code
            if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Invalid Pin", Toast.LENGTH_LONG).show();
            } else {
                preloaderLogo.dismiss();
                //Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Pin has expired", Toast.LENGTH_LONG).show();
//                startActivity(new Intent(ResetEmailPin.this, ForgotPassword.class));
//                finish();
            }

        });
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private void checkEmail(String email) {
        // Assign a unique tag to the request
        String REQUEST_TAG = "ForgotPasswordRequest";

        // Cancel any existing requests with the same tag
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(REQUEST_TAG);

        preloaderLogo.show();
        //String checkUrl = "http://41.78.157.215:4173/auth/sendResetPin?email=" + email;
        String checkUrl = "http://192.168.61.64:5050/auth/sendResetPin?email=" + email;
        JsonObjectRequest checkRequest = new JsonObjectRequest(Request.Method.GET, checkUrl, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Check email for verification pin", Toast.LENGTH_LONG).show();

                } else {
                    preloaderLogo.dismiss();
                }
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not get the response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Could not get connect to the internet", Toast.LENGTH_SHORT).show();
        });
        // make volley send request only once
        // Set the unique tag to the request
        checkRequest.setTag(REQUEST_TAG);

        // Disable retry mechanism
        checkRequest.setRetryPolicy(new DefaultRetryPolicy(
                0, // Initial timeout duration
                0, // Max number of retries (set to 0 to disable retries)
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        VolleySingleton.getInstance(this).addToRequestQueue(checkRequest);
    }
}