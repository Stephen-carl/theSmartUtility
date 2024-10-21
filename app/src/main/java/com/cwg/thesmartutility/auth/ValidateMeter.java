package com.cwg.thesmartutility.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValidateMeter extends AppCompatActivity {

    TextInputEditText meterInput;
    TextInputLayout inputLayout;
    String MeterInput;
    Button validateButton;
    RequestQueue requestQueue;
    SharedPreferences validSharedPref;
    SharedPreferences.Editor prefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.validate_meter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // id
        meterInput = findViewById(R.id.meterInput);
        validateButton = findViewById(R.id.validatedButton);
        inputLayout = findViewById(R.id.inputLayout);

        //requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();
        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        // declare editor to edit
        prefEditor = validSharedPref.edit();

        validateButton.setOnClickListener(v -> {
            // check empty input
            MeterInput = Objects.requireNonNull(meterInput.getText()).toString().trim();
            if (MeterInput.isEmpty()) {
                Toast.makeText(this, "Please input your meter number", Toast.LENGTH_SHORT).show();
                inputLayout.setErrorEnabled(true);
                inputLayout.setError("Please enter your meter number");
            } else{
                inputLayout.setErrorEnabled(false);
                validate(MeterInput);
            }
        });
    }

    private void validate(String meterNumber) {
        String validate_url = "http://192.168.246.60:5050/auth/validate/" + meterNumber;
        try {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, validate_url, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("valid")) {
                        // user's meter number exists
                        JSONObject dataObject = response.getJSONObject("data");
                        int estateID = dataObject.getInt("estateID");
                        String customerID = dataObject.getString("customerID");
                        String tariff = dataObject.getString("tariff");
                        String brand = dataObject.getString("brand");
                        String vendStatus = dataObject.getString("vendStatus");

                        //write to pref
                        prefEditor.putString("meterID", meterNumber);
                        prefEditor.putInt("estateID", estateID);
                        prefEditor.putString("customerID", customerID);
                        prefEditor.putString("tariff", tariff);
                        prefEditor.putString("brand", brand);
                        prefEditor.putString("vendStatus", vendStatus);

                        prefEditor.apply();

                        Intent validateIntent = new Intent(this, Register.class);
                        startActivity(validateIntent);
                        finish();
                    } else {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError("Enter a correct Meter Number");
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Couldn't get message", Toast.LENGTH_SHORT).show();
                }
            }, error -> {

            });
            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}