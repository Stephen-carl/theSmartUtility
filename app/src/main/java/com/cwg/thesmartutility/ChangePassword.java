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

public class ChangePassword extends AppCompatActivity {

    TextInputEditText newPassEdit, conPassEdit;
    TextInputLayout newPassLayout, conPassLayout;
    Button changeButton;
    String NewPass, ConNewPassword;

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

        // ids
        newPassEdit = findViewById(R.id.changePasswordInput);
        conPassEdit = findViewById(R.id.changeConfirmInput);
        newPassLayout = findViewById(R.id.changePasswordInputLayout);
        conPassLayout = findViewById(R.id.changeConfirmInputLayout);
        changeButton = findViewById(R.id.chnageButton);

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
            conPassLayout.setErrorEnabled(false);
            changePassword(ConNewPassword);
        }
    }

    private void changePassword(String password) {
        String changeUrl = "";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("password", password);
        }catch (JSONException e){
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