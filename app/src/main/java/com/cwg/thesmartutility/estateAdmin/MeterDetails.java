package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.PaystackPayment;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MeterDetails extends AppCompatActivity {

    TextView meterNum, meterName, meterEmail, meterDate, meterUnits, meterAmount, meterStatus;
    SwitchCompat theSwitch;
    Button generateButton, proceedButton;
    TextInputEditText amountInput, phoneInput;
    TextInputLayout amountLayout, phoneLayout;
    String AmountText, PhoneText;
    SharedPreferences validSharedPref, utilityPref;
    BottomSheetDialog bottomSheetDialog;
    private final int minMoneyLength = 4;
    private final int maxAmount = 100000;
    String theMeterName, theMeterNum, theMeterEmail, theMeterLastDate, theMeterStatus;
    private ProgressBar progressBar;
    DecimalFormat decimalFormat = new DecimalFormat("#.0000");
    private int retryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.meter_details);

        // ids
        generateButton = findViewById(R.id.detailsGenerate);
        theSwitch = findViewById(R.id.detailsSwitch);
        meterNum = findViewById(R.id.detailsMeterNum);
        meterName = findViewById(R.id.detailsMeterName);
        meterEmail = findViewById(R.id.detailsMeterEmail);
        meterDate = findViewById(R.id.detailsMeterLast);
        meterUnits = findViewById(R.id.detailsMeterUnits);
        meterAmount = findViewById(R.id.detailsMeterAmount);
        meterStatus = findViewById(R.id.transStatus);
        // the bottom sheet

        // the sharedPref for token
        validSharedPref = getSharedPreferences("estateVendPref", Context.MODE_PRIVATE);
        utilityPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // get and set the text and status
        theMeterName = validSharedPref.getString("meterName", null);
        theMeterNum = validSharedPref.getString("meterID", null);
        theMeterEmail = validSharedPref.getString("email", null);
        theMeterLastDate = validSharedPref.getString("lastPurchase", null);
        String theMeterAmount = validSharedPref.getString("totalAmount", null);
        theMeterStatus = validSharedPref.getString("vendStatus", null);
        String theMeterUnits = validSharedPref.getString("totalUnits", null);
        PhoneText = validSharedPref.getString("phone", null);
        meterNum.setText(theMeterNum);
        meterName.setText(theMeterName);
        meterEmail.setText(theMeterEmail);
        meterDate.setText(theMeterLastDate);
        meterUnits.setText(theMeterUnits);
        meterAmount.setText(theMeterAmount);
        // check if enabled or disabled
        assert theMeterStatus != null;
        if (theMeterStatus.equals("Enabled")){
            meterStatus.setText(theMeterStatus);
            meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
            // put on the switch
            theSwitch.setChecked(true);
        } else if (theMeterStatus.equals("Disabled")) {
            meterStatus.setText(theMeterStatus);
            meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
            // put on the switch
            theSwitch.setChecked(false);
        }


        // work on the switch
        theSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            // check the switch status
            String status = isChecked ? "Enabled" : "Disabled";
            updateStatus(status, theMeterNum);
        }));

        // bring out the bottom sheet
        generateButton.setOnClickListener(v -> {
            showBottomSheet();
        });


        // get the amount
        // go to payStack
    }

    // show bottom sheet
    private void showBottomSheet() {
        // get the bottom sheet
        bottomSheetDialog = new BottomSheetDialog(MeterDetails.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet, findViewById(R.id.mainRelative), false);
        // ids of the items in the sheet
        amountInput = bottomSheetView.findViewById(R.id.amountInput);
        amountLayout = bottomSheetView.findViewById(R.id.amountLayout);
        phoneInput = bottomSheetView.findViewById(R.id.phoneInput);
        phoneLayout = bottomSheetView.findViewById(R.id.phoneLayout);
        proceedButton = bottomSheetView.findViewById(R.id.detailsProceed);
        // set the phone text
        phoneInput.setText(PhoneText);
        phoneInput.setClickable(false);
        phoneInput.setFocusable(false);
        phoneInput.setEnabled(false);

        // button to taken the amount and generate the link
        proceedButton.setOnClickListener(v -> {
            AmountText = Objects.requireNonNull(amountInput.getText()).toString().trim();
            if (AmountText.isEmpty()){
                amountLayout.setErrorEnabled(true);
                amountLayout.setError("Input a number");

            } else if(AmountText.length() < minMoneyLength ){ // run the checks
                amountLayout.setError("Amount must be more than ₦1000");
                amountLayout.setErrorEnabled(true);
            } else if(Integer.parseInt(AmountText) > maxAmount) {
                amountLayout.setError("Max Amount is ₦100,000");
                amountLayout.setErrorEnabled(true);
            }else{

                // check if the admin has the access to use vend and use the gateway
                String gatewayStatus = validSharedPref.getString("estateGatewayStatus", null);
                String vendStatus = validSharedPref.getString("estateVendStatus", null);
                // runChecks
                assert gatewayStatus != null;
                assert vendStatus != null;
                runChecks(gatewayStatus, vendStatus);
            }

            // Close the bottom sheet
            bottomSheetDialog.dismiss();
        });

        // Show the BottomSheetDialog
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    // update status
    private void updateStatus (String stat, String metID) {
        String apiURL = "http://192.168.246.60:5050/estate/updateMeterStatus";
        String token =  utilityPref.getString("token", null);
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("status", stat);
            requestData.put("meterID", metID );
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        String status = response.getString("status");
                        // if it works change the text
                        if (status.equals("Enabled")){
                            meterStatus.setText(status);
                            meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
                            // call api to change the database
                        } else { // disabled
                            meterStatus.setText(status);
                            // change the colour
                            meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
                        }
                    } else {
                        Toast.makeText(MeterDetails.this, "No Meter Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(MeterDetails.this, "Please try again", Toast.LENGTH_LONG).show()){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Add the request to the RequestQueue
            VolleySingleton.getInstance(this).addToRequestQueue(objectRequest);
        } catch (Exception e) {
            Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // run checks
    private void runChecks(String gatewayStatus, String vendStatus) {
        String meRef = generateUniqueTransactionReference();
        // gatewayStatus is either true or false, vendStatus is either enabled or disabled
        if (gatewayStatus.equals("true") && vendStatus.equals("Enabled")) {
            // initiate link
            initiate(meRef);
        } else if (gatewayStatus.equals("false") && vendStatus.equals("Enabled")) {
            // check meter brand
            checkMeterBrand(meRef);
        } else if (gatewayStatus.equals("true") && vendStatus.equals("Disabled")) {
            // Toast
            Toast.makeText(MeterDetails.this, "Unable to vend. Kindly contact admin!!", Toast.LENGTH_LONG).show();
        } else if (gatewayStatus.equals("false") && vendStatus.equals("Disabled")) {
            Toast.makeText(MeterDetails.this, "Unable to vend. Kindly contact admin!!", Toast.LENGTH_LONG).show();
        }
    }

    // generate the ref
    private String generateUniqueTransactionReference() {
        String meter = validSharedPref.getString("meterID", null);
        Calendar now = Calendar.getInstance();

        // Get the hours, minutes, and seconds of the current time
        int hours = now.get(Calendar.HOUR_OF_DAY);
        int minutes = now.get(Calendar.MINUTE);
        int seconds = now.get(Calendar.SECOND);

        // Convert the hours and minutes to seconds and add up
        long secondsElapsed = hours * 3600 + minutes * 60 + seconds;
        return "CWG" + meter + secondsElapsed;
    }

    private void checkMeterBrand(String refID){
        String meterBrand = validSharedPref.getString("brand", null);
        //progressBar.setVisibility(View.VISIBLE);
        try {
            if (Objects.equals(meterBrand, "Chint")) {
                genChintToken();
            } else if (Objects.equals(meterBrand, "Hexing")) {
                genHexingToken(refID);
            } else if (Objects.equals(meterBrand, "Kelin")) {
                genKelinLoginToken();
            } else {
                Toast.makeText(MeterDetails.this, "Your meter brand does not exist", Toast.LENGTH_SHORT).show();
                //progressBar.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
            //progressBar.setVisibility(View.GONE);
        }
    }

    // initiate payment
    public void initiate(String meRef) {
        //get amount in int
        int Amount = Integer.parseInt(AmountText);
        int meterEstateID = utilityPref.getInt("estateID", 0);
        String apiURL = "http://192.168.246.74:3030/generateCheckoutSession";
        try {
            JSONObject requestData = new JSONObject();
            //i can also add the meterNumber in the code to see if it works
            requestData.put("estateID", meterEstateID);
            //send the amount to the backend where the heavy lifting will be done
            requestData.put("amount", Amount);
            requestData.put("reference", meRef);
            requestData.put("email", theMeterEmail);
            requestData.put("meter", theMeterNum);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                //get the authorization_URL and point to the checkoutPage
                try {
                    //i can add a progressBar to load so that the user can know something is happening
                    JSONObject jsonObject = response.getJSONObject("data");
                    String checkoutUrl = jsonObject.getString("authorization_url");
                    //the referenceID of the transaction which be used to verify
                    String refID = jsonObject.getString("reference");

                    Intent intent = new Intent(MeterDetails.this, PaystackPayment.class);
                    intent.putExtra("link", checkoutUrl);
                    intent.putExtra("transRef", refID);
                    intent.putExtra("purAmount", Amount);
                    intent.putExtra("estateID", meterEstateID);
                    startActivity(intent);

                } catch (JSONException e) {
                    Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //throw new RuntimeException(e);
                }
            }, error -> Toast.makeText(MeterDetails.this, "Could not connect to network", Toast.LENGTH_LONG).show());

            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void genKelinLoginToken() {
        String token = utilityPref.getString("token", null);
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
                    Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //progressBar.setVisibility(View.GONE);
                }
            }, error -> Toast.makeText(MeterDetails.this, "Couldn't connect to internet", Toast.LENGTH_SHORT).show()
            ){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(jsonRequest);
        } catch (Exception e) {
            Toast.makeText(MeterDetails.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            //progressBar.setVisibility(View.GONE);
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
            vendingObjects.put("meterNo", theMeterNum);
            vendingObjects.put("tariff", meterTariff);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", AmountText);
            vendingObjects.put("date", theDate);
        } catch (JSONException e) {
            Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        new MeterDetails.kellyTransactionDataTask(vendURL, vendingObjects).execute();
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
                Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                        saveToDB(kelinToken, kelinUnits);

                    } catch (JSONException e) {
                        Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        //throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    Toast.makeText(MeterDetails.this, "Could not get the data", Toast.LENGTH_SHORT).show();
                    //progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    private void genChintToken() {
        String token = utilityPref.getString("token", null);
        double am = Double.parseDouble(AmountText);
        String meterCustomerID =  validSharedPref.getString("customerID", null);
        String meterPassword =  utilityPref.getString("chintPassword", null);
        String meterUsername =  utilityPref.getString("gatewayStatus", null);
        //chint tariff is preset in the system and the amount should not be minus
        String ChintAmount = decimalFormat.format(am);
        String vendURL = "http://192.168.246.60:3030/chintVend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("customerId", meterCustomerID);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", ChintAmount);
            vendingObjects.put("password", meterPassword);
            vendingObjects.put("username", meterUsername);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    // i am to edit this
                    JSONObject envelope = response.getJSONObject("soap:Envelope");
                    JSONObject body= envelope.getJSONObject("soap:Body");
                    JSONObject transRes = body.getJSONObject("TransactionResponse");
                    JSONObject transResult = transRes.getJSONObject("TransactionResult");
                    //to get the result
                    JSONObject theResult = transResult.getJSONObject("Result");
                    String ChintResult = theResult.getString("_text");

                    //just in case the system starts misbehaving
                    // this will not be needed
                    if (ChintResult.equals("false")) {
                        //so if it returns false, try again
                        retryCount++;
                        Log.d("Recursive count", String.valueOf(retryCount));
                        genChintToken();
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

                        saveToDB(ChintToken, ChintUnits);
                        //after saving, print the pdf
                    }
                } catch (JSONException e) {
                    Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(MeterDetails.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
                //progressBar.setVisibility(View.GONE);
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
            Toast.makeText(MeterDetails.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            //progressBar.setVisibility(View.GONE);
        }
    }

    private void genHexingToken(String meRef) {
        //String HexingAmount = decimalFormat.format(PaidAmount);
        String theMeterID = theMeterNum;
        //electricity
        int service = 0;
        double amount = Double.parseDouble(AmountText);
        String meterTariff = validSharedPref.getString("tariff", null);
        String token = utilityPref.getString("token", null);
        //the real figure is meant to be the one that i get after i divide the amount by the tariff and send to the system
        double tar = Double.parseDouble(meterTariff);
        String vendURL = "http://192.168.246.60:3030/hexingVend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("meterID", theMeterID);
            vendingObjects.put("transactionId", meRef);
            vendingObjects.put("service", service);
            // i have to do the calculation on the backend
            vendingObjects.put("amount", amount);
            vendingObjects.put("tariff", tar);
            //i am meant to pass in the real amount to vend the token
            //vendingObjects.put("value", valueToSend);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    String HexingStatus = response.getString("statusCode");
                    if (HexingStatus.equals("0")) {
                        JSONObject vendData = response.getJSONObject("vendingData");
                        String tokenData = vendData.getString("tokenDec");
                        String tokenUnit = vendData.getString("unitsActual");

                        saveToDB(tokenData, tokenUnit);
                    }else {
                        Toast.makeText(MeterDetails.this, "Could not generate Token. Contact Admin", Toast.LENGTH_LONG).show();
                        //progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    Toast.makeText(MeterDetails.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //progressBar.setVisibility(View.GONE);
                }
            }, error -> {
                Toast.makeText(MeterDetails.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
                //progressBar.setVisibility(View.GONE);
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
            Toast.makeText(MeterDetails.this, "An Error occurred", Toast.LENGTH_SHORT).show();
            //progressBar.setVisibility(View.GONE);
        }
    }

    private void saveToDB(String tokenNum, String tokenUnit){
        String token = utilityPref.getString("token", null);

    }
}