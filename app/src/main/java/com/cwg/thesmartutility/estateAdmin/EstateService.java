package com.cwg.thesmartutility.estateAdmin;

import android.app.DatePickerDialog;
import android.content.Context;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.EstateServiceAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.EstateServiceModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

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

public class EstateService extends AppCompatActivity {

    SharedPreferences validSharedPref;
    LinearLayout serviceLinear;
    Button applyButton;
    String token, baseUrl;
    RecyclerView serviceRecycler;
    EstateServiceAdapter estateServiceAdapter;
    ArrayList<EstateServiceModel> estateServiceList;
    EstateServiceModel estateServiceModel;
    TextInputEditText startInput, endInput;
    String StartDate, EndDate;
    private int CURRENT_PAGE = 1;
    private final int PAGE_SIZE = 10;
    TextView nextTextButton, previousButton, pagesText, resetAll;
    private boolean isFiltered = false;
    View bottomSheetView;
    BottomSheetDialog bottomSheetDialog;
    ImageView filterRelative, historyBack;
    PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // ids
        serviceRecycler = findViewById(R.id.estateServiceRecycler);
        filterRelative = findViewById(R.id.estateFilterService);
        serviceLinear = findViewById(R.id.serviceEmptyLinear);
        nextTextButton = findViewById(R.id.nextServiceText);
        previousButton = findViewById(R.id.previousServiceText);
        pagesText = findViewById(R.id.pagesServiceText);
        historyBack = findViewById(R.id.backImage);
        historyBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // shared preferences
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = validSharedPref.getString("token", "");

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // array list
        estateServiceList = new ArrayList<>();
        estateServiceAdapter = new EstateServiceAdapter(EstateService.this, estateServiceList);
        serviceRecycler.setLayoutManager(new LinearLayoutManager(EstateService.this));
        serviceRecycler.setAdapter(estateServiceAdapter);

        // fetch transactions
        getService();

        // filter bottom sheet
        filterRelative.setOnClickListener(v -> startBottomSheet());

