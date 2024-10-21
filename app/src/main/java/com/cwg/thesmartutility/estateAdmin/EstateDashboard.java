package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.cwg.thesmartutility.Adapter.EstateMeterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.EstateMeterModel;
import com.cwg.thesmartutility.user.History;
import com.cwg.thesmartutility.user.UserProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EstateDashboard extends AppCompatActivity {

    RelativeLayout historyRelative;
    LinearLayout dashHistoryLinear;
    RecyclerView meterRecycler;
    TextInputEditText tariffAmountInput;
    TextInputLayout tariffLayout;
    String TariffAmountText;
    Button updateButton;
    TextView estateName, tariffText;
    SharedPreferences validSharedPref;
    BottomNavigationView bottomNavigationView;
    ArrayList<EstateMeterModel> userList;
    EstateMeterAdapter meterNumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_dashboard);

        // ids
        estateName = findViewById(R.id.estateNameText);
        tariffLayout = findViewById(R.id.estateUpdateLayout);
        tariffAmountInput = findViewById(R.id.estateUpdateInput);
        tariffText = findViewById(R.id.tariffText);
        updateButton = findViewById(R.id.estateUpdateButton);
        historyRelative = findViewById(R.id.estateTokenHistory);
        dashHistoryLinear = findViewById(R.id.estateHistoryLinear);
        meterRecycler = findViewById(R.id.estateMeterRecycler);
        bottomNavigationView = findViewById(R.id.estateDashNav);

        // initialize the arrayList
        userList = new ArrayList<>();
        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        meterRecycler.setLayoutManager(new LinearLayoutManager(EstateDashboard.this));

        // set estate name and current tariff
        String theEstateName = validSharedPref.getString("estateName", null);
        String currentTariff = validSharedPref.getString("tariff", null);
        estateName.setText(theEstateName);
        tariffText.setText("₦" + currentTariff);

        // fetch the meters
        int EstateID = validSharedPref.getInt("estateID", 0);
        fetchMeters(EstateID);

        // update Button
        updateButton.setOnClickListener(v -> init());
        // go to history
        historyRelative.setOnClickListener(v -> startActivity(new Intent(this, History.class)));

        // bottom Nav
        bottomNavigationView = findViewById(R.id.estateDashNav);
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

    public void init() {
        TariffAmountText = Objects.requireNonNull(tariffAmountInput.getText()).toString().trim();
        //check if empty
        if (TariffAmountText.isEmpty()) {
            tariffLayout.setErrorEnabled(true);
            tariffLayout.setError("Please enter the new tariff.");
        } else {
            int EstateID = validSharedPref.getInt("estateID", 0);
            //pass in the estateID and the Updated Tariff text
            updateTheTariff(EstateID, TariffAmountText);
        }
    }

    // update Tariff
    private void updateTheTariff(int estateID, String newTariff) {
        String apiUrl = "http://192.168.246.60:5050/estate/updateTariff";
        String token =  validSharedPref.getString("token", null);
        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("estateID", estateID);
                jsonRequest.put("tariff", newTariff);
            } catch (JSONException e) {
                Toast.makeText(EstateDashboard.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    apiUrl,
                    jsonRequest,
                    response -> {
                        try {
                            tariffAmountInput.setText("");
                            String message = response.getString("message");
                            if (message.equals("success")) {
                                String theTariff = response.getString("tariff");
                                tariffText.setText("₦" + theTariff);
                            } else {
                                Toast.makeText(this, "Could not update tariff", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(EstateDashboard.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("EstateHistory", "Error: " + error.toString());
                        Toast.makeText(EstateDashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle error
                    }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Add the request to the RequestQueue
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        }catch ( Exception e) {
            Toast.makeText(EstateDashboard.this, "Pls connect to internet", Toast.LENGTH_SHORT).show();

        }
    }

    private void fetchMeters(int estateId) {
        String apiUrl = "http://192.168.246.60:5050/estate/meters/"+estateId;
        String token =  validSharedPref.getString("token", null);
        try {
            JsonObjectRequest meterRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    apiUrl,
                    null,
                    response -> {
                        try {
                            String message = response.getString("message");
                            if (message.equals("success")){
                                dashHistoryLinear.setVisibility(View.GONE);
                                JSONArray jsonArray = response.getJSONArray("data");
                                if (jsonArray.length() > 0) {
                                    //loop through to get the data i need from the array
                                    for (int i = 0; i < jsonArray.length(); i++){
                                        //get the first item in the array
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        //get string or int with the name that corresponds with the one in the database
                                        String meterID = jsonObject.getString("meterID");
                                        int estateID = jsonObject.getInt("estateID");
                                        String customerID = jsonObject.getString("customerID");
                                        String tariff = jsonObject.getString("tariff");
                                        String brand = jsonObject.getString("brand");
                                        String vendStatus = jsonObject.getString("vendStatus");
                                        String email = jsonObject.getString("email");
                                        String username = jsonObject.getString("userName");

                                        //add to the model and list
                                        EstateMeterModel meterNumModel = new EstateMeterModel(meterID, customerID, tariff, brand, vendStatus, email, username,estateID);
                                        userList.add(meterNumModel);
                                    }
                                } else {
                                    Toast.makeText(EstateDashboard.this, "No meters in this estate.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                dashHistoryLinear.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(EstateDashboard.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                        //set the adapter
                        meterNumAdapter = new EstateMeterAdapter(EstateDashboard.this, userList);
                        //set the recycler to the adapter
                        meterRecycler.setAdapter(meterNumAdapter);
                    },
                    error -> {
                        Log.e("EstateHistory", "Error: " + error.toString());
                        Toast.makeText(EstateDashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle error
                    }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Add the request to the RequestQueue
            VolleySingleton.getInstance(this).addToRequestQueue(meterRequest);
        }catch ( Exception e) {
            Toast.makeText(EstateDashboard.this, "Pls connect to internet", Toast.LENGTH_SHORT).show();

        }
    }
}