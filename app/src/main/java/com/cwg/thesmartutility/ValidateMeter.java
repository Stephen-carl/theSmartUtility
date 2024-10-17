package com.cwg.thesmartutility;

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
    String token;

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

        requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();

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
        String validate_url = "";
        try {


            JSONObject jsonObjRequest = new JSONObject();
            try {
                jsonObjRequest.put("meterID", meterNumber);

            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, validate_url, jsonObjRequest, response -> {

            }, error -> {

            })
                // this will be used mainly in the screens that are calling protected api
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            requestQueue.add(jsonRequest);
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}