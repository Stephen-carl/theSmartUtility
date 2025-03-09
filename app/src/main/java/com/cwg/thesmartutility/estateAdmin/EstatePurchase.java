package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.cwg.thesmartutility.PaystackPayment;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.TheReceipt;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class EstatePurchase extends AppCompatActivity {
    private final int minMoneyLength = 3;
    private final int maxAmount = 100000;
    DecimalFormat decimalFormat = new DecimalFormat("#.0000");
    private int retryCount = 0;
    String theMeterName, theMeterNum, theMeterEmail, token, PurchaseAmount, vat, meRef, meterTariff, HasPayAcct, payBaseUrl, baseUrl;
    TextView estatePur1000, estatePur2000, estatePur5000, estatePur10000, estatePur20000, estatePur30000, estatePur50000, estatePur100000;
    SharedPreferences validSharedPref, utilityPref;
    SharedPreferences.Editor prefEditor;
    Button purchaseAmountButton;
    ImageView purchaseBack;
    TextInputEditText purchaseAmountInput;
    TextInputLayout purchaseAmountLayout;
    EditText purchaseMeterNumber, purchaseEmail;
    private PreloaderLogo preloaderLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_purchase);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        payBaseUrl = this.getString(R.string.payBaskURL);
        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        purchaseAmountInput = findViewById(R.id.estatePurchaseAmount);
        purchaseAmountButton = findViewById(R.id.estatePurchaseAmountButton);
        purchaseAmountLayout = findViewById(R.id.estateAmountLayout);
        purchaseBack = findViewById(R.id.estatePurchaseBack);
        estatePur1000 = findViewById(R.id.estatePur1000);
        estatePur2000 = findViewById(R.id.estatePur2000);
        estatePur5000 = findViewById(R.id.estatePur5000);
        estatePur100000 = findViewById(R.id.estatePur100000);
        estatePur10000 = findViewById(R.id.estatePur10000);
        estatePur20000 = findViewById(R.id.estatePur20000);
        estatePur30000 = findViewById(R.id.estatePur30000);
        estatePur50000 = findViewById(R.id.estatePur50000);
        purchaseMeterNumber = findViewById(R.id.estatePurchaseMeterNumber);
        purchaseEmail = findViewById(R.id.estatePurchaseEmail);
        preloaderLogo = new PreloaderLogo(this);

        // get the sharedPref
        validSharedPref = getSharedPreferences("estateVendPref", Context.MODE_PRIVATE);
        utilityPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        prefEditor = validSharedPref.edit();
        vat = utilityPref.getString("vat", null);
        token = utilityPref.getString("token", null);
        theMeterName = validSharedPref.getString("meterName", "");
        theMeterNum = validSharedPref.getString("meterID", "");
        theMeterEmail = validSharedPref.getString("email", "");
        // still work on this
        HasPayAcct = validSharedPref.getString("hasPayAcct", "");
        meterTariff = validSharedPref.getString("tariff", null);

        // set the edittext
        purchaseMeterNumber.setText(theMeterNum);
        purchaseEmail.setText(theMeterEmail);


        // set the edittext when any of the amount is clicked
        estatePur1000.setOnClickListener(v -> purchaseAmountInput.setText("1000"));
        estatePur2000.setOnClickListener(v -> purchaseAmountInput.setText("2000"));
        estatePur5000.setOnClickListener(v -> purchaseAmountInput.setText("5000"));
        estatePur10000.setOnClickListener(v -> purchaseAmountInput.setText("10000"));
        estatePur20000.setOnClickListener(v -> purchaseAmountInput.setText("20000"));
        estatePur30000.setOnClickListener(v -> purchaseAmountInput.setText("30000"));
        estatePur50000.setOnClickListener(v -> purchaseAmountInput.setText("50000"));
        estatePur100000.setOnClickListener(v -> purchaseAmountInput.setText("100000"));

        // when the back image is pressed
        purchaseBack.setOnClickListener(v -> finish());

        // when the button is pressed
        purchaseAmountButton.setOnClickListener(v -> init());

    }

    private void init() {
        PurchaseAmount = Objects.requireNonNull(purchaseAmountInput.getText()).toString().trim();
        if (PurchaseAmount.isEmpty()){
            purchaseAmountLayout.setErrorEnabled(true);
            purchaseAmountLayout.setError("Input a number");
            Toast.makeText(EstatePurchase.this, "Input a number", Toast.LENGTH_SHORT).show();
        } else if(PurchaseAmount.length() < minMoneyLength ){ // run the checks
            purchaseAmountLayout.setError("Amount must be more than ₦1000");
            purchaseAmountLayout.setErrorEnabled(true);
            Toast.makeText(EstatePurchase.this, "Amount must be more than ₦1000", Toast.LENGTH_SHORT).show();
        } else if(Integer.parseInt(PurchaseAmount) > maxAmount) {
            purchaseAmountLayout.setError("Max Amount is ₦100,000");
            purchaseAmountLayout.setErrorEnabled(true);
            Toast.makeText(EstatePurchase.this, "Max Amount is ₦100,000", Toast.LENGTH_SHORT).show();
        }else{

            // check if the admin has the access to use vend and use the gateway
            String gatewayStatus = validSharedPref.getString("estateGatewayStatus", null);
            String vendStatus = validSharedPref.getString("estateVendStatus", null);
            // runChecks
            assert gatewayStatus != null;
            Log.d("Gateway Status", gatewayStatus);
            assert vendStatus != null;
            Log.d("Vend Status", vendStatus);
            // run the checks
            runChecks(gatewayStatus, vendStatus);
        }
    }

    private void runChecks(String gatewayStatus, String vendStatus) {
        meRef = generateUniqueTransactionReference();
        Log.d("Ref", meRef);
        // gatewayStatus is either true or false, vendStatus is either enabled or disabled
        if (gatewayStatus.equals("true") && vendStatus.equals("Enabled")) {
            // initiate link
            initiate(meRef);
        } else if (gatewayStatus.equals("false") && vendStatus.equals("Enabled")) {
            // check meter brand
            checkMeterBrand(meRef);
        } else if (gatewayStatus.equals("true") && vendStatus.equals("Disabled")) {
            Toast.makeText(EstatePurchase.this, "Unable to vend. Kindly contact admin!!", Toast.LENGTH_LONG).show();
        } else if (gatewayStatus.equals("false") && vendStatus.equals("Disabled")) {
            Toast.makeText(EstatePurchase.this, "Unable to vend. Kindly contact admin!!", Toast.LENGTH_LONG).show();
        }
    }

    // get ref
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

    private void checkMeterBrand(String refID){
        String meterBrand = validSharedPref.getString("brand", null);
        preloaderLogo.show();
        try {
            if (Objects.equals(meterBrand, "Chint")) {
                genChintToken();
            } else if (Objects.equals(meterBrand, "Hexing")) {
                genHexingToken(refID);
            } else if (Objects.equals(meterBrand, "Kelin")) {
                genKelinLoginToken();
            } else {
                Toast.makeText(EstatePurchase.this, "Your meter brand does not exist", Toast.LENGTH_SHORT).show();
                preloaderLogo.dismiss();
            }
        } catch (Exception e) {
            Toast.makeText(EstatePurchase.this, e.getMessage(), Toast.LENGTH_LONG).show();
            preloaderLogo.dismiss();
        }
    }

    // fetch payment link
    public void initiate(String meRef) {
        preloaderLogo.show();
        //get amount in int
        int Amount = Integer.parseInt(PurchaseAmount);
        int meterEstateID = utilityPref.getInt("estateID", 0);
        Log.d("Estate ID", String.valueOf(meterEstateID));
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
                    Log.d("Link", checkoutUrl);
                    //the referenceID of the transaction which be used to verify
                    String refID = jsonObject.getString("reference");
                    Log.d("Checkout URL", checkoutUrl);

                    prefEditor.putString("paymentAmount", String.valueOf(Amount));
                    prefEditor.apply();

                    preloaderLogo.dismiss();

                    Intent intent = new Intent(EstatePurchase.this, PaystackPayment.class);
                    intent.putExtra("link", checkoutUrl);
                    Log.d("Link", checkoutUrl);
                    intent.putExtra("transRef", refID);
                    intent.putExtra("purAmount", PurchaseAmount);
                    intent.putExtra("estateID", meterEstateID);
                    intent.putExtra("meterID", theMeterNum);
                    intent.putExtra("customerName", theMeterName);
                    startActivity(intent);

                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(EstatePurchase.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
                    //throw new RuntimeException(e);
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(EstatePurchase.this, "Could not connect to network", Toast.LENGTH_LONG).show();
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

    private void genKelinLoginToken() {
        String token = utilityPref.getString("token", null);
        //progressBar.setVisibility(View.VISIBLE);
        //call the login API
        String apiURL = baseUrl+"/g/kelin/login";
        try {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
                try {
                    //get the login token
                    String loginToken = response.getString("token");
                    kelly(loginToken);
                } catch (JSONException e) {
                    Toast.makeText(EstatePurchase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(EstatePurchase.this, "Couldn't connect to internet", Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(EstatePurchase.this, "An Error occurred" + e.getMessage(), Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    private  void  kelly(String logToken) {
        meterTariff = validSharedPref.getString("tariff", null);
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
        Log.d("Token", logToken);
        Log.d("Meter", theMeterNum);
        Log.d("Tariff", meterTariff);
        Log.d("Date", theDate);
        Log.d("Vat", vat);
        Log.d("Amount", PurchaseAmount);
        try {
            vendingObjects.put("token", logToken);
            vendingObjects.put("meterNo", theMeterNum);
            vendingObjects.put("tariff", meterTariff);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", PurchaseAmount);
            vendingObjects.put("date", theDate);
            vendingObjects.put("vat", Double.parseDouble(vat));
        } catch (JSONException e) {
            preloaderLogo.dismiss();
            Toast.makeText(EstatePurchase.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
        }
        new EstatePurchase.kellyTransactionDataTask(vendURL, vendingObjects).execute();
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
                preloaderLogo.dismiss();
                Toast.makeText(EstatePurchase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                        // JSONObject dataObject = response.getJSONObject("data");
                        String kelinToken = response.getString("payToken");
                        String kelinUnits = response.getString("payKWH");
                        saveToDB(kelinToken, kelinUnits);

                    } catch (JSONException e) {
                        Toast.makeText(EstatePurchase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                        //throw new RuntimeException(e);
                    }
                } catch (JSONException e) {
                    Toast.makeText(EstatePurchase.this, "Could not get the data" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    preloaderLogo.dismiss();
                }
            }
        }
    }

    private void genChintToken() {
        String token = utilityPref.getString("token", null);
        double am = Double.parseDouble(PurchaseAmount);
        String meterCustomerID =  validSharedPref.getString("customerID", null);
        String meterPassword =  utilityPref.getString("chintPassword", null);
        String meterUsername =  utilityPref.getString("chintUsername", null);
        //chint tariff is preset in the system and the amount should not be minus
        String ChintAmount = decimalFormat.format(am);
        String vendURL = baseUrl+"/g/chint/vend";
        double theVat= Double.parseDouble(vat);
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("customerId", meterCustomerID);
            //i am meant to pass in the real amount to vend the token
            vendingObjects.put("amount", ChintAmount);
            vendingObjects.put("password", meterPassword);
            vendingObjects.put("username", meterUsername);
            vendingObjects.put("vat", theVat);
            JsonObjectRequest vendTokenRequest = new JsonObjectRequest(Request.Method.POST, vendURL, vendingObjects, response -> {
                try {
                    // i am to edit this
                    String message = response.getString("message");
                    if (message.equals("success")){
                        //to get the token
                        String ChintToken = response.getString("token");
                        //to get the energy unit
                        String ChintUnits = response.getString("units");
                        //for the change. Confirm from yogesh
//                        JSONObject theChangeAmount = transResult.getJSONObject("ChangeAmount");
//                        String ChangeAmount = theEnergy.getString("_text");

                        saveToDB(ChintToken, ChintUnits);
                    } else {
                        Toast.makeText(EstatePurchase.this, "Kindly check your connection and click the button again", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }
                } catch (JSONException e) {
                    Toast.makeText(EstatePurchase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(EstatePurchase.this, "Could not connect: Please click the button again" , Toast.LENGTH_LONG).show();
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
            int socketTimeout = 100000;  // 30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            vendTokenRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(EstatePurchase.this, "An Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    private void genHexingToken(String meRef) {
        //String HexingAmount = decimalFormat.format(PaidAmount);
        String theMeterID = theMeterNum;
        //electricity
        int service = 0;
        // vat
        double theVat = Double.parseDouble(vat);
        double amount = Double.parseDouble(PurchaseAmount);
        meterTariff = validSharedPref.getString("tariff", null);
        String token = utilityPref.getString("token", null);
        //the real figure is meant to be the one that i get after i divide the amount by the tariff and send to the system
        double tar = Double.parseDouble(meterTariff);
        String vendURL = baseUrl+"/g/hexing/vend";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject vendingObjects = new JSONObject();
            vendingObjects.put("meterID", theMeterID);
            vendingObjects.put("transactionId", meRef);
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
                        Log.d("Token", tokenData);
                        Log.d("Units", tokenUnit);
                        saveToDB(tokenData, tokenUnit);
                    }else {
                        Toast.makeText(EstatePurchase.this, "Could not generate Token. Contact Admin", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }
                } catch (JSONException e) {
                    Toast.makeText(EstatePurchase.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(EstatePurchase.this, "Could not connect to vending application", Toast.LENGTH_SHORT).show();
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
            int socketTimeout = 30000;  // 30 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            vendTokenRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(vendTokenRequest);
        } catch (Exception e) {
            Toast.makeText(EstatePurchase.this, "An Error occurred" + e.getMessage(), Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
    }

    private void saveToDB(String tokenNum, String tokenUnit){
        String token = utilityPref.getString("token", "");
        String role = utilityPref.getString("role", "");
        int estateID = utilityPref.getInt("estateID", 0);
        Log.d("Estate ID", String.valueOf(estateID));

        double Amount = Double.parseDouble(PurchaseAmount) - (Double.parseDouble(PurchaseAmount) * Double.parseDouble(vat)/100);
        //double RealAmount = Double.parseDouble(realAmount);
        double realTariff = Double.parseDouble(meterTariff);
        double realUnits = Double.parseDouble(tokenUnit);
        //the charges from backend
        double realCharge = 0.0;
        //to get the chargeAmount we are taking
        double theRealChargeAmount = 0.0;
        String vendURL = baseUrl+"/g/saveTrans";
        try {
            //i need to pass in the, token, amount, meterNo, tariff, time
            JSONObject databaseObjects = new JSONObject();
            databaseObjects.put("transID",meRef);
            databaseObjects.put("meterID", theMeterNum);
            databaseObjects.put("estateID", estateID);
            databaseObjects.put("amount", PurchaseAmount);      //original amount sent
            databaseObjects.put("tariff",  realTariff);
            databaseObjects.put("vat",  vat);
            databaseObjects.put("token", tokenNum);
            databaseObjects.put("chargePer", realCharge);       //this is meant to be the percentage of the money
            databaseObjects.put("chargeAmount", theRealChargeAmount);            //this is meant to be the amount of the money
            databaseObjects.put("vendedAmount", Amount);        //the amount vended
            databaseObjects.put("units", realUnits);
            databaseObjects.put("channel", "Internal");
            databaseObjects.put("vendedBy", role);
            databaseObjects.put("email", theMeterEmail);
            databaseObjects.put("username", theMeterName);
            databaseObjects.put("day", getCurrentDate());
            databaseObjects.put("time", getCurrentTime());

            // log all the data sent
            Log.d("TransID", meRef);
            Log.d("MeterID", theMeterNum);
            Log.d("EstateID", String.valueOf(estateID));
            Log.d("Amount", PurchaseAmount);
            Log.d("Tariff", String.valueOf(realTariff));
            Log.d("Vat", vat);
            Log.d("Token", tokenNum);
            Log.d("ChargePer", String.valueOf(realCharge));
            Log.d("ChargeAmount", String.valueOf(theRealChargeAmount));
            Log.d("VendedAmount", String.valueOf(Amount));
            Log.d("Units", String.valueOf(realUnits));
            Log.d("Channel", "Internal");
            Log.d("VendedBy", role);
            Log.d("Email", theMeterEmail);

            JsonObjectRequest saveDatabaseRequest = new JsonObjectRequest(Request.Method.POST, vendURL, databaseObjects, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        //JSONObject data = response.getJSONObject("data");
                        String theDated = response.getString("day");
                        String theTime = response.getString("time");
                        Log.d("Date", theDated);
                        Log.d("Time", theTime);

                        // pass data to next page, to avoid constant going to database
                        Intent nextInt = new Intent(EstatePurchase.this, TheReceipt.class);
                        //ref, meterNo, token, amount, tariff, unit, date
                        nextInt.putExtra("repTransID", meRef);
                        nextInt.putExtra("repMeterID", theMeterNum);
                        nextInt.putExtra("repToken", tokenNum);
                        //this is the original amount that has not been touched, in case im ask not to deduct from client
                        nextInt.putExtra("repAmount", PurchaseAmount);
                        nextInt.putExtra("repCharge", String.valueOf(theRealChargeAmount));
                        nextInt.putExtra("repVat", vat);
                        nextInt.putExtra("repTariff", String.valueOf(realTariff));
                        nextInt.putExtra("redVendedAmount", String.valueOf(Amount));
                        nextInt.putExtra("repUnits", String.valueOf(realUnits));
                        nextInt.putExtra("repDate", theDated);
                        nextInt.putExtra("repTime", theTime);
                        startActivity(nextInt);

                        finish();
                        preloaderLogo.dismiss();
                        Toast.makeText(EstatePurchase.this, "Successful!!! ", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(EstatePurchase.this, "Could not save to database", Toast.LENGTH_LONG).show();
                        preloaderLogo.dismiss();
                    }

                } catch (JSONException e) {
                    Toast.makeText(EstatePurchase.this,"Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    preloaderLogo.dismiss();
                }
            }, error -> {
                Toast.makeText(EstatePurchase.this, "Could not save to database: " + error.getMessage(), Toast.LENGTH_LONG).show();
                preloaderLogo.dismiss();
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
            Toast.makeText(EstatePurchase.this, "An Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            preloaderLogo.dismiss();
        }
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