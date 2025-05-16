package com.cwg.thesmartutility.estateAdmin;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateService extends AppCompatActivity {

    Button updateServiceButton;
    TextInputLayout serviceAmountLayout;
    TextInputEditText serviceAmountInput;
    TextView duration6, duration12, duration1, duration3, apartment_type;
    String serviceAmount, duration, typeID;
    SharedPreferences validSharedPref;
    PreloaderLogo preloaderLogo;
    String baseUrl;
    ImageView updateServiceBack;
    int serviceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // ids
        updateServiceButton = findViewById(R.id.serviceUpdateButton);
        serviceAmountLayout = findViewById(R.id.serviceAmountLayout);
        serviceAmountInput = findViewById(R.id.serviceAmountInput);
        duration6 = findViewById(R.id.duration6);
        duration12 = findViewById(R.id.duration12);
        duration1 = findViewById(R.id.duration1);
        duration3 = findViewById(R.id.duration3);
        apartment_type = findViewById(R.id.apartmentText);

        // declare pref, baseURL and preloader
        validSharedPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        baseUrl = this.getString(R.string.managementBaseURL);
        preloaderLogo = new PreloaderLogo(this);

        // get the serviceID and typeID, if not null call the get current service, else let the user make input and save
        Intent intent = getIntent();
        serviceID = intent.getIntExtra("serviceID", 0);
        typeID = intent.getStringExtra("typeID");
        int typeIDInt = Integer.parseInt(typeID);
        if (serviceID != 0 && typeIDInt != 0){
            getCurrentServiceCharge();
        }

        // onclick listeners, when one of the textviews are clicked, the textview is highlighted with colour #cccde8, and duration string is set to the textview clicked
        duration6.setOnClickListener(v -> {
            duration6.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration1.setBackgroundResource(R.drawable.text_back);
            duration3.setBackgroundResource(R.drawable.text_back);
            duration12.setBackgroundResource(R.drawable.text_back);
            duration = duration6.getText().toString();
        });
        duration12.setOnClickListener(v -> {
            duration1.setBackgroundResource(R.drawable.text_back);
            duration3.setBackgroundResource(R.drawable.text_back);
            duration6.setBackgroundResource(R.drawable.text_back);
            duration12.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration = duration12.getText().toString();
        });
        duration1.setOnClickListener(v -> {
            duration1.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration3.setBackgroundResource(R.drawable.text_back);
            duration6.setBackgroundResource(R.drawable.text_back);
            duration12.setBackgroundResource(R.drawable.text_back);
            duration = "1_months";
        });
        duration3.setOnClickListener(v -> {
            duration3.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration1.setBackgroundResource(R.drawable.text_back);
            duration6.setBackgroundResource(R.drawable.text_back);
            duration12.setBackgroundResource(R.drawable.text_back);
            duration = duration3.getText().toString();
        });

        // get the current service charge
        //getCurrentServiceCharge();

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
        String apiUrl = baseUrl+"/estate/getservices?service_id="+serviceID+"&type_id="+typeID;
        String token =  validSharedPref.getString("token", null);

        try {
            JsonObjectRequest meterRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, response -> {
                try {
                    String message = response.getString("message");
                    String service_fee, service_duration;
                    if (message.equals("success")) {
                        preloaderLogo.dismiss();

                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0){
                            Toast.makeText(this, "No service charge", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            JSONObject dataObject = data.getJSONObject(0);
                            apartment_type.setText(dataObject.getString("type_name"));
                            service_fee = dataObject.getString("service_amount");
                            service_duration = dataObject.getString("duration");
                        }

                        if (service_fee == null){
                            service_fee = "0";
                        } else {
                            serviceAmountInput.setText(service_fee);
                        }
                        // serviceAmountInput.setText(service_fee);
                        serviceAmount = service_fee;
                        duration = service_duration;

                        // highlight the duration that is currently set in response
                        if (service_duration.equals("6_months")) {
                            duration6.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration12.setBackgroundResource(R.drawable.text_back);
                            duration1.setBackgroundResource(R.drawable.text_back);
                            duration3.setBackgroundResource(R.drawable.text_back);
                        } else if (service_duration.equals("12_months")) {
                            duration12.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration6.setBackgroundResource(R.drawable.text_back);
                            duration1.setBackgroundResource(R.drawable.text_back);
                            duration3.setBackgroundResource(R.drawable.text_back);
                        } else if (service_duration.equals("1_months")){
                            duration1.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration3.setBackgroundResource(R.drawable.text_back);
                            duration6.setBackgroundResource(R.drawable.text_back);
                            duration12.setBackgroundResource(R.drawable.text_back);
                        } else if (service_duration.equals("3_months")){
                            duration3.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
                            duration1.setBackgroundResource(R.drawable.text_back);
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
        String apiUrl = baseUrl+"/estate/addservice";
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
            updateDetails.put("amount", serviceAmount);
            updateDetails.put("duration", duration);
            updateDetails.put("type_id", typeID);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
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
                        startActivity(new Intent(this, EstateServiceList.class));
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