package com.cwg.thesmartutility.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
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
import com.cwg.thesmartutility.ServicePayment;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserService extends AppCompatActivity {
    private PreloaderLogo preloaderLogo;
    TextView serviceAmountText, serviceDurationText;
    Button serviceButton;
    SharedPreferences validSharedPref;
    String baseUrl, ServiceText, DurationText;
    ImageView serviceBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0 , systemBars.top, 0, 0);
            return insets;
        });

        // ids
        serviceAmountText = findViewById(R.id.serviceAmountText);
        serviceDurationText = findViewById(R.id.durationText);
        serviceButton = findViewById(R.id.servicePaymentButton);
        serviceBack = findViewById(R.id.serviceBack);

        preloaderLogo = new PreloaderLogo(this);

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // get the service charge amount
        getCurrentServiceCharge();

        // when the button is pressed
        serviceButton.setOnClickListener(v -> initiate());

        // for the back icon
        serviceBack.setOnClickListener(v -> finish());
    }

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

                        serviceAmountText.setText(service_fee);
                        serviceDurationText.setText(service_duration);
                        ServiceText = service_fee;
                        DurationText = service_duration;
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Could not get service charge", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
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

    // to get the transRef
    private String generateUniqueTransactionReference() {
        String meter = validSharedPref.getString("meterID", null);
        // seconds since 2024-01-01
        long startTimeInSeconds = 1704067200;

        // Get the current time in seconds
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        // Calculate the elapsed time in seconds
        long elapsedTimeSeconds = currentTimeInSeconds - startTimeInSeconds;

        Log.d("Ref", "CWG" + meter + elapsedTimeSeconds);
        return "S_Fee" + meter + elapsedTimeSeconds;

    }

    // initiate the payment
    public void initiate() {
        preloaderLogo.show();
        String theRef = generateUniqueTransactionReference();
        //get amount in int
        double theAmount = Double.parseDouble(ServiceText);
        int Amount = (int) theAmount;
        int meterEstateID = validSharedPref.getInt("estateID", 0);
        String theMeterNum = validSharedPref.getString("meterID", "");
        String theMeterEmail = validSharedPref.getString("email", "");
        String hasPayAcct = validSharedPref.getString("hasPayAcct", "");

        Log.d("Estate ID", String.valueOf(meterEstateID));
        // check the link
        String apiURL = baseUrl+"/user/payservicecharge";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", meterEstateID);
            //send the amount to the backend where the heavy lifting will be done
            requestData.put("amount", Amount);
            requestData.put("reference", theRef);
            requestData.put("email", theMeterEmail);
            requestData.put("hasPayAcct", hasPayAcct);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                //get the authorization_URL and point to the checkoutPage
                try {
                    //i can add a progressBar to load so that the user can know something is happening
                    JSONObject jsonObject = response.getJSONObject("data");
                    String checkoutUrl = jsonObject.getString("authorization_url");
                    Log.d("Link", checkoutUrl);
                    //the referenceID of the transaction which be used to verify
                    String refID = jsonObject.getString("reference");
                    Log.d("Checkout URL", checkoutUrl);

//                    prefEditor.putString("paymentAmount", String.valueOf(Amount));
//                    prefEditor.apply();

                    preloaderLogo.dismiss();

                    Intent intent = new Intent(UserService.this, ServicePayment.class);
                    intent.putExtra("link", checkoutUrl);
                    Log.d("Link", checkoutUrl);
                    intent.putExtra("transRef", refID);
                    intent.putExtra("purAmount", String.valueOf(Amount));
                    intent.putExtra("estateID", meterEstateID);
                    intent.putExtra("meterID", theMeterNum);
                    intent.putExtra("serviceDuration", DurationText);
                    intent.putExtra("hasPayAcct", hasPayAcct);
                    startActivity(intent);

                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(UserService.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    //throw new RuntimeException(e);
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(UserService.this, "Could not connect to network", Toast.LENGTH_LONG).show();
            });
            int socketTimeout = 30000;  // 30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}