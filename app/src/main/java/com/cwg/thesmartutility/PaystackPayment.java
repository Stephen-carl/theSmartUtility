package com.cwg.thesmartutility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.estateAdmin.MeterDetails;
import com.cwg.thesmartutility.user.UserDashboard;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PaystackPayment extends AppCompatActivity {
    WebView payWebView;
    MaterialButton proceedButton;
    TextView noLinkText;
    String payLink, refID, role, meterID, paymentAmount, brand, token, vat, perCharge, theCustomerID, thePassword, theUsername, meterTariff, theEmail;
    int estateID;
    private ProgressBar progressBar;
    SharedPreferences validSharedPref;
    DecimalFormat decimalFormat = new DecimalFormat("#.0000");
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.paystack_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0,0);
            return insets;
        });

        // ids
        payWebView = findViewById(R.id.loadPayStackWeb);
        noLinkText = findViewById(R.id.unloadPayStack);
        proceedButton = findViewById(R.id.paymentProceedButton);
        progressBar = findViewById(R.id.progressBar);

        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        role = validSharedPref.getString("role", null);
        meterID = validSharedPref.getString("meterID", null);
        brand = validSharedPref.getString("brand", null);
        token = validSharedPref.getString("token", null);
        vat = validSharedPref.getString("vat", null);
        theCustomerID = validSharedPref.getString("customerID", null);
        theUsername = validSharedPref.getString("chintUsername", null);
        thePassword = validSharedPref.getString("chintPassword", null);
        meterTariff = validSharedPref.getString("tariff", null);

        // get the link and ref
        Intent intent = new Intent();
        payLink = intent.getStringExtra("link");
        refID = intent.getStringExtra("transRef");
        estateID = intent.getIntExtra("estateID", 0);
        paymentAmount = intent.getStringExtra("purAmount");

        // load the link in the webView
        startTransaction();

        // verify the transaction after the button is clicked
        proceedButton.setOnClickListener(v -> verifyTransaction(refID));

        // on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new AlertDialog.Builder(PaystackPayment.this, R.style.CustomAlertDialogTheme)
                        .setTitle("Warning!!!")
                        .setMessage("Do you want to stop payment?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            verifyTransaction(refID);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
        //

    }

    private void startTransaction() {
        try {
            if (payLink != null) {
                noLinkText.setVisibility(View.GONE);
                payWebView.setWebViewClient(new WebViewClient());
                payWebView.getSettings().setJavaScriptEnabled(true);
                payWebView.getSettings().setDomStorageEnabled(true);
                payWebView.setInitialScale(1);
                payWebView.getSettings().setBuiltInZoomControls(true);
                payWebView.getSettings().setUseWideViewPort(true);
                payWebView.loadUrl(payLink);
            } else {
                noLinkText.setVisibility(View.VISIBLE);
                payWebView.setVisibility(View.GONE);
                Toast.makeText(PaystackPayment.this, "No payment link", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // verify transaction
    private void verifyTransaction(String refID){
        progressBar.setVisibility(View.VISIBLE);
        String apiURL = "http://192.168.246.74:3030/generateCheckoutSession";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", estateID);
            requestData.put("reference", refID);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                //get the authorization_URL and point to the checkoutPage
                try {
                    //get the status of the result
                    boolean status = response.getBoolean("status");
                    JSONObject jsonObject = response.getJSONObject("data");
                    String successStatus = jsonObject.getString("status");
                    String success = "success";

                    //check what the status says
                    if (status && successStatus.equals(success)) {
                        //then retrieve all the details from the verification
                        JSONObject dataObjects = response.getJSONObject("data");
                        String refer = dataObjects.getString("reference");
                        String paidAt = dataObjects.getString("paid_at");

                        // get the percentage charge
                        JSONObject dataSubAccount = dataObjects.getJSONObject("subaccount");
                        //this is the cut that goes to the estate account that will be used to get the token
                        perCharge = dataSubAccount.getString("percentage_charge");

                        // get the email
                        JSONObject dataCusAccount = dataObjects.getJSONObject("customer");
                        theEmail = dataCusAccount.getString("email");

                        Toast.makeText(PaystackPayment.this, "Verified", Toast.LENGTH_SHORT).show();
                        //String mainCharge = response.getString("theCharge");

                        //save the payment to DB
                        savePayment(refer);

                    } else  {
                        Toast.makeText(PaystackPayment.this, "Purchase failed", Toast.LENGTH_LONG).show();
                        // check for role before send the user
                        if (Objects.equals(role, "user")){
                            Intent in = new Intent(PaystackPayment.this, UserDashboard.class);
                            startActivity(in);
                            finish();
                            progressBar.setVisibility(View.GONE);
                        } else if (Objects.equals(role, "admin")) {
                            Intent in = new Intent(PaystackPayment.this, MeterDetails.class);
                            startActivity(in);
                            finish();
                            progressBar.setVisibility(View.GONE);
                        }

                    }

                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(PaystackPayment.this, "Please Try Again", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            });

            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // save payment
    private void savePayment(String refer) {
        String saveURL = "http://192.168.246.60:3030/savePayment";
        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("estateID", estateID);
                jsonRequest.put("meterID", meterID);
                jsonRequest.put("refID", refer);
                jsonRequest.put("amount", paymentAmount);
                jsonRequest.put("role", role);
            } catch (JSONException e) {
                Log.e("Error with json object data", Objects.requireNonNull(e.getMessage()));
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    saveURL,
                    jsonRequest,
                    response -> {
                        try {
                            //get the message
                            String message = response.getString("message");
                            if (message.equals("success")) {
                                // run checks on the meter brand type
                                checkMeterBrand();
                            }else {
                                // there is a reason to this
                                verifyTransaction(refID);
                            }

                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PaystackPayment.this, "Could not read response. Contact Admin", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("Payment", "Error: " + error.toString());
                        progressBar.setVisibility(View.GONE);
                    }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        }catch ( Exception e) {
            Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    // run checks on the meter brand
    private void checkMeterBrand(){
        try {
            if (Objects.equals(brand, "Chint")) {
                generateChintToken();
            } else if (Objects.equals(brand, "Hexing")) {
                generateHexingToken();
            } else if (Objects.equals(brand, "Kelin")) {
                genKelinLoginToken();
            } else {
                Toast.makeText(PaystackPayment.this, "Your meter brand does not exist", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private double removeTheCharge(){
        //get the double charge and multiply by the amount
        double tCharge = 100 - Integer.parseInt(perCharge);     //eg, 100 - 98.5
        double paidAmount = Double.parseDouble(paymentAmount);
        return paidAmount - (paidAmount * (tCharge / 100));
    }

    // kelin
    private void genKelinLoginToken() {
        //progressBar.setVisibility(View.VISIBLE);
        //call the login API
        String apiURL = "http://192.168.246.60:3030/loginKelinToken";
        try {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, apiURL, null, response -> {
                try {
                    //get the login token
                    String loginToken = response.getString("token");
                    kelly(loginToken);
                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(PaystackPayment.this, "Couldn't connect to internet", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private  void  kelly(String logToken) {
        String meterTariff = validSharedPref.getString("tariff", null);
        //progressBar.setVisibility(View.VISIBLE);
        LocalDateTime localDateTime = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDateTime = LocalDateTime.now();
        }
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
        assert localDateTime != null;
        String theDate = localDateTime.toString();
        Log.d("Current Time", theDate);
        String vendURL = "http://192.168.246.60:3030/vendKelinToken";
        JSONObject vendingObjects = new JSONObject();
        try {
            vendingObjects.put("token", logToken);
            vendingObjects.put("meterNo", meterID);
            vendingObjects.put("tariff", meterTariff);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", removeTheCharge());   // amount with removed charge
            vendingObjects.put("date", theDate);
            vendingObjects.put("vat", Double.parseDouble(vat));     // in percentage
        } catch (JSONException e) {
            Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        new PaystackPayment.kellyTransactionDataTask(vendURL, vendingObjects).execute();
    }
    private class kellyTransactionDataTask extends AsyncTask<Void, Void, String> {
        private final String apiUrl;
        private final JSONObject jsonData;

        private kellyTransactionDataTask(String apiUrl, JSONObject jsonData) {
            this.apiUrl = apiUrl;
            this.jsonData = jsonData;
        }
        @Override
        //retrieves the json response from the API
        protected String doInBackground(Void... voids) {
            try {
                //create the connection
                URL url= new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Write data to the connection
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonData.toString().getBytes());
                outputStream.flush();
                outputStream.close();
                //get the responseCode
                int responseCode = connection.getResponseCode();
                //then check the response
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read the response from the server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    // Return the entire JSON response
                    return response.toString();
                }

            } catch (Exception e){
                Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            // Return null if there's an issue with the network or API call
            return null;
        }
        @Override
        //processes the json response
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {

                // Parse the entire JSON response into a JSONObject
                try {
                    JSONObject response = new JSONObject(jsonResponse);
                    try {
                        String kelinToken = response.getString("payToken");
                        String kelinUnits = response.getString("payKWH");
                        saveToDatabase(kelinToken, kelinUnits);

                    } catch (JSONException e) {
                        Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        //throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, "Could not get the data", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    // chint
    private void generateChintToken() {
        //chint tariff is preset in the system and the amount should not be minus
        String ChintAmount = decimalFormat.format(removeTheCharge());
        String customerID = theCustomerID;
        String pass = thePassword;
        String user = theUsername;
        double theVat= Double.parseDouble(vat);
        String vendURL = "http://192.168.246.60:3030/chintVend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("customerId", customerID);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", ChintAmount);
            vendingObjects.put("password", pass);
            vendingObjects.put("username", user);
            vendingObjects.put("vat", theVat);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    JSONObject envelope = response.getJSONObject("soap:Envelope");
                    JSONObject body= envelope.getJSONObject("soap:Body");
                    JSONObject transRes = body.getJSONObject("TransactionResponse");
                    JSONObject transResult = transRes.getJSONObject("TransactionResult");
                    //to get the result
                    JSONObject theResult = transResult.getJSONObject("Result");
                    String ChintResult = theResult.getString("_text");

                    //just in case the system starts misbehaving
                    if (ChintResult.equals("false")) {
                        retryCount++;
                        Log.d("Recursive count", String.valueOf(retryCount));
                        generateChintToken();
//                        Toast.makeText(SuccessPage.this, "Please Click the button again", Toast.LENGTH_LONG).show();
//                        progressBar.setVisibility(View.GONE);
                    }else {
                        //to get the token
                        JSONObject theRechargeToken = transResult.getJSONObject("RechargeToken");
                        String ChintToken = theRechargeToken.getString("_text");
                        //to get the energy unit
                        JSONObject theEnergy = transResult.getJSONObject("Energy");
                        String ChintUnits = theEnergy.getString("_text");
                        //for the change. Confirm from yogesh
                        JSONObject theChangeAmount = transResult.getJSONObject("ChangeAmount");
                        String ChangeAmount = theEnergy.getString("_text");

                        saveToDatabase(ChintToken, ChintUnits);
                        //after saving, print the pdf
                    }
                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(PaystackPayment.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    // hexing
    private void generateHexingToken() {
        //String HexingAmount = decimalFormat.format(PaidAmount);
        //electricity
        int service = 0;
        double amount = removeTheCharge();
        double theVat = Double.parseDouble(vat);
        //the real figure is meant to be the one that i get after i divide the amount by the tariff and send to the system
        double tar = Double.parseDouble(meterTariff);
        String vendURL = "http://192.168.246.60:3030/hexingVend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("meterID", meterID);
            vendingObjects.put("transactionId", refID);
            vendingObjects.put("service", service);
            // i have to do the calculation on the backend
            vendingObjects.put("amount", amount);
            vendingObjects.put("tariff", tar);
            vendingObjects.put("vat", theVat);
            //i am meant to pass in the real amount to vend the token
            //vendingObjects.put("value", valueToSend);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    String HexingStatus = response.getString("statusCode");
                    if (HexingStatus.equals("0")) {
                        JSONObject vendData = response.getJSONObject("vendingData");
                        String tokenData = vendData.getString("tokenDec");
                        String tokenUnit = vendData.getString("unitsActual");

                        saveToDatabase(tokenData, tokenUnit);
                    }else {
                        Toast.makeText(PaystackPayment.this, "Could not generate Token. Contact Admin", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(PaystackPayment.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }

    private void saveToDatabase(String Token, String unit) {
        double Amount = removeTheCharge() - (removeTheCharge() * Double.parseDouble(vat)/100);
        //double RealAmount = Double.parseDouble(realAmount);
        double realTariff = Double.parseDouble(meterTariff);
        double realUnits = Double.parseDouble(unit);
        //the charges from backend
        double realCharge = 100 - Integer.parseInt(perCharge);
        //to get the chargeAmount we are taking
        double theRealChargeAmount = Double.parseDouble(paymentAmount) * ( realCharge / 100 );
        String vendURL = "http://192.168.246.60:3030/transaction";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject databaseObjects = new JSONObject();
            databaseObjects.put("transID",refID);
            databaseObjects.put("meterID", meterID);
            databaseObjects.put("estateID", estateID);
            databaseObjects.put("amount", paymentAmount);      //original amount sent
            databaseObjects.put("tariff",  realTariff);
            databaseObjects.put("vat",  vat);
            databaseObjects.put("token", Token);
            databaseObjects.put("chargePer", realCharge);       //this is meant to be the percentage of the money
            databaseObjects.put("chargeAmount", theRealChargeAmount);            //this is meant to be the amount of the money
            databaseObjects.put("vendedAmount", Amount);        //the amount vended
            databaseObjects.put("units", realUnits);
            databaseObjects.put("channel", "PayStack");
            databaseObjects.put("vendedBy", role);
            databaseObjects.put("email", theEmail);

            JsonObjectRequest saveDatabaseRequest = new JsonObjectRequest(Request.Method.POST, vendURL, databaseObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        JSONObject data = response.getJSONObject("data");
                        String theDated = data.getString("date");
                        String theTime = data.getString("time");

                        // pass data to next page, to avoid constant going to database
                        Intent nextInt = new Intent(PaystackPayment.this, TheReceipt.class);
                        //ref, meterNo, token, amount, tariff, unit, date
                        nextInt.putExtra("repTransID", refID);
                        nextInt.putExtra("repMeterID", meterID);
                        nextInt.putExtra("repToken", Token);
                        //this is the original amount that has not been touched, in case im ask not to deduct from client
                        nextInt.putExtra("repAmount", paymentAmount);
                        nextInt.putExtra("repCharge", theRealChargeAmount);
                        nextInt.putExtra("repVat", vat);
                        nextInt.putExtra("repTariff", String.valueOf(realTariff));
                        nextInt.putExtra("redVendedAmount", Amount);
                        nextInt.putExtra("repUnits", realTariff);
                        nextInt.putExtra("repDate", theDated);
                        nextInt.putExtra("repTime", theTime);
                        startActivity(nextInt);

                        finish();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PaystackPayment.this, "Successful!!! ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(PaystackPayment.this, "Could not save to database", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    Toast.makeText(PaystackPayment.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(PaystackPayment.this, "Could not save to database", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(saveDatabaseRequest);
        } catch (Exception e) {
            Toast.makeText(PaystackPayment.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}