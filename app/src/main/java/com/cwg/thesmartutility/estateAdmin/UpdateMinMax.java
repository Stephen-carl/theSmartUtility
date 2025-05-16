package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
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
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateMinMax extends AppCompatActivity {
    Button updateButton;
    TextInputEditText minInput, maxInput;
    TextInputLayout minLayout, maxLayout;
    String minAmount, maxAmount, MinAmount, MaxAmount, baseUrl, token;
    SharedPreferences validPref;
    ImageView backButton;
    int estateID;
    PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_update_min_max);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // ids
        updateButton = findViewById(R.id.updatePurchaseButton);
        minInput = findViewById(R.id.minAmountInput);
        maxInput = findViewById(R.id.maxAmountInput);
        minLayout = findViewById(R.id.minAmountLayout);
        maxLayout = findViewById(R.id.maxAmountLayout);
        backButton = findViewById(R.id.updateVendBack);

        // pref
        validPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        estateID = validPref.getInt("estateID", 0);
        token = validPref.getString("token", "");

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // back button
        backButton.setOnClickListener(v -> finish());

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // get the min and max amount
        getMinMax();

        // update button
         updateButton.setOnClickListener(v -> init());

    }

    // get the min and max amount
    private void getMinMax () {
        preloaderLogo.show();
        String getMinMaxURL = baseUrl+"/estate/getMinMax";
        try {
            JsonObjectRequest getMinMaxRequest = new JsonObjectRequest(Request.Method.GET, getMinMaxURL, null, response -> {
                preloaderLogo.dismiss();
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        minAmount = response.getString("min_amount");
                        maxAmount = response.getString("max_amount");
                        minInput.setText(minAmount);
                        maxInput.setText(maxAmount);
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not get data", Toast.LENGTH_SHORT).show();
                finish();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            getMinMaxRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(getMinMaxRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Check Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    // update
    private void init() {
        MinAmount = Objects.requireNonNull(minInput.getText()).toString().trim();
        MaxAmount = Objects.requireNonNull(maxInput.getText()).toString().trim();
        if (MinAmount.isEmpty() || MaxAmount.isEmpty()) {
            checkFields(minInput, minLayout);
            checkFields(maxInput, maxLayout);
        } else {
            updateMinMax(MinAmount, MaxAmount);
        }
    }

    private void checkFields(TextInputEditText input, TextInputLayout layout) {
        if (Objects.requireNonNull(input.getText()).toString().trim().isEmpty()) {
            layout.setErrorEnabled(true);
            layout.setError("Field is required");
        } else {
            layout.setErrorEnabled(false);
        }
    }

    private void updateMinMax(String min_amount, String max_amount) {
        preloaderLogo.show();
        String updateMinMaxURL = baseUrl+"/estate/updateminmax";
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("min_amount", min_amount);
            requestData.put("max_amount", max_amount);
            JsonObjectRequest updateMinMaxRequest = new JsonObjectRequest(Request.Method.PUT, updateMinMaxURL, requestData, response -> {
                preloaderLogo.dismiss();
                try {
                    String message = response.getString("message");
                    boolean data = response.getBoolean("data");
                    if (message.equals("success") && data) {
                        Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Could not update", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Could not update", Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            updateMinMaxRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(updateMinMaxRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Check Internet connection", Toast.LENGTH_SHORT).show();
        }
    }
}