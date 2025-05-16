package com.cwg.thesmartutility.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.cwg.thesmartutility.PaystackPayment;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.TheReceipt;
import com.cwg.thesmartutility.UnverifiedPayment;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class VerifyingPay extends AppCompatActivity {

    String baseUrl, token, meterID, role, transRef, purAmount, theEmail, customerID, brand, pay_id, payment_type, duration, HasPayAcct, payBaseUrl;
    int estateID;
    SharedPreferences validSharedPref;
    private PreloaderLogo preloaderLogo;
    double TokenAmount, CWGChargeAmount;
    String perCharge, vat, theUsername, thePassword, meterTariff, customerName;
    DecimalFormat decimalFormat = new DecimalFormat("#.0000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.verifying_pay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // init
        preloaderLogo = new PreloaderLogo(this);

        // shared pref
        validSharedPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        baseUrl = this.getString(R.string.managementBaseURL);
        payBaseUrl = this.getString(R.string.payBaskURL);
        token = validSharedPref.getString("token", "");
        role = validSharedPref.getString("role", "");
        estateID = validSharedPref.getInt("estateID", 0);
        meterID = validSharedPref.getString("meterID", "");
        customerID = validSharedPref.getString("customerID", "");
        brand = validSharedPref.getString("brand", "");
        HasPayAcct = validSharedPref.getString("hasPayAcct", "");
        vat = validSharedPref.getString("vat", "");
        theUsername = validSharedPref.getString("chintUsername", "");
        thePassword = validSharedPref.getString("chintPassword", "");
        meterTariff = validSharedPref.getString("tariff", null);
        customerName = validSharedPref.getString("username", "");

        // get the intent
        Intent intent = getIntent();
        transRef = intent.getStringExtra("ref");
        purAmount = intent.getStringExtra("purAmount");
        payment_type = intent.getStringExtra("payment_type");
        duration = intent.getStringExtra("duration");
        pay_id = intent.getStringExtra("pay_id");
        // customerID = intent.getStringExtra("customerID");
        // brand = intent.getStringExtra("brand");

        // check the payment type
        checkPaymentType();

        // tell users to wait while token is being generated
        Toast.makeText(VerifyingPay.this, "Please wait while token is being generated!!!", Toast.LENGTH_SHORT).show();

    }

    // check the payment type
    private void checkPaymentType() {
        if (payment_type.equals("token")) {
            verifyToken();
        } else if (payment_type.equals("service")) {
            verifyService();
        }
    }

    // verify for token
    private void verifyToken() {
        preloaderLogo.show();
        String apiURL = payBaseUrl+"/g/verPaystack";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", estateID);
            requestData.put("hasPayAcct", HasPayAcct);
            requestData.put("reference", transRef);
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
                        String refer = dataObjects.getString("reference");
                        double tokenAmount = response.getDouble("tokenAmount");
                        double chargeAmount = response.getDouble("cwgAmount");
                        String paidAt = dataObjects.getString("paid_at");
                        TokenAmount = tokenAmount;
                        CWGChargeAmount = chargeAmount;

                        // get the percentage charge
                        JSONObject dataSubAccount = dataObjects.getJSONObject("subaccount");
                        //this is the cut that goes to the estate account that will be used to get the token
                        perCharge = dataSubAccount.getString("percentage_charge");
                        Log.d("Charge", perCharge);

                        // get the email
                        JSONObject dataCusAccount = dataObjects.getJSONObject("customer");
                        theEmail = dataCusAccount.getString("email");

                        Toast.makeText(VerifyingPay.this, "Verified", Toast.LENGTH_SHORT).show();
                        //String mainCharge = response.getString("theCharge");

                        //save the payment to DB
                        savePayment(refer);

                    } else  {
                        Toast.makeText(VerifyingPay.this, "Purchase failed", Toast.LENGTH_LONG).show();
                        // delete from pre-payment list
                        deleteFromPreList();
                        preloaderLogo.dismiss();

                    }

                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error:  Verification Failed", Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                    deleteFromPreList();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Please Try Again", Toast.LENGTH_LONG).show();
                preloaderLogo.dismiss();
                // delete from pre-payment list
                deleteFromPreList();
                //finish();
            });
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: Verification Failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // verify for service
    private void verifyService() {
        preloaderLogo.show();
        String apiURL = baseUrl+"/user/verservicecharge";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", estateID);
            requestData.put("hasPayAcct", HasPayAcct);
            requestData.put("reference", transRef);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
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

                        Toast.makeText(VerifyingPay.this, "Verified", Toast.LENGTH_SHORT).show();
                        //String mainCharge = response.getString("theCharge");

                        //save the payment to DB
                        saveServiceTransaction();

                    } else  {
                        Toast.makeText(VerifyingPay.this, "Payment failed", Toast.LENGTH_LONG).show();
                        // delete from pre-payment list
                        deleteFromPreList();
                        preloaderLogo.dismiss();
                    }

                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                    finish();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Error:  Verification Failed" , Toast.LENGTH_LONG).show();
                //finish();
                preloaderLogo.dismiss();
                // delete from pre-payment list
                deleteFromPreList();
            });
            // Set the RetryPolicy here
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            jsonObjectRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // save to payment
    private void savePayment(String ref) {
        String saveURL = baseUrl+"/g/savePayment";
        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("estateID", estateID);
                jsonRequest.put("meterID", meterID);
                jsonRequest.put("refID", ref);
                jsonRequest.put("amount", purAmount);
                jsonRequest.put("role", role);
                // and time
                jsonRequest.put("time", "");

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
                                verifyToken();
                            }

                        } catch (JSONException e) {
                            preloaderLogo.dismiss();
                            Toast.makeText(VerifyingPay.this, "Could not read response. Contact Admin", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("Payment", "Error: " + error.toString());
                        preloaderLogo.dismiss();
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
            Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
            preloaderLogo.dismiss();
        }
    }

    // check brand
    private void checkMeterBrand(){
        try {
            if (Objects.equals(brand, "Chint")) {
                generateChintToken();
            } else if (Objects.equals(brand, "Hexing")) {
                generateHexingToken();
            } else if (Objects.equals(brand, "Kelin")) {
                genKelinLoginToken();
            } else {
                Toast.makeText(VerifyingPay.this, "Your meter brand does not exist", Toast.LENGTH_SHORT).show();
                preloaderLogo.dismiss();
            }
        } catch (Exception e) {
            Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
            preloaderLogo.dismiss();
        }
    }

    // remove charges
    private double removeTheCharge(){
        // time for calculation
        double paidAmount;
        double tCharge = 0;//eg, 100 - 98.5
        try {
            if (HasPayAcct.equals("true")){
                // they have the higher percentage, so subtract
                //get the double charge and multiply by the amount
                tCharge = 100 - Double.parseDouble(perCharge);
                //Log.d("Charge", String.valueOf(tCharge));
            }else {
                // we have the lower percentage, no need to subtract
                tCharge = Double.parseDouble(perCharge);
                //Log.d("Charge", String.valueOf(tCharge));
            }
            paidAmount = Double.parseDouble(purAmount);
            return paidAmount - (paidAmount * (tCharge / 100));
            //Log.d("Payment Amount", String.valueOf(paidAmount));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid charge format");
            //Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        // Log.d("Return Payment Amount", String.valueOf(paidAmount - (paidAmount * (tCharge / 100))));
        //return paidAmount - (paidAmount * (tCharge / 100));
    }

    // kelin
    private void genKelinLoginToken() {
        //call the login API
        String apiURL = baseUrl+"/g/kelin/login";
        try {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
                try {
                    //get the login token
                    String loginToken = response.getString("token");
                    kelly(loginToken);
                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Couldn't connect to internet", Toast.LENGTH_SHORT).show();
                preloaderLogo.dismiss();
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
            Toast.makeText(VerifyingPay.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    private  void  kelly(String logToken) {
        //progressBar.setVisibility(View.VISIBLE);
        LocalDateTime localDateTime = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            localDateTime = LocalDateTime.now();
        }
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
        assert localDateTime != null;
        String theDate = localDateTime.toString();
        Log.d("Current Time", theDate);
        String vendURL = baseUrl+"/g/kelin/vend";
        JSONObject vendingObjects = new JSONObject();
        try {
            vendingObjects.put("token", logToken);
            vendingObjects.put("meterNo", meterID);
            vendingObjects.put("tariff", meterTariff);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", TokenAmount);   // amount with removed charge
            vendingObjects.put("date", theDate);
            vendingObjects.put("vat", Double.parseDouble(vat));     // in percentage
            // add the rules to first verify payment
            vendingObjects.put("estateID", estateID);
            vendingObjects.put("hasPayAcct", HasPayAcct);
            vendingObjects.put("reference", transRef);
        } catch (JSONException e) {
            Toast.makeText(VerifyingPay.this,"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        new VerifyingPay.kellyTransactionDataTask(vendURL, vendingObjects).execute();
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
                Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
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
                        saveTokenTransaction(kelinToken, kelinUnits);

                    } catch (JSONException e) {
                        Toast.makeText(VerifyingPay.this, "Kindly check your connection and click the button again", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                        //throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Kindly check your connection and click the button again", Toast.LENGTH_SHORT).show();
                    preloaderLogo.dismiss();
                }
            }
        }
    }

    // chint
    private void generateChintToken() {
        //chint tariff is preset in the system and the amount should not be minus
        String ChintAmount = decimalFormat.format(TokenAmount);
        // String customerID = customerID;
        String pass = thePassword;
        String user = theUsername;
        double theVat= Double.parseDouble(vat);
        String vendURL = baseUrl+"/g/chint/vend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("customerId", customerID);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", ChintAmount);
            vendingObjects.put("password", pass);
            vendingObjects.put("username", user);
            vendingObjects.put("vat", theVat);
            // to first verify payment
//            vendingObjects.put("estateID", estateID);
//            vendingObjects.put("hasPayAcct", HasPayAcct);
//            vendingObjects.put("reference", refID);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        //to get the token
                        String ChintToken = response.getString("token");
                        //to get the energy unit
                        String ChintUnits = response.getString("units");
                        //for the change. Confirm from yogesh
//                        JSONObject theChangeAmount = transResult.getJSONObject("ChangeAmount");
//                        String ChangeAmount = theEnergy.getString("_text");

                        saveTokenTransaction(ChintToken, ChintUnits);
                    } else {
                        Toast.makeText(VerifyingPay.this, "Kindly check your connection and click the button again", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }

                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                // Display an AlertDialog with the error message
                new androidx.appcompat.app.AlertDialog.Builder(VerifyingPay.this)
                        .setTitle("Try Again!!!")
                        .setMessage("Kindly check your connection")
                        .setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            // go to the prepayment activity
                            startActivity(new Intent(VerifyingPay.this, UnverifiedPayment.class));
                            finish();
                        })
                        .show();
//                Toast.makeText(PaystackPayment.this, "Kindly check your connection and click the button again", Toast.LENGTH_LONG).show();
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
            int socketTimeout = 100000;  // 100 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            vendTokenRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(VerifyingPay.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    // hexing
    private void generateHexingToken() {
        //String HexingAmount = decimalFormat.format(PaidAmount);
        //electricity
        int service = 0;
        double amount = TokenAmount;
        Log.d("Amount", String.valueOf(amount));
        double theVat = Double.parseDouble(vat);
        //the real figure is meant to be the one that i get after i divide the amount by the tariff and send to the system
        double tar = Double.parseDouble(meterTariff);
        String vendURL = baseUrl+"/g/hexing/vend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            try {
                vendingObjects.put("meterID", meterID);
                vendingObjects.put("transactionId", transRef);
                vendingObjects.put("service", service);
                // i have to do the calculation on the backend
                vendingObjects.put("amount", amount);
                vendingObjects.put("tariff", tar);
                vendingObjects.put("vat", theVat);
                // to first verify payment
//                vendingObjects.put("estateID", estateID);
//                vendingObjects.put("hasPayAcct", HasPayAcct);
//                vendingObjects.put("reference", transRef);
            } catch (JSONException e) {
                Toast.makeText(VerifyingPay.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            //i am meant to pass in the real amount to vend the token
            //vendingObjects.put("value", valueToSend);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    String HexingStatus = response.getString("statusCode");
                    if (HexingStatus.equals("0")) {
                        JSONObject vendData = response.getJSONObject("vendingData");
                        String tokenData = vendData.getString("tokenDec");
                        String tokenUnit = vendData.getString("unitsActual");

                        saveTokenTransaction(tokenData, tokenUnit);
                    }else {
                        Toast.makeText(VerifyingPay.this, "Could not generate Token. Contact Admin", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }
                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Wrong amount", Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
                preloaderLogo.dismiss();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            int socketTimeout = 30000;  // 30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            vendTokenRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(VerifyingPay.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    // save token transaction
    private void saveTokenTransaction(String Token, String unit) {
        double Amount = removeTheCharge() - (removeTheCharge() * Double.parseDouble(vat)/100);
        //double RealAmount = Double.parseDouble(realAmount);
        double realTariff = Double.parseDouble(meterTariff);
        double realUnits = Double.parseDouble(unit);
        //the charges from backend
        double realCharge ;
        if (HasPayAcct.equals("true")){
            // they have the higher percentage, so subtract
            //get the double charge and multiply by the amount
            realCharge = 100 - Double.parseDouble(perCharge);
            //Log.d("Charge", String.valueOf(tCharge));
        }else {
            // we have the lower percentage, no need to subtract
            realCharge = Double.parseDouble(perCharge);
            //Log.d("Charge", String.valueOf(tCharge));
        }
        //to get the chargeAmount we are taking
        double theRealChargeAmount = Double.parseDouble(purAmount) * ( realCharge / 100 );
        String vendURL = baseUrl+"/g/saveTrans";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject databaseObjects = new JSONObject();
            databaseObjects.put("transID",transRef);
            databaseObjects.put("meterID", meterID);
            databaseObjects.put("estateID", estateID);
            databaseObjects.put("amount", purAmount);      //original amount sent
            databaseObjects.put("tariff",  realTariff);
            databaseObjects.put("vat",  vat);
            databaseObjects.put("token", Token);
            databaseObjects.put("chargePer", realCharge);       //this is meant to be the percentage of the money
            databaseObjects.put("chargeAmount", CWGChargeAmount);            //this is meant to be the amount of the money charge
            databaseObjects.put("vendedAmount", TokenAmount);        //the amount vended
            databaseObjects.put("units", realUnits);
            databaseObjects.put("channel", "PayStack");
            databaseObjects.put("vendedBy", role);
            databaseObjects.put("email", theEmail);
            // pass the customer name
            databaseObjects.put("username", customerName);
            databaseObjects.put("day", getCurrentDate());
            databaseObjects.put("time", getCurrentTime());

            JsonObjectRequest saveDatabaseRequest = new JsonObjectRequest(Request.Method.POST, vendURL, databaseObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        preloaderLogo.dismiss();
                        Toast.makeText(VerifyingPay.this, "Successful!!! ", Toast.LENGTH_SHORT).show();
                        //JSONObject data = response.getJSONObject("data");
//                        String theDated = data.getString("date");
//                        String theTime = data.getString("time");
                        String time = response.getString("time");
                        String date = response.getString("day");

                        // pass data to next page, to avoid constant going to database
                        Intent nextInt = new Intent(VerifyingPay.this, TheReceipt.class);
                        //ref, meterNo, token, amount, tariff, unit, date
                        nextInt.putExtra("repTransID", transRef);
                        nextInt.putExtra("repMeterID", meterID);
                        nextInt.putExtra("repToken", Token);
                        //this is the original amount that has not been touched, in case im ask not to deduct from client
                        nextInt.putExtra("repAmount",  String.valueOf(purAmount));
                        nextInt.putExtra("repCharge", String.valueOf(CWGChargeAmount));
                        nextInt.putExtra("repVat", vat);
                        nextInt.putExtra("repTariff", String.valueOf(realTariff));
                        nextInt.putExtra("redVendedAmount",  String.valueOf(TokenAmount));
                        nextInt.putExtra("repUnits",  String.valueOf(realUnits));
                        nextInt.putExtra("repDate", date);
                        nextInt.putExtra("repTime", time);
                        startActivity(nextInt);

                        finish();
                    }
                    else {
                        Toast.makeText(VerifyingPay.this, "Could not save to database", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                        finish();
                    }

                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                    finish();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Could not save to database", Toast.LENGTH_LONG).show();
                preloaderLogo.dismiss();
                finish();
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
            Toast.makeText(VerifyingPay.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
            finish();
        }
    }

    // save service transaction
    private void saveServiceTransaction() {
        String vendURL = baseUrl+"/user/saveservicecharge";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject databaseObjects = new JSONObject();
            databaseObjects.put("reference",transRef);
            databaseObjects.put("estateID", estateID);
            databaseObjects.put("amount", purAmount);
            databaseObjects.put("duration", duration);

            JsonObjectRequest saveDatabaseRequest = new JsonObjectRequest(Request.Method.POST, vendURL, databaseObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        JSONObject dataObject = response.getJSONObject("data");
                        String PaymentDate = dataObject.getString("payment_date");
                        String ExpiryDate = dataObject.getString("expiry_date");
                        String payAmount = dataObject.getString("sub_amount");

                        // pass data to next page, to avoid constant going to database
                        Intent nextInt = new Intent(VerifyingPay.this, ServiceReceipt.class);
                        //ref, meterNo, token, amount, tariff, unit, date
                        nextInt.putExtra("serviceRefID", transRef);
                        //this is the original amount that has not been touched, in case im ask not to deduct from client
                        nextInt.putExtra("payAmount",  payAmount);
                        nextInt.putExtra("payDate", PaymentDate);
                        nextInt.putExtra("expDate", ExpiryDate);
                        nextInt.putExtra("payDuration", duration);

                        startActivity(nextInt);

                        finish();
                        preloaderLogo.dismiss();
                        Toast.makeText(VerifyingPay.this, "Successful!!! ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(VerifyingPay.this, "Could not save to database", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                    finish();
                }
            }, error -> {
                Toast.makeText(VerifyingPay.this, "Could not save to database", Toast.LENGTH_LONG).show();
                preloaderLogo.dismiss();
                finish();
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
            Toast.makeText(VerifyingPay.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
            finish();
        }
    }

    // delete from the pre-payment list and go back to the list
    private void deleteFromPreList() {
        preloaderLogo.show();
        String deleteURL = baseUrl+"/g/deletePrePayment/?transID="+transRef+"&amount="+purAmount;
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteURL, null, response -> {
            try {
                preloaderLogo.dismiss();
                //get the message
                String message = response.getString("message");
                if (message.equals("success")){
                    Toast.makeText(VerifyingPay.this, "Deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerifyingPay.this, "Error: " + message , Toast.LENGTH_LONG).show();
                }
                //go to the unverified activity
                startActivity(new Intent(VerifyingPay.this, UnverifiedPayment.class));
                finish();
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(VerifyingPay.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                // go to the unverified activity
                startActivity(new Intent(VerifyingPay.this, UnverifiedPayment.class));
                finish();
            }

        }, error -> {
            preloaderLogo.dismiss();
            Toast.makeText(VerifyingPay.this, "Could not delete from database", Toast.LENGTH_LONG).show();
            // go to the unverified activity
            startActivity(new Intent(VerifyingPay.this, UnverifiedPayment.class));
            finish();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        int socketTimeout = 5000;  // 5 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        deleteRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }

    public String getCurrentDate() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd"); // Only date
        return dateFormatter.format(new Date());
    }

    public String getCurrentTime() {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss"); // Only time
        // Set the timezone to GMT+1
        timeFormatter.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return timeFormatter.format(new Date());
    }
}