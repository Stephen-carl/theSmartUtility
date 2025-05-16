package com.cwg.thesmartutility.estateAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.EditMeterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.EditMeterModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditService extends AppCompatActivity {
    TextInputEditText serviceAmountInput;
    TextInputLayout serviceAmountLayout;
    Spinner addSpinner;
    TextView addDuration1, addDuration3, addDuration6, addDuration12;
    SharedPreferences validSharedPref;
    String ServiceAmount, token, baseUrl, theApartmentType, duration;
    Button addButton;
    EditMeterAdapter editMeterAdapter;
    ArrayList<EditMeterModel> editMeterModelList;
    ImageView addBack;
    PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // ids
        serviceAmountInput = findViewById(R.id.addAmountInput);
        serviceAmountLayout = findViewById(R.id.addAmountLayout);
        addSpinner = findViewById(R.id.addApartSpinner);
        addDuration1 = findViewById(R.id.addDuration1);
        addDuration3 = findViewById(R.id.addDuration3);
        addDuration6 = findViewById(R.id.addDuration6);
        addDuration12 = findViewById(R.id.addDuration12);
        addButton = findViewById(R.id.addServiceButton);
        addBack = findViewById(R.id.addBack);

        // sharedPref and baseUrl, preloader
        validSharedPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        token = validSharedPref.getString("token", "");
        baseUrl = this.getString(R.string.managementBaseURL);
        preloaderLogo = new PreloaderLogo(this);

        // initialize the list and adapter
        editMeterModelList = new ArrayList<>();
        editMeterAdapter = new EditMeterAdapter(this, editMeterModelList);

        // get Apartments
        getApartments();

        // the textViews for the duration onClick
        // onclick listeners, when one of the textviews are clicked, the textview is highlighted with colour #cccde8, and duration string is set to the textview clicked
        addDuration6.setOnClickListener(v -> {
            addDuration6.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            addDuration1.setBackgroundResource(R.drawable.text_back);
            addDuration3.setBackgroundResource(R.drawable.text_back);
            addDuration12.setBackgroundResource(R.drawable.text_back);
            duration = addDuration6.getText().toString();
        });
        addDuration12.setOnClickListener(v -> {
            addDuration1.setBackgroundResource(R.drawable.text_back);
            addDuration3.setBackgroundResource(R.drawable.text_back);
            addDuration6.setBackgroundResource(R.drawable.text_back);
            addDuration12.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            duration = addDuration12.getText().toString();
        });
        addDuration1.setOnClickListener(v -> {
            addDuration1.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            addDuration3.setBackgroundResource(R.drawable.text_back);
            addDuration6.setBackgroundResource(R.drawable.text_back);
            addDuration12.setBackgroundResource(R.drawable.text_back);
            duration = "1_months";
        });
        addDuration3.setOnClickListener(v -> {
            addDuration3.setBackgroundColor(ContextCompat.getColor(this, R.color.lineEmpty));
            addDuration1.setBackgroundResource(R.drawable.text_back);
            addDuration6.setBackgroundResource(R.drawable.text_back);
            addDuration12.setBackgroundResource(R.drawable.text_back);
            duration = addDuration3.getText().toString();
        });

        // populate the spinner
        addSpinner.setAdapter(editMeterAdapter);
        addSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EditMeterModel editMeterModel = (EditMeterModel) parent.getItemAtPosition(position);
                if (editMeterModel != null) {
                    theApartmentType = editMeterModel.getTextOne();
                    // Toast.makeText(EditMeter.this, "Selected: " + theApartmentType, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // buttons
        addButton.setOnClickListener(v -> init());

        addBack.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(this, EstateServiceList.class));
        });

    }

    private void getApartments(){
        String apiURL = baseUrl+"/estate/getApartments";
        JsonObjectRequest apartRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    JSONArray dataObjects = response.getJSONArray("data");
                    // Clear the current list
                    editMeterModelList.clear();
                    // Add each apartment to the list
                    for (int i = 0; i < dataObjects.length(); i++){
                        JSONObject jsonObject = dataObjects.getJSONObject(i);
                        String type_id = jsonObject.getString("type_id");
                        String type_name = jsonObject.getString("type_name");
                        EditMeterModel editMeterModel = new EditMeterModel(type_id, type_name);
                        editMeterModelList.add(editMeterModel);
                    }
                    // Notify the adapter that the data has changed.
                    editMeterAdapter.notifyDataSetChanged();
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
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(apartRequest);
    }

    private void init(){
        ServiceAmount = serviceAmountInput.getText().toString();
        if (ServiceAmount.isEmpty()){
            serviceAmountLayout.setError("Service amount is required");
        } else {
            addService();
        }
    }

    private void addService(){
        preloaderLogo.show();
        String apiURL = baseUrl+"/estate/addservice";
        JSONObject addServiceDetails = new JSONObject();
        try {
            addServiceDetails.put("amount", ServiceAmount);
            addServiceDetails.put("duration", duration);
            addServiceDetails.put("type_id", theApartmentType);
        } catch (JSONException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        try {
            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.POST, apiURL, addServiceDetails, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Service charge added", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(new Intent(this, EstateServiceList.class));
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Could not update service charge", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 5000;  // 5 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            updateRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(updateRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}