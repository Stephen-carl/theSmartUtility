package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EstateServiceFeeDetail extends AppCompatActivity {

    TextView serviceTransID, serviceMeterNum, serviceAmount, servicePayDate, serviceExpiryDate, serviceHeadAmount;
    String serviceRefID, baseUrl, token;
    ImageView backIcon;
    SharedPreferences validPref;
    PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_service_fee_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ids
        serviceTransID = findViewById(R.id.serviceTransID);
        serviceMeterNum = findViewById(R.id.serviceMeterNum);
        serviceAmount = findViewById(R.id.serviceAmount);
        servicePayDate = findViewById(R.id.servicePayDate);
        serviceExpiryDate = findViewById(R.id.serviceExpiryDate);
        serviceHeadAmount = findViewById(R.id.serviceAmountText);
        backIcon = findViewById(R.id.backIcon);

        // back button
        backIcon.setOnClickListener(v -> finish());

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // get the service ref id from the intent
        serviceRefID = getIntent().getStringExtra("serviceRefID");

        // base url
        baseUrl = getString(R.string.managementBaseURL);

        // get token from shared preferences
        validPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = validPref.getString("token", "");

        // fetch the service details
        fetchServiceDetails();
    }

    // fetch the service details from the database using the service ref id
    private void fetchServiceDetails() {
        preloaderLogo.show();
        String fetchURl = baseUrl+"/user/oneservicetrans/?refID="+serviceRefID;
        try {
            JsonObjectRequest fetchService = new JsonObjectRequest(Request.Method.GET, fetchURl, null, response -> {
                try {
                    preloaderLogo.dismiss();
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        JSONObject data = response.getJSONObject("data");
                        serviceTransID.setText(data.getString("subscription_id"));
                        serviceMeterNum.setText(data.getString("meterID"));
                        serviceAmount.setText("NGN "+data.getString("sub_amount"));
                        serviceHeadAmount.setText("NGN "+data.getString("sub_amount"));
                        servicePayDate.setText(data.getString("payment_date"));
                        serviceExpiryDate.setText(data.getString("expiry_date"));
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            fetchService.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(fetchService);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}