        // next and previous buttons
        nextTextButton.setOnClickListener(v -> {
            if (isFiltered) {
                CURRENT_PAGE++;
                filteredService(StartDate, EndDate, CURRENT_PAGE);
            } else {
                CURRENT_PAGE++;
                getService();
            }
        });
        previousButton.setOnClickListener(v -> {
            if (CURRENT_PAGE > 1) {
                if (isFiltered) {
                    CURRENT_PAGE--;
                    filteredService(StartDate, EndDate, CURRENT_PAGE);
                } else {
                    CURRENT_PAGE--;
                    getService();
                }
            }
        });
    }

    // fetch service transaction
    private void getService() {
        preloaderLogo.show();
        isFiltered = false;
        String serviceRequest = baseUrl+"/user/allservicetrans?page=" + CURRENT_PAGE + "&pageSize=" + PAGE_SIZE;
        try {
            JsonObjectRequest fetchServiceRequest = new JsonObjectRequest(Request.Method.GET, serviceRequest, null, response -> {
                preloaderLogo.dismiss();
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        // show recycler and hide linear
                        serviceRecycler.setVisibility(View.VISIBLE);
                        serviceLinear.setVisibility(View.GONE);
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1)/PAGE_SIZE;
                        if (totalPages == CURRENT_PAGE){
                            // make next button invisible
                            nextTextButton.setVisibility(View.INVISIBLE);
                        } else {
                            nextTextButton.setVisibility(View.VISIBLE);
                        }
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            pagesText.setText("--0--");
                            serviceLinear.setVisibility(View.VISIBLE);
                            serviceRecycler.setVisibility(View.GONE);
                            Toast.makeText(this, "No transactions found.", Toast.LENGTH_LONG).show();
                        } else {
                            serviceLinear.setVisibility(View.GONE);
                            estateServiceList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject serviceObject = data.getJSONObject(i);
                                String serviceID = serviceObject.getString("subscription_id");
                                String meterID = serviceObject.getString("meterID");
                                String serviceAmount = serviceObject.getString("sub_amount");
                                String servicePayDate = serviceObject.getString("payment_date");
                                String serviceEndDate = serviceObject.getString("expiry_date");

                                // set page text
                                pagesText.setText(CURRENT_PAGE + " of " + totalPages+" Pages");

                                // add to model
                                estateServiceModel = new EstateServiceModel(serviceID, meterID, serviceAmount, servicePayDate, serviceEndDate);
                                estateServiceList.add(estateServiceModel);
                            }
                            estateServiceAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    serviceLinear.setVisibility(View.VISIBLE);
                    serviceRecycler.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }, error -> {
                preloaderLogo.dismiss();
                serviceLinear.setVisibility(View.VISIBLE);
                serviceRecycler.setVisibility(View.GONE);
                Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            fetchServiceRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(fetchServiceRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // bottom sheet
    private void startBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetView = getLayoutInflater().inflate(R.layout.estate_service_trans_bs, findViewById(R.id.estateServiceBS), false);

        // ids
        resetAll = bottomSheetView.findViewById(R.id.dateResetAll);
        startInput = bottomSheetView.findViewById(R.id.filterStartDate);
        endInput = bottomSheetView.findViewById(R.id.filterEndDate);
        applyButton = bottomSheetView.findViewById(R.id.filterDateButton);

        // reset all
        resetAll.setOnClickListener(v -> {
            startInput.setText("");
            endInput.setText("");
        });

        // start and end date
        startInput.setFocusable(false);
        endInput.setFocusable(false);
        startInput.setOnClickListener(v -> theDatePicker(startInput));
        endInput.setOnClickListener(v -> theDatePicker(endInput));

        // apply button
        applyButton.setOnClickListener(v -> {
            StartDate = Objects.requireNonNull(startInput.getText()).toString();
            EndDate = Objects.requireNonNull(endInput.getText()).toString();
            CURRENT_PAGE = 1;

            // Dismiss the bottom sheet if it's showing
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }

            // Check if both dates are empty, then call getService() for unfiltered data
            if (StartDate.isEmpty() && EndDate.isEmpty()){
                isFiltered = false;
                getService();
            } else {
                filteredService(StartDate, EndDate, CURRENT_PAGE);
            }
            //filteredService(StartDate, EndDate, CURRENT_PAGE);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void filteredService(String start, String end, int page) {
        preloaderLogo.show();
        isFiltered = true;
        String serviceRequest = baseUrl+"/user/allservicetrans?startDate=" + start + "&endDate=" + end + "&page=" + page + "&pageSize=" + PAGE_SIZE;
        try {
            JsonObjectRequest fetchServiceRequest = new JsonObjectRequest(Request.Method.GET, serviceRequest, null, response -> {
                preloaderLogo.dismiss();
                bottomSheetDialog.dismiss();
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        // show recycler and hide linear
                        serviceRecycler.setVisibility(View.VISIBLE);
                        serviceLinear.setVisibility(View.GONE);
                        // get the total count
                        int totalCount = response.getInt("totalCount");
                        int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
                        if (totalPages == CURRENT_PAGE) {
                            // make next button invisible
                            nextTextButton.setVisibility(View.INVISIBLE);
                        } else {
                            nextTextButton.setVisibility(View.VISIBLE);
                        }
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            pagesText.setText("--0--");
                            serviceLinear.setVisibility(View.VISIBLE);
                            serviceRecycler.setVisibility(View.GONE);
                            Toast.makeText(this, "No transactions found.", Toast.LENGTH_LONG).show();
                        } else {
                            serviceLinear.setVisibility(View.GONE);
                            estateServiceList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject serviceObject = data.getJSONObject(i);
                                String serviceID = serviceObject.getString("subscription_id");
                                String meterID = serviceObject.getString("meterID");
                                String serviceAmount = serviceObject.getString("sub_amount");
                                String servicePayDate = serviceObject.getString("payment_date");
                                String serviceEndDate = serviceObject.getString("expiry_date");

                                // set page text
                                pagesText.setText(CURRENT_PAGE + " of " + totalPages+" Pages");

                                // add to model
                                estateServiceModel = new EstateServiceModel(serviceID, meterID, serviceAmount, servicePayDate, serviceEndDate);
                                estateServiceList.add(estateServiceModel);
                            }
                            bottomSheetDialog.dismiss();
                            estateServiceAdapter.notifyDataSetChanged();
                        }
                    } else {
                        pagesText.setText("--0--");
                        serviceLinear.setVisibility(View.VISIBLE);
                        serviceRecycler.setVisibility(View.GONE);
                        Toast.makeText(this, "No transactions found.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
//                    preloaderLogo.dismiss();
//                    bottomSheetDialog.dismiss();
                    serviceLinear.setVisibility(View.VISIBLE);
                    serviceRecycler.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                bottomSheetDialog.dismiss();
                pagesText.setText("--0--");
                serviceLinear.setVisibility(View.VISIBLE);
                serviceRecycler.setVisibility(View.GONE);
                Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            fetchServiceRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(fetchServiceRequest);
        }catch (Exception e) {
//            preloaderLogo.dismiss();
//            bottomSheetDialog.dismiss();
            serviceLinear.setVisibility(View.VISIBLE);
            serviceRecycler.setVisibility(View.GONE);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void theDatePicker(TextInputEditText input) {
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
}