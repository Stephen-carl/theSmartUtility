package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.cwg.thesmartutility.Adapter.EstateMeterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.model.EstateMeterModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstateMeters extends AppCompatActivity {
    SharedPreferences utilityPref;
    ArrayList<EstateMeterModel> userList;
    EstateMeterAdapter meterNumAdapter;
    private boolean isBackPressed = false;
    RecyclerView meterRecycler;
    LinearLayout dashMeterLinear;
    BottomNavigationView bottomNavigationView;
    TextInputEditText meterInput;
    String MeterInput, token, baseUrl;
    ImageView backImage;
    int estateId;
    private int page = 1;
    private final int pageSize = 70;
    private PreloaderLogo preloaderLogo;
    //int page = 1;
    //int pageSize = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_meters);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        meterRecycler = findViewById(R.id.estateMeterRecycler);
        dashMeterLinear = findViewById(R.id.estateMeterEmptyLinear);
        meterInput = findViewById(R.id.estateMeterInput);
        backImage = findViewById(R.id.backImage);
        preloaderLogo = new PreloaderLogo(this);

        // sharedPref
        utilityPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = utilityPref.getString("token", "");
        estateId = utilityPref.getInt("estateID", 0);

        // initialize the arrayList
        userList = new ArrayList<>();
        meterRecycler.setLayoutManager(new LinearLayoutManager(EstateMeters.this));
        meterNumAdapter = new EstateMeterAdapter(EstateMeters.this, userList);
        fetchMeters(estateId);

        // back button
        backImage.setOnClickListener(v -> {
            startActivity(new Intent(this, EstateDashboard.class));
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // add text watcher to the edittext and go to filtered function
        meterInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //fetchFilteredMeters(estateId,s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchFilteredMeters(estateId,s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //fetchFilteredMeters(estateId,s.toString());
            }
        });

        // bottom Nav
        bottomNavigationView = findViewById(R.id.estateMeterNav);
        bottomNavigationView.setSelectedItemId(R.id.estateMeterMenu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.estateHomeMenu){
                startActivity(new Intent(this, EstateDashboard.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateHistoryMenu) {
                startActivity(new Intent(this, EstateHistory.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateMeterMenu) {
                return true;
            } else if (itemID == R.id.estateProfileMenu) {
                startActivity(new Intent(this, EstateProfile.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });

        // on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the login activity
                if (isBackPressed) {
                    // Navigate to the login activity only on second press
                    Intent intent = new Intent(EstateMeters.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                    startActivity(intent);
                    finish();
                } else {
                    // Set flag and show Toast on first press
                    isBackPressed = true;
                    Toast.makeText(EstateMeters.this, "Press again to log out.", Toast.LENGTH_SHORT).show();

                    // Reset the flag after 2 seconds
                    new Handler().postDelayed(() -> isBackPressed = false, 2000);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }

    private void fetchMeters(int estateId) {
        preloaderLogo.show();
        //String apiUrl = "http://192.168.61.64:5050/estate/meters/"+estateId+ "?page="  + page +"&pageSize=" + pageSize;
        String apiUrl = baseUrl+"/estate/meters/"+estateId+ "?page="  + page +"&pageSize=" + pageSize;
        try {
            JsonObjectRequest meterRequest = new JsonObjectRequest(Request.Method.GET, apiUrl, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        preloaderLogo.dismiss();
                        // clear list
                        //userList.clear();
                        dashMeterLinear.setVisibility(View.GONE);
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
                            Toast.makeText(EstateMeters.this, "No meters in this estate.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        preloaderLogo.dismiss();
                        dashMeterLinear.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(EstateMeters.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                }
                //set the adapter
                meterNumAdapter.notifyDataSetChanged();
                //set the recycler to the adapter
                meterRecycler.setAdapter(meterNumAdapter);

                },
                error -> {
                    preloaderLogo.dismiss();
                    Log.e("EstateHistory", "Error: " + error.toString());
                    Toast.makeText(EstateMeters.this, "Error:" + error, Toast.LENGTH_SHORT).show();
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
            preloaderLogo.dismiss();
            Toast.makeText(EstateMeters.this, "Pls connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    // filtered meters
    private void fetchFilteredMeters(int estateId, String meter) {
        preloaderLogo.show();
        //String apiUrl = "http://192.168.61.64:5050/estate/estatemeters?page=" + page +"&pageSize=" + pageSize;
        String apiUrl = baseUrl+"/estate/estatemeters?page=" + page +"&pageSize=" + pageSize;
        try {
            JSONObject requestObject = new JSONObject();
            requestObject.put("estateID", estateId);
            requestObject.put("meterID", meter);
            JsonObjectRequest meterRequest = new JsonObjectRequest(Request.Method.POST, apiUrl, requestObject, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        preloaderLogo.dismiss();
                        // clear list
                        userList.clear();
                        meterRecycler.setVisibility(View.VISIBLE);
                        dashMeterLinear.setVisibility(View.GONE);
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
                                String blockNo = jsonObject.getString("blockNo");
                                String flatNo = jsonObject.getString("flatNo");

                                //add to the model and list
                                EstateMeterModel meterNumModel = new EstateMeterModel(meterID, customerID, tariff, brand, vendStatus, email, username,estateID);
                                userList.add(meterNumModel);
                            }
                        } else {
                            meterRecycler.setVisibility(View.GONE);
                            dashMeterLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(EstateMeters.this, "No meters found.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        preloaderLogo.dismiss();
                        dashMeterLinear.setVisibility(View.VISIBLE);
                    }

                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(EstateMeters.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                }
                //set the adapter
                //meterNumAdapter = new EstateMeterAdapter(EstateMeters.this, userList);
                //set the recycler to the adapter
                meterNumAdapter.notifyDataSetChanged();
                meterRecycler.setAdapter(meterNumAdapter);
            },
            error -> {
                preloaderLogo.dismiss();
                Log.e("EstateHistory", "Error: " + error.toString());
                //Toast.makeText(EstateMeters.this, "Error:" + error, Toast.LENGTH_LONG).show();
                Toast.makeText(this, "No meters found.", Toast.LENGTH_SHORT).show();
                meterRecycler.setVisibility(View.GONE);
                dashMeterLinear.setVisibility(View.VISIBLE);
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
            preloaderLogo.dismiss();
            Toast.makeText(EstateMeters.this, "Pls connect to internet", Toast.LENGTH_SHORT).show();
        }
    }
}