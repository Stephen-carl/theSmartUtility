package com.cwg.thesmartutility.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {

    TextInputLayout textInputLayout;
    TextInputEditText emailEditText;
    Button forgotButton;
    ImageView imageView;
    private String Email, baseUrl;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);
        // ids
        textInputLayout = findViewById(R.id.forgotEmailInputLayout);
        emailEditText = findViewById(R.id.forgotEmailInput);
        forgotButton = findViewById(R.id.forgotButton);
        imageView = findViewById(R.id.forgotBackImage);
        preloaderLogo = new PreloaderLogo(this);

        // go to the login
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, Login.class);
            startActivity(intent);
            finish();
        });

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
        // Assign a unique tag to the request
        String REQUEST_TAG = "ForgotPasswordRequest";

        // Cancel any existing requests with the same tag
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll(REQUEST_TAG);

        preloaderLogo.show();
        //String checkUrl = "http://41.78.157.215:4173/auth/sendResetPin?email=" + email;
        String checkUrl = baseUrl+"/auth/sendResetPin?email=" + email;
        JsonObjectRequest checkRequest = new JsonObjectRequest(Request.Method.GET, checkUrl, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Check email for verification pin", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPassword.this, ResetEmailPin.class);
                    intent.putExtra("userEmail", Email);
                    startActivity(intent);
                } else {
                    preloaderLogo.dismiss();
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError("Invalid Email");
                }
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not get the response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            preloaderLogo.dismiss();
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(error.getMessage());
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