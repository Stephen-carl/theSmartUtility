package com.cwg.thesmartutility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.user.ServiceReceipt;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ServicePayment extends AppCompatActivity {

    WebView loadServiceWeb;
    Button serviceProceedButton;
    TextView noLinkText;
    String baseUrl, payLink, refID, paymentAmount, durationText, theEmail, perCharge, HasPayAcct;
    double TokenAmount, CWGChargeAmount;
    int estateID;
    private PreloaderLogo preloaderLogo;
    SharedPreferences validSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.service_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // ids
        loadServiceWeb = findViewById(R.id.loadServiceWeb);
        serviceProceedButton = findViewById(R.id.serviceProceedButton);
        noLinkText = findViewById(R.id.unloadPayStack);
        preloaderLogo = new PreloaderLogo(this);

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // Pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // intents
        // get the link and ref
        Intent intent = getIntent();
        payLink = intent.getStringExtra("link");
        refID = intent.getStringExtra("transRef");
        estateID = intent.getIntExtra("estateID", 0);
        // this would work if it's coming from the estate side
        paymentAmount = intent.getStringExtra("purAmount");
        durationText = intent.getStringExtra("serviceDuration");
        HasPayAcct = intent.getStringExtra("hasPayAcct");

        // start the transaction
        startTransaction();

        // the button
        serviceProceedButton.setOnClickListener(v -> verifyTransaction(refID));

        // on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(ServicePayment.this, R.style.CustomAlertDialogTheme)
                        .setTitle("Warning!!!")
                        .setMessage("Do you want to stop payment?")
                        .setPositiveButton("Yes", (dialog, which) -> verifyTransaction(refID))
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);

    }

    // load Paystack
    private void startTransaction() {
        try {
            if (payLink != null) {
                noLinkText.setVisibility(View.GONE);
                loadServiceWeb.setVisibility(View.VISIBLE);
                //payWebView.setWebViewClient(new WebViewClient());
                loadServiceWeb.getSettings().setJavaScriptEnabled(true);
                loadServiceWeb.getSettings().setDomStorageEnabled(true);
                loadServiceWeb.setInitialScale(1);
                loadServiceWeb.getSettings().setBuiltInZoomControls(true);
                loadServiceWeb.getSettings().setUseWideViewPort(true);
                loadServiceWeb.loadUrl(payLink);
            } else {
                noLinkText.setVisibility(View.VISIBLE);
                loadServiceWeb.setVisibility(View.GONE);
                Toast.makeText(ServicePayment.this, "No payment link", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(ServicePayment.this, "Error: " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // verify payment
    private void verifyTransaction(String refID){
        preloaderLogo.show();
        //String apiURL = "http://192.168.61.64:3030/verifyTransCallback";
        String apiURL = baseUrl+"/user/verservicecharge";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", estateID);
            requestData.put("hasPayAcct", HasPayAcct);
            requestData.put("reference", refID);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                //get the authorization_URL and point to the checkoutPage
                try {
                    //get the status of the result
                    JSONObject theData = response.getJSONObject("theData");
                    boolean status = theData.getBoolean("status");
                    JSONObject jsonObject = theData.getJSONObject("data");
                    String successStatus = jsonObject.getString("status");


                    String success = "success";

                    //check what the status says
                    if (status && successStatus.equals(success)) {
                        //then retrieve all the details from the verification
                        JSONObject dataObjects = theData.getJSONObject("data");
                        //String refer = dataObjects.getString("reference");

                        // get the email
                        JSONObject dataCusAccount = dataObjects.getJSONObject("customer");
                        theEmail = dataCusAccount.getString("email");

                        Toast.makeText(ServicePayment.this, "Verified", Toast.LENGTH_SHORT).show();
                        //String mainCharge = response.getString("theCharge");

                        //save the payment to DB
                        saveToDatabase();

                    } else  {
                        Toast.makeText(ServicePayment.this, "Purchase failed", Toast.LENGTH_LONG).show();
                        // check for role before send the user
                        finish();
                        preloaderLogo.dismiss();

                    }

                } catch (JSONException e) {
                    Toast.makeText(ServicePayment.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(ServicePayment.this, "Please Try Again", Toast.LENGTH_LONG).show();
                finish();
                preloaderLogo.dismiss();
            });
            // Set the RetryPolicy here
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveToDatabase() {
        String token = validSharedPref.getString("token", null);

        String vendURL = baseUrl+"/user/saveservicecharge";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject databaseObjects = new JSONObject();
            databaseObjects.put("reference",refID);
            databaseObjects.put("estateID", estateID);
            databaseObjects.put("amount", paymentAmount);
            databaseObjects.put("duration", durationText);

            JsonObjectRequest saveDatabaseRequest = new JsonObjectRequest(Request.Method.POST, vendURL, databaseObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        JSONObject dataObject = response.getJSONObject("data");
                        String PaymentDate = dataObject.getString("payment_date");
                        String ExpiryDate = dataObject.getString("expiry_date");
                        String payAmount = dataObject.getString("sub_amount");

                        // pass data to next page, to avoid constant going to database
                        Intent nextInt = new Intent(ServicePayment.this, ServiceReceipt.class);
                        //ref, meterNo, token, amount, tariff, unit, date
                        nextInt.putExtra("serviceRefID", refID);
                        //this is the original amount that has not been touched, in case im ask not to deduct from client
                        nextInt.putExtra("payAmount",  payAmount);
                        nextInt.putExtra("payDate", PaymentDate);
                        nextInt.putExtra("expDate", ExpiryDate);
                        nextInt.putExtra("payDuration", durationText);

                        startActivity(nextInt);

                        finish();
                        preloaderLogo.dismiss();
                        Toast.makeText(ServicePayment.this, "Successful!!! ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ServicePayment.this, "Could not save to database", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }

                } catch (JSONException e) {
                    Toast.makeText(ServicePayment.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(ServicePayment.this, "Could not save to database", Toast.LENGTH_LONG).show();
                preloaderLogo.dismiss();
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
            saveDatabaseRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(saveDatabaseRequest);
        } catch (Exception e) {
            Toast.makeText(ServicePayment.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }
}