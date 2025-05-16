package com.cwg.thesmartutility.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.util.Objects;

public class ResendVerificationLink extends AppCompatActivity {
    TextView loginText;
    Button resendButton;
    TextInputLayout emailLayout;
    TextInputEditText emailInput;
    String Email, baseUrl;
    PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.resend_verification_link);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ids
        loginText = findViewById(R.id.loginAccount);
        resendButton = findViewById(R.id.resendButton);
        emailLayout = findViewById(R.id.resendEmailLayout);
        emailInput = findViewById(R.id.resendEmailInput);

        // baseUrl
        baseUrl = this.getString(R.string.managementBaseURL);
        preloaderLogo = new PreloaderLogo(this);

        // resend
        resendButton.setOnClickListener(v -> init());
        loginText.setOnClickListener(v -> finish());
    }

    private void init() {
        Email = emailInput.getText().toString().trim();
        if (Email.isEmpty()) {
            checkFields(emailInput, emailLayout);
        } else {
            resendVerification(Email);
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

    private void resendVerification(String email) {
        preloaderLogo.show();
        String resendUrl = baseUrl+"/auth/resendver?email="+email;
        JsonObjectRequest resendRequest = new JsonObjectRequest(Request.Method.GET, resendUrl, null, response -> {
            preloaderLogo.dismiss();
            try {
                String message = response.getString("message");
                if (message.equals("success")) {
                    Toast.makeText(this, "Verification link sent", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "No response.", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            preloaderLogo.dismiss();
            if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                emailLayout.setErrorEnabled(true);
                emailLayout.setError("Email not found.");
            } else if (error.networkResponse != null && error.networkResponse.statusCode == 500) {
                emailLayout.setErrorEnabled(true);
                emailLayout.setError("Verification link was not sent. Try again");
            }
        });
        int socketTimeout = 30000;  // 30 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        resendRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance(this).addToRequestQueue(resendRequest);
    }
}