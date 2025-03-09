package com.cwg.thesmartutility.estateAdmin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class UpdateService extends AppCompatActivity {

    Button updateServiceButton;
    TextInputLayout serviceAmountLayout;
    TextInputEditText serviceAmountInput;
    TextView duration6, duration12;
    String serviceAmount, duration;
    SharedPreferences validSharedPref;
    PreloaderLogo preloaderLogo;
    String baseUrl;
    ImageView updateServiceBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // ids
        updateServiceButton = findViewById(R.id.serviceUpdateButton);
        serviceAmountLayout = findViewById(R.id.serviceAmountLayout);
        serviceAmountInput = findViewById(R.id.serviceAmountInput);
        duration6 = findViewById(R.id.duration6);
        duration12 = findViewById(R.id.duration12);

        // declare pref, baseURL and preloader
        validSharedPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        baseUrl = this.getString(R.string.managementBaseURL);
        preloaderLogo = new PreloaderLogo(this);

        // onclick listeners, when one of the textviews are clicked, the textview is highlighted with colour #cccde8, and duration string is set to the textview clicked
        duration6.setOnClickListener(v -> {
            duration6.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration12.setBackgroundResource(R.drawable.text_back);
            duration = duration6.getText().toString();
        });
        duration12.setOnClickListener(v -> {
            duration6.setBackgroundResource(R.drawable.text_back);
            duration12.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration = duration12.getText().toString();
        });

        // get the current service charge
        getCurrentServiceCharge();

        // update the service charge
        updateServiceButton.setOnClickListener(v -> updateServiceCharge());

        // back button
        updateServiceBack = findViewById(R.id.updateServiceBack);
        updateServiceBack.setOnClickListener(v -> finish());
    }

    // to get the current service charge
    private void getCurrentServiceCharge(){
        // call api to get the current charge and set the text
        preloaderLogo.show();
        String apiUrl = baseUrl+"/user/getservicecharge";
        String token =  validSharedPref.getString("token", null);

        try {
            JsonObjectRequest meterRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        preloaderLogo.dismiss();

                        JSONObject data = response.getJSONObject("data");
                        String service_fee = data.getString("service_fee");
                        String service_duration = data.getString("service_duration");

                        serviceAmountInput.setText(service_fee);
                        serviceAmount = service_fee;
                        duration = service_duration;

                        // highlight the duration that is currently set in response
                        if (service_duration.equals("6_months")) {
                            duration6.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration12.setBackgroundResource(R.drawable.text_back);
                        } else if (service_duration.equals("12_months")) {
                            duration12.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration6.setBackgroundResource(R.drawable.text_back);
                        } else {
                            duration6.setBackgroundResource(R.drawable.text_back);
                            duration12.setBackgroundResource(R.drawable.text_back);
                        }
                        // durationText.setText(service_duration);
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Could not get service charge", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            meterRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(meterRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateServiceCharge(){
        preloaderLogo.show();
        String apiUrl = baseUrl+"/estate/updateservicecharge";
        String token =  validSharedPref.getString("token", null);
        int estateID = validSharedPref.getInt("estateID", 0);
        serviceAmount = Objects.requireNonNull(serviceAmountInput.getText()).toString();
        // check if service amount is empty
        if (serviceAmount.isEmpty()) {
            serviceAmountLayout.setError("Service amount is required");
            return;
        }

        JSONObject updateDetails = new JSONObject();
        try {
            updateDetails.put("estate", estateID);
            updateDetails.put("serviceCharge", serviceAmount);
            updateDetails.put("duration", duration);
        } catch (JSONException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        try {
            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.POST, apiUrl, updateDetails, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Service charge updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Could not update service charge", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            updateRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(updateRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}