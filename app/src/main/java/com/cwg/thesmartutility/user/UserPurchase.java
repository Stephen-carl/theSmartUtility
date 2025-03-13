package com.cwg.thesmartutility.user;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.PaystackPayment;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserPurchase extends AppCompatActivity {

    TextInputEditText purchaseAmount;
    TextInputLayout purchaseAmountLayout;
    String PurchaseAmount, Token, vendStatus, HasPayAcct, baseUrl, payBaseUrl;
    TextView userPur1000, userPur2000, userPur5000, userPur10000, userPur20000, userPur30000, userPur50000, userPur100000;
    Button purchaseAmountButton;
    private final int minMoneyLength = 3;
    private final int maxAmount = 100000;
    SharedPreferences validSharedPref;
    ImageView purchaseBack;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_purchase);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);
        payBaseUrl = this.getString(R.string.payBaskURL);
        // ids
        purchaseAmount = findViewById(R.id.purchaseAmount);
        userPur1000 = findViewById(R.id.userPur1000);
        userPur2000 = findViewById(R.id.userPur2000);
        userPur5000 = findViewById(R.id.userPur5000);
        userPur10000 = findViewById(R.id.userPur10000);
        userPur20000 = findViewById(R.id.userPur20000);
        userPur30000 = findViewById(R.id.userPur30000);
        userPur50000 = findViewById(R.id.userPur50000);
        userPur100000 = findViewById(R.id.userPur100000);
        purchaseAmountButton = findViewById(R.id.purchaseAmountButton);
        purchaseAmountLayout = findViewById(R.id.purchaseAmountLayout);
        purchaseBack = findViewById(R.id.purchaseBack);
        preloaderLogo = new PreloaderLogo(this);

        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        // Log the brand
        String brand = validSharedPref.getString("brand", null);
        String customerID = validSharedPref.getString("customerID", "");
        assert brand != null;
        Log.d("Brand: ", brand);
        Log.d("The CustomerID: ", customerID);
        Token = validSharedPref.getString("token", null);
        vendStatus =  validSharedPref.getString("vendStatus", null);
        HasPayAcct = validSharedPref.getString("hasPayAcct", null);
        //assert vendStatus != null;

        // when any of the textViews is clicked, clear and set the edittext to the number value
        userPur1000.setOnClickListener(v -> purchaseAmount.setText("1000"));
        userPur2000.setOnClickListener(v -> purchaseAmount.setText("2000"));
        userPur5000.setOnClickListener(v -> purchaseAmount.setText("5000"));
        userPur10000.setOnClickListener(v -> purchaseAmount.setText("10000"));
        userPur20000.setOnClickListener(v -> purchaseAmount.setText("20000"));
        userPur30000.setOnClickListener(v -> purchaseAmount.setText("30000"));
        userPur50000.setOnClickListener(v -> purchaseAmount.setText("50000"));
        userPur100000.setOnClickListener(v -> purchaseAmount.setText("100000"));

        // once the button is clicked, init function
        purchaseAmountButton.setOnClickListener(v -> init());

        purchaseBack.setOnClickListener(v -> startActivity(new Intent(UserPurchase.this, UserDashboard.class)));

    }

    public void init() {
        PurchaseAmount = Objects.requireNonNull(purchaseAmount.getText()).toString();
        int thePrice = Integer.parseInt(PurchaseAmount);
        // check if the amount is empty
        if (PurchaseAmount.isEmpty()) {
            // set the error
            purchaseAmountLayout.setErrorEnabled(true);
            purchaseAmountLayout.setError("Amount is required");
        } else if (PurchaseAmount.length() < minMoneyLength) {
            // set error
            purchaseAmountLayout.setErrorEnabled(true);
            purchaseAmountLayout.setError("Amount must be more than ₦ 1000");
        } else if (thePrice > maxAmount) {
            // set error
            purchaseAmountLayout.setErrorEnabled(true);
            purchaseAmountLayout.setError("Max Amount is ₦ 100,000");
        } else {
            preloaderLogo.show();
            runChecks();
        }

    }

    private void runChecks() {
        String meRef = generateUniqueTransactionReference();
        // gatewayStatus is either true or false, vendStatus is either enabled or disabled
        if (vendStatus.equals("Enabled")) {
            // initiate link
            initiate(meRef);
        } else {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Unable to purchase token, kindly contact Admin.", Toast.LENGTH_LONG).show();
        }
    }

    public void initiate(String meRef) {
        String theMeterNum = validSharedPref.getString("meterID", null);
        String theMeterEmail = validSharedPref.getString("email", null);
        int meterEstateID = validSharedPref.getInt("estateID", 0);
        String token = validSharedPref.getString("token", "");
        //get amount in int
        int Amount = Integer.parseInt(PurchaseAmount);
        //String apiURL = "http://192.168.61.64:3030/generateCheckoutSession";
        String apiURL = payBaseUrl+"/g/genPaystack";

        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", meterEstateID);
            //send the amount to the backend where the heavy lifting will be done
            requestData.put("amount", Amount);
            requestData.put("reference", meRef);
            requestData.put("email", theMeterEmail);
            requestData.put("meter", theMeterNum);
            requestData.put("hasPayAcct", HasPayAcct);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                //get the authorization_URL and point to the checkoutPage
                try {
                    //i can add a progressBar to load so that the user can know something is happening
                    JSONObject jsonObject = response.getJSONObject("data");
                    String checkoutUrl = jsonObject.getString("authorization_url");
                    //the referenceID of the transaction which be used to verify
                    String refID = jsonObject.getString("reference");

                    if (checkoutUrl.isEmpty()) {
                        preloaderLogo.dismiss();
                        Toast.makeText(UserPurchase.this, "No payment link", Toast.LENGTH_SHORT).show();
                    } else {
                        preloaderLogo.dismiss();
                        Intent intent = new Intent(UserPurchase.this, PaystackPayment.class);
                        intent.putExtra("link", checkoutUrl);
                        intent.putExtra("transRef", refID);
                        intent.putExtra("estateID", meterEstateID);
                        intent.putExtra("purAmount", PurchaseAmount);

                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(UserPurchase.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    //throw new RuntimeException(e);
                }
            }, error -> {
                preloaderLogo.dismiss();
                if (error.networkResponse != null && error.networkResponse.statusCode == 401 || Objects.requireNonNull(error.networkResponse).statusCode == 500) {
                    String errorMessage = "Unknown error";
                    try {
                        // Convert the error response to a string
                        String json = new String(error.networkResponse.data, "UTF-8");
                        JSONObject errorObj = new JSONObject(json);
                        errorMessage = errorObj.getString("message");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Display an AlertDialog with the error message
                    new AlertDialog.Builder(UserPurchase.this)
                            .setTitle("Error")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                } else {
                    // Handle other error cases if needed.
                }

                //Toast.makeText(UserPurchase.this, "Please Try Again", Toast.LENGTH_LONG).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 10000;  // 30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String generateUniqueTransactionReference() {
        String meter = validSharedPref.getString("meterID", null);
        // seconds since 2024-01-01
        long startTimeInSeconds = 1704067200;

        // Get the current time in seconds
        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        // Calculate the elapsed time in seconds
        long elapsedTimeSeconds = currentTimeInSeconds - startTimeInSeconds;

        Log.d("Ref", "CWG" + meter + elapsedTimeSeconds);
        return "CWG" + meter + elapsedTimeSeconds;
    }
}