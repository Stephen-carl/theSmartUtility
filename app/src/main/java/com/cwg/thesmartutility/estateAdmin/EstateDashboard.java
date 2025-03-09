package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.cwg.thesmartutility.Adapter.EstateMeterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.model.EstateMeterModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstateDashboard extends AppCompatActivity {

    RelativeLayout historyRelative;
    LinearLayout dashHistoryLinear;
    RecyclerView meterRecycler;
    RelativeLayout updateVATButton, updateTariffButton, updateServiceButton;
    TextView estateName, tariffText, vatText;
    SharedPreferences validSharedPref;
    BottomNavigationView bottomNavigationView;
    ArrayList<EstateMeterModel> userList;
    EstateMeterAdapter meterNumAdapter;
    private boolean isBackPressed = false;
    int page = 1;
    int pageSize = 10;
    String baseUrl;
    private PreloaderLogo preloaderLogo;
    Button serviceTransButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.estateMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);
        // ids
        estateName = findViewById(R.id.estateNameText);
        tariffText = findViewById(R.id.tariffText);
        updateVATButton = findViewById(R.id.VATButton);
        updateTariffButton = findViewById(R.id.TariffButton);
        updateServiceButton = findViewById(R.id.serviceButton);
        serviceTransButton = findViewById(R.id.serviceTransButton);
        historyRelative = findViewById(R.id.estateTokenHistory);
        dashHistoryLinear = findViewById(R.id.estateHistoryLinear);
        meterRecycler = findViewById(R.id.estateMeterRecycler);
        bottomNavigationView = findViewById(R.id.estateDashNav);
        vatText = findViewById(R.id.vatText);
        preloaderLogo = new PreloaderLogo(this);


        // initialize the arrayList
        userList = new ArrayList<>();
        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        meterRecycler.setLayoutManager(new LinearLayoutManager(EstateDashboard.this));

        // set estate name and current tariff
        String theEstateName = validSharedPref.getString("estateName", "");
        String currentTariff = validSharedPref.getString("tariff", "");
        String vatAmount = validSharedPref.getString("vat", "");
        String serviceStatus = validSharedPref.getString("service_status", "");
        estateName.setText(theEstateName);
        tariffText.setText("₦" + currentTariff);
        vatText.setText(vatAmount + " %");

        // fetch the meters
        int EstateID = validSharedPref.getInt("estateID", 0);
        fetchMeters(EstateID);

        // update Buttons
        updateVATButton.setOnClickListener(v -> startActivity(new Intent(this, UpdateVAT.class)));
        updateTariffButton.setOnClickListener(v -> startActivity(new Intent(this, UpdateTariff.class)));
        // if serviceStatus is "true" then show the button. Else hide it
        if (serviceStatus.equals("true")) {
            updateServiceButton.setVisibility(View.VISIBLE);
        } else {
            updateServiceButton.setVisibility(View.GONE);
        }

        // service transaction history
        serviceTransButton.setOnClickListener(v -> startActivity(new Intent(this, EstateService.class)));

        // update service button
        updateServiceButton.setOnClickListener(v -> startActivity(new Intent(this, UpdateService.class)));



        // TODO: Work on this
        historyRelative.setOnClickListener(v -> startActivity(new Intent(this, EstateMeters.class)));

        // bottom Nav
        bottomNavigationView = findViewById(R.id.estateDashNav);
        bottomNavigationView.setSelectedItemId(R.id.estateHomeMenu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.estateHomeMenu){
                return true;
            } else if (itemID == R.id.estateHistoryMenu) {
                startActivity(new Intent(this, EstateHistory.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateMeterMenu) {
                startActivity(new Intent(this, EstateMeters.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                    Intent intent = new Intent(EstateDashboard.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                    startActivity(intent);
                    finish();
                } else {
                    // Set flag and show Toast on first press
                    isBackPressed = true;
                    Toast.makeText(EstateDashboard.this, "Press again to log out.", Toast.LENGTH_SHORT).show();

                    // Reset the flag after 2 seconds
                    new Handler().postDelayed(() -> isBackPressed = false, 2000);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);

    }

    private void fetchMeters(int estateId) {
        preloaderLogo.show();
        String apiUrl = baseUrl+"/estate/meters/"+estateId+ "?page="  + page +"&pageSize=" + pageSize;
        //String apiUrl = "http://41.78.157.215:4173/estate/meters/"+estateId+ "?page="  + page +"&pageSize=" + pageSize;
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
                                preloaderLogo.dismiss();
                                // clear list
                                userList.clear();
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
                                    meterRecycler.setVisibility(View.GONE);
                                    dashHistoryLinear.setVisibility(View.VISIBLE);
                                    Toast.makeText(EstateDashboard.this, "No meters in this estate.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                preloaderLogo.dismiss();
                                dashHistoryLinear.setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            preloaderLogo.dismiss();
                            Toast.makeText(EstateDashboard.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                        //set the adapter
                        meterNumAdapter = new EstateMeterAdapter(EstateDashboard.this, userList);
                        //set the recycler to the adapter
                        meterRecycler.setAdapter(meterNumAdapter);
                    },
                    error -> {
                        preloaderLogo.dismiss();
                        Log.e("EstateHistory", "Error: " + error.toString());
                        Toast.makeText(EstateDashboard.this, "Error: "  + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(EstateDashboard.this, "Pls connect to internet", Toast.LENGTH_SHORT).show();

        }
    }

}