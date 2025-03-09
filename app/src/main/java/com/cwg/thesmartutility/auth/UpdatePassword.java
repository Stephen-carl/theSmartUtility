package com.cwg.thesmartutility.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdatePassword extends AppCompatActivity {

    TextInputEditText curPassEdit, newPassEdit, conPassEdit;
    TextInputLayout curPassLayout, newPassLayout, conPassLayout;
    Button changeButton;
    String OldPass, NewPass, ConNewPassword, Email, token, baseUrl;
    ImageView backImage;
    SharedPreferences sharedPreferences;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        curPassEdit = findViewById(R.id.oldPasswordInput);
        newPassEdit = findViewById(R.id.updatePasswordInput);
        conPassEdit = findViewById(R.id.updateConfirmInput);
        curPassLayout = findViewById(R.id.oldPasswordInputLayout);
        newPassLayout = findViewById(R.id.updatePasswordInputLayout);
        conPassLayout = findViewById(R.id.updateConfirmInputLayout);
        changeButton = findViewById(R.id.updateButton);
        backImage = findViewById(R.id.updateBackImage);
        preloaderLogo = new PreloaderLogo(this);

        // get the email from the utility pref
        sharedPreferences = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        Email = sharedPreferences.getString("email", "");
        token = sharedPreferences.getString("token", "");

        // back button should go back to the user profile
        backImage.setOnClickListener(v -> onBackPressed());

        // button
        changeButton.setOnClickListener(v -> init());


    }

    private void init(){
        OldPass = Objects.requireNonNull(curPassEdit.getText()).toString().trim();
        NewPass = Objects.requireNonNull(newPassEdit.getText()).toString().trim();
        ConNewPassword = Objects.requireNonNull(conPassEdit.getText()).toString().trim();
        if (OldPass.isEmpty() || NewPass.isEmpty() || ConNewPassword.isEmpty()){
            checkFields(curPassEdit, curPassLayout);
            checkFields(newPassEdit, newPassLayout);
            checkFields(conPassEdit, conPassLayout);
        } else if (!ConNewPassword.equals(NewPass)) {
            conPassLayout.setErrorEnabled(true);
            conPassLayout.setError("Re-type your password");
        } else {
            preloaderLogo.show();
            conPassLayout.setErrorEnabled(false);
            updatePassword(OldPass, ConNewPassword);
        }

    }

    private void updatePassword(String CurrentPassword, String ConfirmedPassword) {
        String updateUrl = baseUrl+"/auth/updateAppPassword";
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("currentPassword", CurrentPassword);
                jsonObject.put("newPassword", ConfirmedPassword);
                jsonObject.put("email", Email);
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.POST, updateUrl, jsonObject, response -> {
                // do something
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        Toast.makeText(this, "Successful, kindly login.", Toast.LENGTH_SHORT).show();
                        // to the login page
                        startActivity(new Intent(UpdatePassword.this, Login.class));
                        finish();
                        preloaderLogo.dismiss();
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Error: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not update password", Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(updateRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this,"Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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