package com.cwg.thesmartutility.estateAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
import com.cwg.thesmartutility.Adapter.EstateServiceAdapter;
import com.cwg.thesmartutility.Adapter.ServiceAdapter;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.model.ServiceModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstateServiceList extends AppCompatActivity {
    ImageView backButton;
    Button addFeeButton;
    RecyclerView feeRecycler;
    ServiceAdapter serviceAdapter;
    ArrayList<ServiceModel> serviceList;
    SharedPreferences validSharedPref;
    PreloaderLogo preloaderLogo;
    String token, baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_service_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        // ids
        backButton = findViewById(R.id.feeBack);
        addFeeButton = findViewById(R.id.addFeeButton);
        feeRecycler = findViewById(R.id.feeRecycler);

        // initialize shared preferences
        validSharedPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        token = validSharedPref.getString("token", "");

        // baseUrl
        baseUrl = this.getString(R.string.managementBaseURL);

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // array list
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(EstateServiceList.this, serviceList);
        feeRecycler.setLayoutManager(new LinearLayoutManager(EstateServiceList.this));
        feeRecycler.setAdapter(serviceAdapter);

        // get the Service
        getServiceList();

        // backButton
        backButton.setOnClickListener(v -> {
            finish();
            startActivity(new Intent(EstateServiceList.this, EstateDashboard.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // add fee button to go update service class
        addFeeButton.setOnClickListener(v -> {
            startActivity(new Intent(EstateServiceList.this, EditService.class));
        });

    }

    private void getServiceList(){
        preloaderLogo.show();
        String serviceRequest = baseUrl+"/estate/getservices";
        JsonObjectRequest fetchServiceRequest = new JsonObjectRequest(Request.Method.GET, serviceRequest, null, response -> {
            try {
                preloaderLogo.dismiss();
                String message = response.getString("message");
                if (message.equals("success")){
                    serviceList.clear();
                    JSONArray dataArray = response.getJSONArray("data");
                    if (dataArray.length() != 0){
                        for (int i = 0; i < dataArray.length(); i++){
                            JSONObject dataObject = dataArray.getJSONObject(i);
                            int serviceID = dataObject.getInt("service_id");
                            int estateID = dataObject.getInt("estate_id");
                            String typeID = dataObject.getString("type_id");
                            String duration = dataObject.getString("duration");
                            String service_amount = dataObject.getString("service_amount");
                            String type_name = dataObject.getString("type_name");

                            ServiceModel serviceModel = new ServiceModel(serviceID, estateID, typeID, duration, service_amount, type_name);
                            serviceList.add(serviceModel);
                        }
                        serviceAdapter.notifyDataSetChanged();
                    }else {
                        Toast.makeText(EstateServiceList.this, "No Service Found", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(EstateServiceList.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            preloaderLogo.dismiss();
            Toast.makeText(EstateServiceList.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(fetchServiceRequest);
    }
}