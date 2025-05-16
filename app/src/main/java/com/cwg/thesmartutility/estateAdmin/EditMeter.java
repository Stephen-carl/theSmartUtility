package com.cwg.thesmartutility.estateAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.Adapter.EditMeterAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.EditMeterModel;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditMeter extends AppCompatActivity {
    ArrayList<EditMeterModel> editMeterModelList;
    EditMeterAdapter editMeterAdapter;
    SharedPreferences SharedPref, validPref;
    TextInputEditText blockNoInput, flatNoInput;
    String blockNo, flatNo, token, theApartmentType, meterID, typeID;
    Spinner apartSpinner;
    String baseUrl;
    ImageView backButton;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.edit_meter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        // ids
        blockNoInput = findViewById(R.id.blockNoInput);
        flatNoInput = findViewById(R.id.flatNoInput);
        apartSpinner = findViewById(R.id.apartSpinner);
        backButton = findViewById(R.id.editBack);
        saveButton = findViewById(R.id.editButton);

        SharedPref = getSharedPreferences("estateVendPref", MODE_PRIVATE);
        validPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        token = validPref.getString("token", "");
        meterID = SharedPref.getString("meterID", "");
        typeID = SharedPref.getString("typeID", "");

        // base url
        baseUrl = this.getString(R.string.managementBaseURL);

        // the list and adapter
        editMeterModelList = new ArrayList<>();
        editMeterAdapter = new EditMeterAdapter(this, editMeterModelList);

        // get the apartment
        getMeterApartment();

//        // get apartment
//        getApartments();


        // save button
        saveButton.setOnClickListener(v -> init());

        // get the current blockNo and flatNo and populate the fields
        blockNo = SharedPref.getString("blockNo", "");
        flatNo = SharedPref.getString("flatNo", "");
        blockNoInput.setText(blockNo);
        flatNoInput.setText(flatNo);

        // baseUrl
        baseUrl = this.getString(R.string.managementBaseURL);

        // back button
        backButton.setOnClickListener(v -> finish());

        // populate the spinner
        apartSpinner.setAdapter(editMeterAdapter);
        apartSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // get the item
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

        apartSpinner.setOnTouchListener((v, event) -> {
            // Load the full list when the spinner is touched.
            getApartments();
            return false;
        });

    }

    // get apartments
    private void getApartments() {
        String apiURL = baseUrl+"/estate/getApartments";
        JsonObjectRequest apartRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    JSONArray dataObjects = response.getJSONArray("data");
                    // Preserve current selection value (if any) so we can restore it after updating.
                    String previousSelected = theApartmentType;

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

                    // Attempt to find the index of the previously selected apartment.
                    int indexToSelect = 0;
                    for (int i = 0; i < editMeterModelList.size(); i++) {
                        // Here getTextTwo() returns the type_name; adjust if needed.
                        if (editMeterModelList.get(i).getTextOne().equals(previousSelected)) {
                            indexToSelect = i;
                            break;
                        }
                    }
                    // Restore the spinner selection
                    apartSpinner.setSelection(indexToSelect);
//                    editMeterModelList.clear();
//                    for (int i = 0; i < dataObjects.length(); i++){
//                        JSONObject jsonObject = dataObjects.getJSONObject(i);
//                        String type_id = jsonObject.getString("type_id");
//                        String type_name = jsonObject.getString("type_name");
//                        EditMeterModel editMeterModel = new EditMeterModel(type_id, type_name);
//                        editMeterModelList.add(editMeterModel);
//                    }
//                    editMeterAdapter.notifyDataSetChanged();
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

    // save the edited meter Details
    public void init() {
        blockNo = Objects.requireNonNull(blockNoInput.getText()).toString().trim();
        flatNo = Objects.requireNonNull(flatNoInput.getText()).toString().trim();
        // cannot be null
        if (blockNo.isEmpty() || flatNo.isEmpty() || theApartmentType.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else {
            saveMeter(blockNo, flatNo, theApartmentType);
        }

    }

    // save the edited meter Details
    public void saveMeter(String blockNo, String flatNo, String theApartmentType) {
        String apiURL = baseUrl+"/estate/updatemeter";
        JSONObject requestObject = new JSONObject();
        try {
            requestObject.put("meterID", meterID);
            requestObject.put("blockNo", blockNo);
            requestObject.put("flatNo", flatNo);
            requestObject.put("type_id", theApartmentType);
            JsonObjectRequest saveRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestObject, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        Toast.makeText(this, "Meter Updated", Toast.LENGTH_SHORT).show();
                        // go to estate meter
                        startActivity(new Intent(EditMeter.this, EstateMeters.class));
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
            VolleySingleton.getInstance(this).addToRequestQueue(saveRequest);

        } catch (JSONException e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getMeterApartment() {
        String apiURL = baseUrl+"/estate/getApartments?type_id="+typeID;
        JsonObjectRequest apartRequest = new JsonObjectRequest(Request.Method.GET, apiURL, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    // clear the list
                    editMeterModelList.clear();
                    JSONArray dataObjects = response.getJSONArray("data");
                    for (int i = 0; i < dataObjects.length(); i++) {
                        JSONObject jsonObject = dataObjects.getJSONObject(i);
                        String type_id = jsonObject.getString("type_id");
                        String type_name = jsonObject.getString("type_name");
                        // display in the spinner using the edit meter card as the layout
                        EditMeterModel editMeterModel = new EditMeterModel(type_id, type_name);
                        editMeterModelList.add(editMeterModel);
                        editMeterAdapter.notifyDataSetChanged();
                    }
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
}