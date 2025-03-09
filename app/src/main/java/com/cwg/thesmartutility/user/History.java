package com.cwg.thesmartutility.user;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.cwg.thesmartutility.Adapter.UserTransAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.model.UserTransModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class History extends AppCompatActivity {
    BottomNavigationView userHistoryDash;
    LinearLayout historyLinear;
    RecyclerView historyRecycle;
    Button goHomeButton;
    SharedPreferences validSharedPref;
    ArrayList<UserTransModel> userList;
    private int CURRENT_PAGE = 1;
    private final int PAGE_SIZE = 10;
    TextView nextTextButton, previousButton, resetAll, pagesText;
    private boolean isFiltered = false;
    String StartDate, EndDate, meter, token, Email, baseUrl;
    ImageView filterRelative, historyBack;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    UserTransModel userTransModel;
    TextInputEditText startInput, endInput;
    Button applyButton;
    UserTransAdapter userTransAdapter;

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

        baseUrl = this.getString(R.string.managementBaseURL);

        //ids
        goHomeButton = findViewById(R.id.purchaseHomeButton);
        historyLinear = findViewById(R.id.emptyLinear);
        historyRecycle = findViewById(R.id.historyRecycler);
        filterRelative = findViewById(R.id.filterRelative);
        nextTextButton = findViewById(R.id.nextMeterText);
        previousButton = findViewById(R.id.previousMeterText);
        historyBack = findViewById(R.id.userHistoryBack);
        pagesText = findViewById(R.id.pageText);

        historyBack.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboard.class));
            finish();
        });

        // set the padding
        historyRecycle.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            historyRecycle.setPadding(0, 0, 0, bottomInset);
            return insets.consumeSystemWindowInsets();
        });

        userList = new ArrayList<>();
        historyRecycle.setLayoutManager(new LinearLayoutManager(History.this));
        userTransAdapter = new UserTransAdapter(History.this, userList);
        //add to recycler
        historyRecycle.setAdapter(userTransAdapter);

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // i need to see a way i can share this file with the admin
        meter = validSharedPref.getString("meterID", "");
        String role = validSharedPref.getString("role", "");
        int estateID = validSharedPref.getInt("estateID", 0);
        // get the token
        token =  validSharedPref.getString("token", null);
        // add email to request body of filter on the backend and mobile
        Email = validSharedPref.getString("email", "");



        // fetch the data
        if (role.equals("user")){
            fetchUserTransactions(meter, CURRENT_PAGE);
            // go home
            goHomeButton.setOnClickListener(v -> startActivity(new Intent(this, UserDashboard.class)));
        } else {
            Toast.makeText(this, "Not Permitted", Toast.LENGTH_SHORT).show();
        }

        // call the filter bottom sheet
        filterRelative.setOnClickListener(v -> showFilterBottomSheet());

        // next text button
        nextTextButton.setOnClickListener(v -> {
            if (isFiltered) {
                CURRENT_PAGE++;
                fetchUserFilteredTransactions(StartDate, EndDate, meter, CURRENT_PAGE, Email);
            } else {
                CURRENT_PAGE++;
                fetchUserTransactions(meter, CURRENT_PAGE);
            }
        });

        // previous text button
        previousButton.setOnClickListener(v -> {
            if (CURRENT_PAGE > 1) {
                if (isFiltered){
                    CURRENT_PAGE --;
                    fetchUserFilteredTransactions(StartDate, EndDate, meter, CURRENT_PAGE, Email);
                } else {
                    CURRENT_PAGE--;
                    fetchUserTransactions(meter, CURRENT_PAGE);
                }
            }
        });

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

    public void fetchUserTransactions(String meterID, int page) {
        isFiltered = false;
        String transRequest = baseUrl+"/user/transactions?page=" + page + "&pageSize=" + PAGE_SIZE;

        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1)/PAGE_SIZE;
                        if (totalPages == CURRENT_PAGE) {
                            // make next button invisible
                            nextTextButton.setVisibility(View.INVISIBLE);
                        } else {
                            nextTextButton.setVisibility(View.VISIBLE);
                        }
                        historyLinear.setVisibility(View.GONE);
                        historyRecycle.setVisibility(View.VISIBLE);
                        JSONArray jsonArray = response.getJSONArray("data");
                        if (jsonArray.length() > 0) {
                            userList.clear();
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

                                // set the pages text
                                pagesText.setText(CURRENT_PAGE + " of " + totalPages+" Pages");

                                // add the result to the model
                                userTransModel = new UserTransModel(transID, meterId, amount, tariff, tokenUnit, chargePer, chargeAmount, vendedAmount, units, vendedBy, email, date, time, estateID);
                                userList.add(userTransModel);
                            }
                            // adapter
                            userTransAdapter.notifyDataSetChanged();
                        } else {
                            pagesText.setText("--0--");
                            historyRecycle.setVisibility(View.GONE);
                            historyLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No more transactions found.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        historyRecycle.setVisibility(View.GONE);
                        historyLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Meter Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error : " +  e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                // the adapter


            }, error -> {
                historyRecycle.setVisibility(View.GONE);
                historyLinear.setVisibility(View.VISIBLE);
                Toast.makeText(this, "No more transactions found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showFilterBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetView = getLayoutInflater().inflate(R.layout.user_history_bottom_sheet, findViewById(R.id.userMain), false);

        // ids
        startInput = bottomSheetView.findViewById(R.id.filterMeterStartDate);
        endInput = bottomSheetView.findViewById(R.id.filterMeterEndDate);
        applyButton = bottomSheetView.findViewById(R.id.filterMeterButton);
        resetAll = bottomSheetView.findViewById(R.id.meterResetAll);

        startInput.setFocusable(false);
        endInput.setFocusable(false);
        startInput.setOnClickListener(v -> theDatePicker(startInput));
        endInput.setOnClickListener(v -> theDatePicker(endInput));

        resetAll.setOnClickListener(v -> {
            startInput.setText("");
            endInput.setText("");
        });

        applyButton.setOnClickListener(v -> {
            StartDate = Objects.requireNonNull(startInput.getText()).toString();
            EndDate = Objects.requireNonNull(endInput.getText()).toString();
            CURRENT_PAGE = 1;
            fetchUserFilteredTransactions(StartDate, EndDate, meter, CURRENT_PAGE, Email);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void theDatePicker(TextInputEditText input) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DateDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) ->{
            String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            input.setText(date);
        }, year, month, day);
        datePickerDialog.show();
    }

    public void fetchUserFilteredTransactions(String start, String end,String meterID, int page, String userEmail) {
        isFiltered = true;
        String transRequest = baseUrl+"/user/transactions";

        try {
            JSONObject filterRequest = new JSONObject();
            try {
                filterRequest.put("startDate", start);
                filterRequest.put("endDate", end);
                filterRequest.put("meterID", meterID);
                filterRequest.put("page", page);
                filterRequest.put("pageSize", PAGE_SIZE);
                filterRequest.put("email", userEmail);
            } catch (JSONException e) {
                Toast.makeText(this, "Could not pass body", Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.POST, transRequest, filterRequest, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        historyRecycle.setVisibility(View.VISIBLE);
                        historyLinear.setVisibility(View.GONE);
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1)/PAGE_SIZE;
                        if (totalPages == CURRENT_PAGE) {
                            // make next button invisible
                            nextTextButton.setVisibility(View.INVISIBLE);
                        } else {
                            nextTextButton.setVisibility(View.VISIBLE);
                        }
                        JSONArray jsonArray = response.getJSONArray("data");
                        if (jsonArray.length() > 0) {
                            userList.clear();
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

                                // set the pages text
                                pagesText.setText(CURRENT_PAGE + " of " + totalPages+" Pages");

                                // add the result to the model
                                userTransModel = new UserTransModel(transID, meterId, amount, tariff, tokenUnit, chargePer, chargeAmount, vendedAmount, units, vendedBy, email, date, time, estateID);
                                userList.add(userTransModel);
                            }
                            bottomSheetDialog.dismiss();
                            // adapter
                            userTransAdapter.notifyDataSetChanged();
                        } else {
                            bottomSheetDialog.dismiss();
                            historyRecycle.setVisibility(View.GONE);
                            historyLinear.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "No more transactions found.", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        bottomSheetDialog.dismiss();
                        historyRecycle.setVisibility(View.GONE);
                        historyLinear.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Incorrect Meter Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    bottomSheetDialog.dismiss();
                    Toast.makeText(this, "Error Occurred", Toast.LENGTH_SHORT).show();
                }
                // the adapter


            }, error -> {
                bottomSheetDialog.dismiss();
                historyLinear.setVisibility(View.VISIBLE);
                Toast.makeText(this, "No more transactions found", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}