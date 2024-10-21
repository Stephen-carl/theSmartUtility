package com.cwg.thesmartutility.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.EstateTransAdapter;
import com.cwg.thesmartutility.Adapter.UserTransAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.model.UserTransModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class History extends AppCompatActivity {
    BottomNavigationView userHistoryDash;
    LinearLayout historyLinear;
    RecyclerView historyRecycle;
    Button goHomeButton;
    SharedPreferences validSharedPref;
    ArrayList<UserTransModel> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0,0);
            return insets;
        });

        //ids
        goHomeButton = findViewById(R.id.purchaseHomeButton);
        historyLinear = findViewById(R.id.emptyLinear);
        historyRecycle = findViewById(R.id.historyRecycler);

        userList = new ArrayList<>();
        historyRecycle.setLayoutManager(new LinearLayoutManager(History.this));

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // i need to see a way i can share this file with the admin
        String meter = validSharedPref.getString("meterID", null);
        String role = validSharedPref.getString("role", null);
        int estateID = validSharedPref.getInt("estateID", 0);
        // fetch the data
        assert role != null;
        if (role.equals("user")){
            fetchUserTransactions(meter);
            // go home
            goHomeButton.setOnClickListener(v -> startActivity(new Intent(this, UserDashboard.class)));
        } else if (role.equals("admin")) {
            fetchEstateTrans(estateID);
            // go to the purchase token page
//            goHomeButton.setOnClickListener(v -> {
//                startActivity(new Intent(this, UserDashboard.class));
//            });
        } // last one will be for super admin


        userHistoryDash = findViewById(R.id.userHistoryNav);
        userHistoryDash.setSelectedItemId(R.id.historyIcon);

        userHistoryDash.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homeIcon){
                if (role.equals("user")){
                    startActivity(new Intent(this, UserDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if (role.equals("admin")) {
                    startActivity(new Intent(this, EstateDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                return true;
            } else if (itemID == R.id.historyIcon) {
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

    public void fetchUserTransactions(String meterID) {
        String transRequest = "http://192.168.246.60:5050/user/transaction/" + meterID;
        // get the token
        String token =  validSharedPref.getString("token", null);
        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        historyLinear.setVisibility(View.GONE);
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
                            historyLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No transactions yet.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        historyLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Meter Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // the adapter
                UserTransAdapter userTransAdapter = new UserTransAdapter(History.this, userList);
                //add to recycler
                historyRecycle.setAdapter(userTransAdapter);
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

    public void fetchEstateTrans(int estateId) {
        String transRequest = "http://192.168.246.60:5050/estate/transactions/" + estateId;
        // get the token
        String token =  validSharedPref.getString("token", null);
        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        historyLinear.setVisibility(View.GONE);
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
                            historyLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No transactions yet.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        historyLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Estate Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // the adapter
                EstateTransAdapter estateTransAdapter = new EstateTransAdapter(History.this, userList);
                //add to recycler
                historyRecycle.setAdapter(estateTransAdapter);
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
}