package com.cwg.thesmartutility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.UnverifiedAdapter;
import com.cwg.thesmartutility.model.UnverifiedModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnverifiedPayment extends AppCompatActivity {
    ImageView backImage;
    RecyclerView unverifyRecycler;
    ArrayList<UnverifiedModel> unverifiedList;
    SharedPreferences validSharedPref;
    private PreloaderLogo preloaderLogo;
    int page = 1;
    int pageSize = 10;
    String baseUrl, token;
    UnverifiedAdapter unverifiedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.unverified_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // ids
        backImage = findViewById(R.id.backImage);
        unverifyRecycler = findViewById(R.id.unverifyRecycler);

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = validSharedPref.getString("token", "");

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // list
        unverifiedList = new ArrayList<>();
        // recyclerview
        unverifyRecycler.setLayoutManager(new LinearLayoutManager(this));
        // adapter
        unverifiedAdapter = new UnverifiedAdapter(this, unverifiedList);
        // add to recycler
        unverifyRecycler.setAdapter(unverifiedAdapter);

        // fetch the pre-payment
        getPrePayment();

        // back button
        backImage.setOnClickListener(v -> finish());
    }

    // fetch the pre-payment
    private void getPrePayment(){
        preloaderLogo.show();
        String prePaymentRequest = baseUrl+"/g/getPrePayment?page="  + page +"&pageSize=" + pageSize;
        JsonObjectRequest prePayRequest = new JsonObjectRequest(Request.Method.GET, prePaymentRequest, null, response -> {
            try {
                String message = response.getString("message");
                preloaderLogo.dismiss();
                if (message.equalsIgnoreCase("success")){
                    // clear list
                    unverifiedList.clear();
                    JSONArray dataArray = response.getJSONArray("data");
                    if (dataArray.length() > 0) {
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            String pay_id = dataObject.getString("pay_id");
                            String amount = dataObject.getString("amount");
                            String trans_ref = dataObject.getString("trans_ref");
                            String payment_type = dataObject.getString("payment_type");
                            String duration = dataObject.getString("duration");
                            String customerID = dataObject.getString("customerID");
                            String brand = dataObject.getString("brand");

                            UnverifiedModel unverifiedModel = new UnverifiedModel(pay_id, amount, trans_ref, payment_type, duration, customerID, brand);
                            unverifiedList.add(unverifiedModel);
                        }
                        unverifiedAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No pending payment verification.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        // Set the RetryPolicy here
        int socketTimeout = 10000;  // 10 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        prePayRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance(this).addToRequestQueue(prePayRequest);
    }
}