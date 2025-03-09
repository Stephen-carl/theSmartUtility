package com.cwg.thesmartutility.auth;

import android.content.Intent;
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

public class ChangePassword extends AppCompatActivity {

    TextInputEditText newPassEdit, conPassEdit;
    TextInputLayout newPassLayout, conPassLayout;
    Button changeButton;
    String NewPass, ConNewPassword, Email;
    ImageView backImage;
    String baseUrl;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        newPassEdit = findViewById(R.id.changePasswordInput);
        conPassEdit = findViewById(R.id.changeConfirmInput);
        newPassLayout = findViewById(R.id.changePasswordInputLayout);
        conPassLayout = findViewById(R.id.changeConfirmInputLayout);
        changeButton = findViewById(R.id.chnageButton);
        backImage = findViewById(R.id.changeBackImage);
        preloaderLogo = new PreloaderLogo(this);

        // get the email from the forgot password class
        Intent intent = getIntent();
        Email = intent.getStringExtra("email");

        // back button
        backImage.setOnClickListener(v -> {
            startActivity(new Intent(ChangePassword.this, ForgotPassword.class));
            finish();
        });

        // button
        changeButton.setOnClickListener(v -> {
            init();
        });

    }

    private void init(){
        // to string
        NewPass = Objects.requireNonNull(newPassEdit.getText()).toString().trim();
        ConNewPassword = Objects.requireNonNull(conPassEdit.getText()).toString().trim();
        if (NewPass.isEmpty() || ConNewPassword.isEmpty()) {
            checkFields(newPassEdit, newPassLayout);
            checkFields(conPassEdit, conPassLayout);
        } else if (!ConNewPassword.equals(NewPass)) {
            conPassLayout.setErrorEnabled(true);
            conPassLayout.setError("Re-type your password");
        } else {
            preloaderLogo.show();
            conPassLayout.setErrorEnabled(false);
            changePassword( ConNewPassword);
        }
    }

    private void changePassword(String password) {
        //String changeUrl = "http://41.78.157.215:4173/auth/updatePassword";
        String changeUrl = baseUrl+"/auth/updatePassword";
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("newPassword", password);
                jsonObject.put("email", Email);
            }catch (JSONException e){
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest changePassRequest = new JsonObjectRequest(Request.Method.POST, changeUrl, jsonObject, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        Toast.makeText(this, "Successful, kindly login.", Toast.LENGTH_SHORT).show();
                        // to the login page
                        startActivity(new Intent(ChangePassword.this, Login.class));
                        finish();
                        preloaderLogo.dismiss();
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not change password", Toast.LENGTH_SHORT).show();
            });
            VolleySingleton.getInstance(this).addToRequestQueue(changePassRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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