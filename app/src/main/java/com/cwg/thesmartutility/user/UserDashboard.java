package com.cwg.thesmartutility.user;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.UserTransAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.model.UserTransModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserDashboard extends AppCompatActivity {

    RelativeLayout historyRelative, copyRelative, purchaseButton, serviceRelative;
    LinearLayout dashHistoryLinear;
    RecyclerView historyRecycler;
    TextView meterText, usernameText, totalAmountText, amountYear;
    SharedPreferences validSharedPref;
    BottomNavigationView bottomNavigationView;
    ArrayList<UserTransModel> userList;
    private boolean isBackPressed = false;
    int page = 1;
    int pageSize = 10;
    String baseUrl;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        purchaseButton = findViewById(R.id.tokenButton);
        historyRelative = findViewById(R.id.tokenHistory);
        historyRecycler = findViewById(R.id.tokenRecycler);
        meterText = findViewById(R.id.meterNumberText);
        dashHistoryLinear = findViewById(R.id.dashHistoryLinear);
        copyRelative = findViewById(R.id.copyRelative);
        usernameText = findViewById(R.id.userDashName);
        totalAmountText = findViewById(R.id.userDashAmount);
        amountYear = findViewById(R.id.userDashAmountYear);
        serviceRelative = findViewById(R.id.serviceRelative);
        preloaderLogo = new PreloaderLogo(this);

        // get the current and assign to amountYear
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        amountYear.setText(currentYear);

        userList = new ArrayList<>();
        historyRecycler.setLayoutManager(new LinearLayoutManager(UserDashboard.this));

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        String brand = validSharedPref.getString("brand", null);
        assert brand != null;
        Log.d("Brand: ", brand);

        // load the meter number
        String meter = validSharedPref.getString("meterID", null);
        String username = validSharedPref.getString("username", null);
        String serviceStatus = validSharedPref.getString("service_status", "");
        meterText.setText(meter);
        usernameText.setText(username);

        // display the relative layout and send user to service page if service charge is true
        if (serviceStatus.equals("true")) {
            serviceRelative.setVisibility(View.VISIBLE);
        } else {
            serviceRelative.setVisibility(View.GONE);
        }
        serviceRelative.setOnClickListener(v -> startActivity(new Intent(this, UserService.class)));

        // settings the padding for bottom
        dashHistoryLinear.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            dashHistoryLinear.setPadding(0, 0, 0, bottomInset);
            return insets.consumeSystemWindowInsets();
        });

        historyRecycler.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            historyRecycler.setPadding(0, 0, 0, bottomInset);
            return insets.consumeSystemWindowInsets();
        });

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
            // GO TO USER PURCHASE ACTIVITY
            startActivity(new Intent(this, UserPurchase.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
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

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the login activity
                if (isBackPressed) {
                    // Navigate to the login activity only on second press
                    Intent intent = new Intent(UserDashboard.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                    startActivity(intent);
                    finish();
                } else {
                    // Set flag and show Toast on first press
                    isBackPressed = true;
                    Toast.makeText(UserDashboard.this, "Please press again to log out.", Toast.LENGTH_SHORT).show();

                    // Reset the flag after 2 seconds
                    new Handler().postDelayed(() -> isBackPressed = false, 2000);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }

    public void fetchUserTransactions(String meterID) {
        preloaderLogo.show();
        String transRequest = baseUrl+"/user/transactions?page="  + page +"&pageSize=" + pageSize;
        // get the token
        String token =  validSharedPref.getString("token", "");
        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");

                    if (message.equals("success")) {
                        preloaderLogo.dismiss();
                        // making sure the list is empty first
                        userList.clear();
                        dashHistoryLinear.setVisibility(View.GONE);
                        historyRecycler.setVisibility(View.VISIBLE);
                        String totalAmount = response.getString("totalAmount");
                        // set the amount text view to this amount
                        totalAmountText.setText(totalAmount);
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
                            preloaderLogo.dismiss();
                            historyRecycler.setVisibility(View.GONE);
                            dashHistoryLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No transactions yet.", Toast.LENGTH_LONG).show();
                        }
                        // get brand
                        getBrand();

                    }
                    else {
                        preloaderLogo.dismiss();
                        historyRecycler.setVisibility(View.GONE);
                        dashHistoryLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Meter Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // the adapter
                UserTransAdapter userTransAdapter = new UserTransAdapter(UserDashboard.this, userList);
                //add to recycler
                historyRecycler.setAdapter(userTransAdapter);
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }) // this will be used mainly in the screens that are calling protected api
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
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // get brand
    private void getBrand() {
        String apiURL = baseUrl+"/user/getBrand";
        String token =  validSharedPref.getString("token", "");
        JsonObjectRequest brandRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")) {
                    JSONObject brandData = response.getJSONObject("brand");
                    String brand = brandData.getString("brand");
                    String customerID = brandData.getString("customerID");
                    // save the brand to Pref
                    SharedPreferences.Editor editor = validSharedPref.edit();
                    editor.putString("brand", brand);
                    editor.putString("customerID", customerID);
                    editor.apply();
                } else {
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(brandRequest);
    }

}