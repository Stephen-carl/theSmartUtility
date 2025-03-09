package com.cwg.thesmartutility.estateAdmin;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.cwg.thesmartutility.Adapter.EstateTransAdapter;
import com.cwg.thesmartutility.Adapter.FilterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.FilterModel;
import com.cwg.thesmartutility.model.UserTransModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EstateHistory extends AppCompatActivity {

    BottomNavigationView estateHistoryDash;
    LinearLayout historyLinear;
    RecyclerView historyRecycle, meterListRecycler, nameListRecycler;
    Button goHomeButton, applyButton;
    SharedPreferences validSharedPref;
    ArrayList<UserTransModel> userList;
    ArrayList<FilterModel> meterList;
    FilterModel meterListModel;
    FilterAdapter meterListAdapter, nameListAdapter;
    String meter, role, token, baseUrl;
    int estateID;
    ImageView filterRelative, historyBack;
    EstateTransAdapter estateTransAdapter;
    UserTransModel userTransModel;
    BottomSheetDialog bottomSheetDialog, meterNumBottomSheetDialog, meterNameBottomSheetDialog;
    View bottomSheetView, meterNumBottomSheetView, meterNameBottomSheetView;
    TextInputEditText startInput, endInput, customerNameInput, customerMeterInput, meterSearchInput, nameSearchInput;
    String StartDate, EndDate, CustomerName, CustomerMeter;
    TextInputLayout startLayout, endLayout, nameLayout, meterLayout, meterSearchLayout, nameSearchLayout;
    private int CURRENT_PAGE = 1;
    private final int PAGE_SIZE = 10;
    TextView nextTextButton, previousButton, resetAll, pagesText;
    private boolean isFiltered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.filterMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        goHomeButton = findViewById(R.id.estatePurchaseButton);
        historyLinear = findViewById(R.id.estateEmptyLinear);
        historyRecycle = findViewById(R.id.estateHistoryRecycler);
        filterRelative = findViewById(R.id.estateFilterRelative);
        estateHistoryDash = findViewById(R.id.estateHistoryNav);
        nextTextButton = findViewById(R.id.nextText);
        previousButton = findViewById(R.id.previousText);
        pagesText = findViewById(R.id.pagesText);
        historyBack = findViewById(R.id.backImage);

        historyBack.setOnClickListener(v -> {
            startActivity(new Intent(this, EstateDashboard.class));
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);

        // HANDLING THE THE ADAPTER HERE SO THE TWO TRANSACTION API RESPONSE CAN BE PASSED IN
        // userList
        userList  = new ArrayList<>();
        // set the recyclerVie
        historyRecycle.setLayoutManager(new LinearLayoutManager(this));
        estateTransAdapter = new EstateTransAdapter(EstateHistory.this, userList);
        //add to recycler
        historyRecycle.setAdapter(estateTransAdapter);

        // get the meter, role and estateID value
        meter = validSharedPref.getString("meterID", "");
        role = validSharedPref.getString("role", "");
        estateID = validSharedPref.getInt("estateID", 0);
        token =  validSharedPref.getString("token", "");


        // settings the padding for bottom
        historyLinear.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            historyLinear.setPadding(0, 0, 0, bottomInset);
            return insets.consumeSystemWindowInsets();
        });

        historyRecycle.setOnApplyWindowInsetsListener((v, insets) -> {
            int bottomInset = insets.getSystemWindowInsetBottom();
            historyRecycle.setPadding(0, 0, 0, bottomInset);
            return insets.consumeSystemWindowInsets();
        });


        // fetch the transactions
        fetchEstateTrans(estateID, CURRENT_PAGE);
        //fetchFilteredTrans(StartDate, EndDate, CustomerName, CustomerMeter, CURRENT_PAGE);

        // let's work on the filter
        filterRelative.setOnClickListener(v -> {
            // call the bottom sheet
            bottomSheetFilter();
        });

        // next text button to get the next page
        nextTextButton.setOnClickListener(v -> {
            // check if isFiltered = false;
            if (isFiltered){
                CURRENT_PAGE++;
                fetchFilteredTrans(StartDate, EndDate, CustomerName, CustomerMeter, CURRENT_PAGE);
            } else {
                CURRENT_PAGE++;
                fetchEstateTrans(estateID, CURRENT_PAGE);
            }
        });

        // previous text button to get the previous page
        previousButton.setOnClickListener(v -> {
            // check if it page is greater than 1 else do nothing
            if (CURRENT_PAGE > 1) {
                if (isFiltered){
                    CURRENT_PAGE--;
                    fetchFilteredTrans(StartDate, EndDate, CustomerName, CustomerMeter, CURRENT_PAGE);
                } else {
                    CURRENT_PAGE--;
                    fetchEstateTrans(estateID, CURRENT_PAGE);
                }
            }

        });

        // set the navigation view
        estateHistoryDash.setSelectedItemId(R.id.estateHistoryMenu);
        estateHistoryDash.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.estateHomeMenu){
                startActivity(new Intent(this, EstateDashboard.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateHistoryMenu) {
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

    }

    public void fetchEstateTrans(int estateId, int page) {
        isFiltered = false;
        String transRequest = baseUrl+"/estate/transactions/" + estateId + "?page=" + page + "&pageSize=" + PAGE_SIZE;
//        String transRequest = "http://41.78.157.215:4173/estate/transactions/" + estateId + "?page=" + page + "&pageSize=" + PAGE_SIZE;
        try {
            JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, transRequest, null, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1)/PAGE_SIZE;
                        historyRecycle.setVisibility(View.VISIBLE);
                        historyLinear.setVisibility(View.GONE);
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
                            estateTransAdapter.notifyDataSetChanged();
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
                        Toast.makeText(this, "Incorrect Estate Number.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "No more transactions found.", Toast.LENGTH_SHORT).show();
                }
                // the adapter
//                estateTransAdapter.notifyDataSetChanged();
            }, error -> {
                Toast.makeText(this, "No more transactions found.", Toast.LENGTH_SHORT).show();
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

    // Interface to handle selected meter in the secondary bottom sheet
    private interface MeterSelectionListener {
        void onMeterSelected(String meter);
    }

    // FIRST BOTTOM SHEET
    private void bottomSheetFilter() {
        // bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(EstateHistory.this);
        bottomSheetView = getLayoutInflater().inflate(R.layout.filter_bottom_sheet, findViewById(R.id.filterMain), false);

        // the ids
        startInput = bottomSheetView.findViewById(R.id.filterStartDate);
        endInput = bottomSheetView.findViewById(R.id.filterEndDate);
        customerNameInput = bottomSheetView.findViewById(R.id.filterNameInput);
        customerMeterInput = bottomSheetView.findViewById(R.id.filterMeterInput);
        startLayout = bottomSheetView.findViewById(R.id.filterStartLayout);
        endLayout = bottomSheetView.findViewById(R.id.filterEndLayout);
        nameLayout = bottomSheetView.findViewById(R.id.filterNameLayout);
        meterLayout = bottomSheetView.findViewById(R.id.filterMeterLayout);
        applyButton = bottomSheetView.findViewById(R.id.filterButton);
        resetAll = bottomSheetView.findViewById(R.id.estateResetAll);

        // if the user clicks on the customerMeterInput, it should display the search_bottom_sheet
        customerMeterInput.setFocusable(false);
        customerMeterInput.setOnClickListener(v -> meterNumBottomSheet(meter -> customerMeterInput.setText(meter)));
        customerNameInput.setFocusable(false);
        customerNameInput.setOnClickListener(v -> meterNamBottomSheet(meter -> customerNameInput.setText(meter)));

        // the start and end date
        startInput.setFocusable(false);
        endInput.setFocusable(false);
        startInput.setOnClickListener(v -> theDatePicker(startInput));
        endInput.setOnClickListener(v -> theDatePicker(endInput));

        // if reset all is clicked, clear all the input fields
        resetAll.setOnClickListener(v -> {
                    startInput.setText("");
                    endInput.setText("");
                    customerNameInput.setText("");
                    customerMeterInput.setText("");
        });

        // start by calling an api to get the data and load in the recycler view of the search bottom sheet
        applyButton.setOnClickListener(v -> {
            // get the the text from the input
            StartDate = Objects.requireNonNull(startInput.getText()).toString();
            EndDate = Objects.requireNonNull(endInput.getText()).toString();
            CustomerName = Objects.requireNonNull(customerNameInput.getText()).toString();
            CustomerMeter = Objects.requireNonNull(customerMeterInput.getText()).toString();

            CURRENT_PAGE = 1;
            // fetch the filtered transactions
            fetchFilteredTrans(StartDate, EndDate, CustomerName, CustomerMeter, CURRENT_PAGE);
        });

        // show the bottom sheet
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    // datePicker function
    public void theDatePicker(TextInputEditText input) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // DateDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) ->{
            calendar.set(selectedYear, selectedMonth, selectedDay);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            //String date = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            input.setText(formattedDate);
        }, year, month, day);
        datePickerDialog.show();
    }

    // get the transactions for the filter
    private void fetchFilteredTrans(String start, String end, String name, String meter, int page) {
        isFiltered = true;
//        String filterUrl = "http://41.78.157.215:4173/estate/transactions";
        String filterUrl = baseUrl+"/estate/transactions";
        try {
            JSONObject filterRequest = new JSONObject();
            try {
                filterRequest.put("startDate", start);
                filterRequest.put("endDate", end);
                filterRequest.put("customerName", name);
                filterRequest.put("meterID", meter);
                filterRequest.put("estateID", estateID);
                filterRequest.put("page", page);
                filterRequest.put("pageSize", PAGE_SIZE);
            } catch (JSONException e) {
                Toast.makeText(this, "Could not pass body", Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest filterRequestObject = new JsonObjectRequest(Request.Method.POST, filterUrl, filterRequest, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        historyRecycle.setVisibility(View.VISIBLE);
                        historyLinear.setVisibility(View.GONE);
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1)/PAGE_SIZE;
                        JSONArray jsonArray = response.getJSONArray("data");

//                        Set<String> existingTransIds = new HashSet<>();
//
//                        // Store existing transaction IDs to avoid duplicates
//                        for (UserTransModel model : userList) {
//                            existingTransIds.add(model.getTransID());
//                        }
//
//                        //get the initial sze of userList, cus the first transaction must have populated it.
//                        int initialSize = userList.size();
//                        boolean dataAdded = false;

                        if (jsonArray.length() > 0) {
                            //loop through to get the data i need from the array
                            userList.clear();
                            for (int i = 0; i < jsonArray.length(); i++){
                                //get the first item in the array
                                JSONObject dataObject = jsonArray.getJSONObject(i);
                                String transID = dataObject.getString("transRef");
//                                // Check if the transaction ID already exists in the existingTransIds set
//                                if (existingTransIds.contains(transID)) {
//                                    continue;
//                                }
                                // get the data
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

//                                existingTransIds.add(transID);
//                                // Notify adapter about the new item added
                                //estateTransAdapter.notifyItemInserted(0);
                                //dataAdded = true;
                            }
                            bottomSheetDialog.dismiss();
                            estateTransAdapter.notifyDataSetChanged();
                        } else {
                            pagesText.setText("--0--");
                            historyRecycle.setVisibility(View.GONE);
                            historyLinear.setVisibility(View.VISIBLE);
                            bottomSheetDialog.dismiss();
                            Toast.makeText(this, "No transactions yet.", Toast.LENGTH_LONG).show();
                        }
                        bottomSheetDialog.dismiss();
                        // Check if any new data was added
//                        if (!dataAdded && initialSize == userList.size()) {
//                            Toast.makeText(this, "No new transactions found.", Toast.LENGTH_LONG).show();
//                            // close the bottom sheet
//                            bottomSheetDialog.dismiss();
//                        }

                        // Show the existing or updated list without changing visibility of `historyLinear`
                        //historyRecycle.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        // dismiss the bottom sheet and clear the recycler view, set the visibility to gone and show the empty linear layout
                        bottomSheetDialog.dismiss();
                        historyRecycle.setVisibility(View.GONE);
                        historyLinear.setVisibility(View.VISIBLE);
                    }
                    // the adapter
                    bottomSheetDialog.dismiss();
//                    estateTransAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {

                bottomSheetDialog.dismiss();
                Toast.makeText(this, "No more transactions found.", Toast.LENGTH_SHORT).show();
            }) // this will be used mainly in the screens that are calling protected api
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(filterRequestObject);
        } catch (Exception e) {
            historyLinear.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void meterNumBottomSheet(MeterSelectionListener listener) {
        meterNumBottomSheetDialog = new BottomSheetDialog(this);
        meterNumBottomSheetView = getLayoutInflater().inflate(R.layout.search_bottom_sheet, findViewById(R.id.searchMain), false);
        // ids
        meterSearchInput = meterNumBottomSheetView.findViewById(R.id.searchMeterInput);
        meterListRecycler = meterNumBottomSheetView.findViewById(R.id.searchRecycler);
        meterSearchLayout = meterNumBottomSheetView.findViewById(R.id.searchMeterLayout);
        // initialize the arrayList
        meterList = new ArrayList<>();
        // set the recycler view
        meterListRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // set adapter and recycler
        meterListAdapter = new FilterAdapter( meterList, this, met ->{
            // pass in the selected string
            listener.onMeterSelected(met.getMeterNumber());
            meterNumBottomSheetDialog.dismiss();
        });
        meterListRecycler.setAdapter(meterListAdapter);

        // get the data from the api response and pass into the adapter
        String filterUrl = baseUrl+"/estate/estatemeters?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        //String filterUrl = "http://41.78.157.215:4173/estate/estatemeters?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("estateID", estateID);
            jsonRequest.put("meterID", meter);
        } catch (JSONException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        JsonObjectRequest filterRequest = new JsonObjectRequest(Request.Method.POST, filterUrl, jsonRequest, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")) {
                    //meterList.clear();
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String meterID = dataObject.getString("meterID");
                        String username = dataObject.getString("userName");
                        String blockNo = dataObject.getString("blockNo");
                        String flatNo = dataObject.getString("flatNo");

                        //add to the model and list
                        meterListModel = new FilterModel(username, meterID, blockNo, flatNo);
                        meterList.add(meterListModel);
                    }
                } else {
                    Toast.makeText(this, "No meters found.", Toast.LENGTH_SHORT).show();
                    meterListRecycler.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            meterListAdapter.updateData(meterList);

        }, error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(filterRequest);

        // add text-watcher to the input to trigger logic with API calls as user types
        meterSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                fetchFilteredMeter(s.toString(), meterListAdapter);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchFilteredMeter(s.toString(), meterListAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // show the bottom sheet
        meterNumBottomSheetDialog.setContentView(meterNumBottomSheetView);
        meterNumBottomSheetDialog.show();

    }

    private void fetchFilteredMeter(String mete, FilterAdapter adapter) {
        // this filter makes an api call to get the meters, then changes the state of the recyclerView and adapter to the filtered list
        String filterUrl = baseUrl+"/estate/estatemeters?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        //String filterUrl = "http://41.78.157.215:4173/estate/estatemeters?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        ArrayList<FilterModel> newList = new ArrayList<>();
        meterListRecycler.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("estateID", estateID);
            jsonRequest.put("meterID", mete);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        JsonObjectRequest filterRequest = new JsonObjectRequest(Request.Method.POST, filterUrl, jsonRequest, response -> {
            try {
                String message = response.getString("message");

                if (message.equals("success")) {
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String meterID = dataObject.getString("meterID");
                        String username = dataObject.getString("userName");
                        String blockNo = dataObject.getString("blockNo");
                        String flatNo = dataObject.getString("flatNo");

                        //add to the model and list
                        meterListModel = new FilterModel(username, meterID, blockNo, flatNo);
                        newList.add(meterListModel);
                    }
                } else {
                    Toast.makeText(this, "No meters found.", Toast.LENGTH_SHORT).show();
                    meterListRecycler.setVisibility(View.GONE);
                }
            }catch (JSONException e) {

                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
            }
            // set adapter and recycler
            adapter.updateData(newList);
            //meterListRecycler.setAdapter(adapter);
        }, error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(filterRequest);
    }

    // for meter name
    private void meterNamBottomSheet(MeterSelectionListener listener) {
        meterNameBottomSheetDialog = new BottomSheetDialog(this);
        meterNameBottomSheetView = getLayoutInflater().inflate(R.layout.search_customer_sheet, findViewById(R.id.filterCustomer), false);
        // ids
        nameSearchInput = meterNameBottomSheetView.findViewById(R.id.searchNameInput);
        nameListRecycler = meterNameBottomSheetView.findViewById(R.id.searchNameRecycler);
        nameSearchLayout = meterNameBottomSheetView.findViewById(R.id.searchNameLayout);
        // initialize the arrayList
        meterList = new ArrayList<>();
        // set the recycler view
        nameListRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // set adapter and recycler
        nameListAdapter = new FilterAdapter( meterList, this, met ->{
            // pass in the selected string inversely, this is cus i'm using one adapter that been defined on how it should be.
            listener.onMeterSelected(met.getMeterNumber());
            meterNameBottomSheetDialog.dismiss();
        });
        nameListRecycler.setAdapter(nameListAdapter);

        // get the data from the api response and pass into the adapter
        String filterUrl = baseUrl+"/estate/estatemetername?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        //String filterUrl = "http://41.78.157.215:4173/estate/estatemetername?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("estateID", estateID);
            jsonRequest.put("meterName", meter);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        JsonObjectRequest filterRequest = new JsonObjectRequest(Request.Method.POST, filterUrl, jsonRequest, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")) {
                    //meterList.clear();
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String meterID = dataObject.getString("meterID");
                        String username = dataObject.getString("userName");
                        String blockNo = dataObject.getString("blockNo");
                        String flatNo = dataObject.getString("flatNo");

                        //add to the model and list
                        meterListModel = new FilterModel(meterID, username, blockNo, flatNo);
                        meterList.add(meterListModel);
                    }
                } else {
                    Toast.makeText(this, "No meters found.", Toast.LENGTH_SHORT).show();
                    nameListRecycler.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            nameListAdapter.updateData(meterList);

        }, error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(filterRequest);

        // add text-watcher to the input to trigger logic with API calls as user types
        nameSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //fetchFilteredName(s.toString(), nameListAdapter);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fetchFilteredName(s.toString(), nameListAdapter);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // show the bottom sheet
        meterNameBottomSheetDialog.setContentView(meterNameBottomSheetView);
        meterNameBottomSheetDialog.show();

    }

    private void fetchFilteredName(String mete, FilterAdapter adapter) {
        // this filter makes an api call to get the meters, then changes the state of the recyclerView and adapter to the filtered list
        String filterUrl = baseUrl+"/estate/estatemetername?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        //String filterUrl = "http://41.78.157.215:4173/estate/estatemetername?page="  + CURRENT_PAGE +"&pageSize=" + PAGE_SIZE;
        ArrayList<FilterModel> newList = new ArrayList<>();
        nameListRecycler.setLayoutManager(new LinearLayoutManager(this));
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("estateID", estateID);
            jsonRequest.put("meterName", mete);
        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        JsonObjectRequest filterRequest = new JsonObjectRequest(Request.Method.POST, filterUrl, jsonRequest, response -> {
            try {
                String message = response.getString("message");

                if (message.equals("success")) {
                    //newList.clear();
                    JSONArray dataArray = response.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String meterID = dataObject.getString("meterID");
                        String username = dataObject.getString("userName");
                        String blockNo = dataObject.getString("blockNo");
                        String flatNo = dataObject.getString("flatNo");

                        //add to the model and list
                        meterListModel = new FilterModel(meterID, username, blockNo, flatNo);
                        newList.add(meterListModel);
                    }
                } else {
                    Toast.makeText(this, "No meters found.", Toast.LENGTH_SHORT).show();
                    nameListRecycler.setVisibility(View.GONE);
                }
            }catch (JSONException e) {
                Toast.makeText(this, "No Internet Connection.", Toast.LENGTH_SHORT).show();
            }
            // set adapter and recycler
            adapter.updateData(newList);
            //meterListRecycler.setAdapter(adapter);
        }, error -> {
            String message = Arrays.toString(error.networkResponse.data);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(filterRequest);
    }
}