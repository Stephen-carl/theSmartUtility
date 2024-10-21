package com.cwg.thesmartutility.user;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.UserTransAdapter;
import com.cwg.thesmartutility.PaystackPayment;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.UserTransModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class UserDashboard extends AppCompatActivity {

    RelativeLayout historyRelative, copyRelative;
    LinearLayout dashHistoryLinear;
    RecyclerView historyRecycler;
    TextInputEditText tokenAmountInput;
    TextInputLayout tokenInputLayout;
    String currentDate, TokenAmountText;
    int year;
    Button purchaseButton;
    TextView meterText;
    SharedPreferences validSharedPref;
    BottomNavigationView bottomNavigationView;
    ArrayList<UserTransModel> userList;
    Calendar calendar;
    private final int minMoneyLength = 4;
    private final int maxAmount = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_dashboard);

        // ids
        tokenAmountInput = findViewById(R.id.tokenInput);
        tokenInputLayout = findViewById(R.id.tokenInputLayout);
        purchaseButton = findViewById(R.id.tokenButton);
        historyRelative = findViewById(R.id.tokenHistory);
        historyRecycler = findViewById(R.id.tokenRecycler);
        meterText = findViewById(R.id.meterNumberText);
        dashHistoryLinear = findViewById(R.id.dashHistoryLinear);
        copyRelative = findViewById(R.id.copyRelative);

        userList = new ArrayList<>();
        historyRecycler.setLayoutManager(new LinearLayoutManager(UserDashboard.this));

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        //Get the year, month and the month digit to get the total for that month
        calendar = Calendar.getInstance();
        currentDate = new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.getTime());
        //set the date text to the current month and the total
//        userDate.setText(currentDate + " -");
//        userTotalAmount.setText(theAm);
        year = calendar.get(Calendar.YEAR);

        // load the meter number
        String meter = validSharedPref.getString("meterID", null);
        meterText.setText(meter);

        // copy text
        copyRelative.setOnClickListener(v -> {
            // Copy the text to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", meter);
            clipboard.setPrimaryClip(clip);

            // Show a confirmation message
            Toast.makeText(UserDashboard.this, "Copied " + meter +" to clipboard", Toast.LENGTH_SHORT).show();
        });

        // load the recycler view
        fetchUserTransactions(meter);

        // go to history page
        historyRelative.setOnClickListener(v -> startActivity(new Intent(this, History.class)));

        // button to payStack
        purchaseButton.setOnClickListener(v -> {
            // check if empty
            TokenAmountText = Objects.requireNonNull(tokenAmountInput.getText()).toString().trim();
            int thePrice = Integer.parseInt(TokenAmountText);
            if (TokenAmountText.isEmpty()){
                tokenInputLayout.setErrorEnabled(true);
                tokenInputLayout.setError("Cannot be empty");
            } else if(TokenAmountText.length() < minMoneyLength ){
                tokenInputLayout.setErrorEnabled(true);
                tokenInputLayout.setError("Amount must be more than ₦1000");

            } else if(thePrice > maxAmount) {
                tokenInputLayout.setErrorEnabled(true);
                tokenInputLayout.setError("Max Amount is ₦100,000");

            } else {
                String vendStatus =  validSharedPref.getString("vendStatus", null);
                assert vendStatus != null;
                runChecks(vendStatus);
            }
        });

        // bottom Nav
        bottomNavigationView = findViewById(R.id.userDashNav);
        bottomNavigationView.setSelectedItemId(R.id.homeIcon);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homeIcon){
                return true;
            } else if (itemID == R.id.historyIcon) {
                startActivity(new Intent(this, History.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemID == R.id.profileIcon) {
                startActivity(new Intent(this, UserProfile.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    // limited to six
    public void fetchUserTransactions(String meterID) {
        String transRequest = "http://192.168.246.60:5050/user/translimit/" + meterID;
        // get the token
        String token =  validSharedPref.getString("token", null);
        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        dashHistoryLinear.setVisibility(View.GONE);
                        JSONArray jsonArray = response.getJSONArray("data");
                        if (jsonArray.length() > 0) {
                            //loop through to get the data i need from the array
                            for (int i = 0; i < jsonArray.length(); i++){
                                //get the first item in the array
                                JSONObject dataObject = jsonArray.getJSONObject(i);
                                String transID = dataObject.getString("transRef");
                                String meterId = dataObject.getString("meterID");
                                int estateID = dataObject.getInt("estateID");
                                String amount = dataObject.getString("amount");
                                String tariff = dataObject.getString("tariff");
                                String tokenUnit = dataObject.getString("token");
                                String chargePer = dataObject.getString("chargePer");
                                String chargeAmount = dataObject.getString("chargeAmount");
                                String vendedAmount = dataObject.getString("vendedAmount");
                                String units = dataObject.getString("units");
                                String vendedBy = dataObject.getString("vendedBy");
                                String email = dataObject.getString("email");
                                String date = dataObject.getString("date");
                                String time = dataObject.getString("time");

                                // add the result to the model
                                UserTransModel userTransModel = new UserTransModel(transID, meterId, amount, tariff, tokenUnit, chargePer, chargeAmount, vendedAmount, units, vendedBy, email, date, time, estateID);
                                userList.add(userTransModel);
                            }
                        } else {
                            dashHistoryLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No transactions yet.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        dashHistoryLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Meter Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // the adapter
                UserTransAdapter userTransAdapter = new UserTransAdapter(UserDashboard.this, userList);
                //add to recycler
                historyRecycler.setAdapter(userTransAdapter);
            }, error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) // this will be used mainly in the screens that are calling protected api
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(fetchRequest);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void runChecks(String vendStatus) {
        String meRef = generateUniqueTransactionReference();
        // gatewayStatus is either true or false, vendStatus is either enabled or disabled
        if (vendStatus.equals("Enabled")) {
            // initiate link
            initiate(meRef);
        } else {
            Toast.makeText(this, "Unable to purchase token, kindly contact Admin.", Toast.LENGTH_LONG).show();
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

    // get link
    public void initiate(String meRef) {
        String theMeterNum = validSharedPref.getString("meterID", null);
        String theMeterEmail = validSharedPref.getString("email", null);
        int meterEstateID = validSharedPref.getInt("estateID", 0);
        //get amount in int
        int Amount = Integer.parseInt(TokenAmountText);
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

                    if (checkoutUrl.isEmpty()) {
                        Toast.makeText(UserDashboard.this, "No payment link", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(UserDashboard.this, PaystackPayment.class);
                        intent.putExtra("link", checkoutUrl);
                        intent.putExtra("transRef", refID);
                        intent.putExtra("estateID", meterEstateID);
                        intent.putExtra("purAmount", Amount);

                        startActivity(intent);
                    }


                } catch (JSONException e) {
                    Toast.makeText(UserDashboard.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    //throw new RuntimeException(e);
                }
            }, error -> Toast.makeText(UserDashboard.this, "Please Try Again", Toast.LENGTH_LONG).show());

            VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}