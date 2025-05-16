package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MeterDetails extends AppCompatActivity {

    TextView meterNum, meterName, meterEmail, meterDate, meterUnits, meterAmount, meterStatus, meterPhone, meterBlockNo, meterFlatNo, updateMet;
    SwitchCompat theSwitch;
    Button generateButton;
    String PhoneText, vat;
    SharedPreferences validSharedPref, utilityPref;
    SharedPreferences.Editor prefEditor;
    String theMeterName, theMeterNum, theMeterEmail, theMeterLastDate, theMeterStatus, theMeterPhone, theMeterBlockNo, theMeterFlatNo, baseUrl;
    RelativeLayout backRelative;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.meter_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRelative), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        generateButton = findViewById(R.id.detailsGenerate);
        theSwitch = findViewById(R.id.detailsSwitch);
        meterNum = findViewById(R.id.detailsMeterNum);
        meterName = findViewById(R.id.detailsMeterName);
        meterEmail = findViewById(R.id.detailsMeterEmail);
        meterDate = findViewById(R.id.detailsMeterLast);
        meterUnits = findViewById(R.id.detailsMeterUnits);
        meterAmount = findViewById(R.id.detailsMeterAmount);
        meterStatus = findViewById(R.id.transStatus);
        backRelative = findViewById(R.id.backRelative);
        meterPhone = findViewById(R.id.detailsMeterPhone);
        meterBlockNo = findViewById(R.id.detailsMeterBlockNo);
        meterFlatNo = findViewById(R.id.detailsMeterFlatNo);
        updateMet = findViewById(R.id.updateMet);

        preloaderLogo = new PreloaderLogo(this);

        // implementation of the back button
        backRelative.setOnClickListener(v -> finish());
        updateMet.setOnClickListener(v -> startActivity(new Intent(MeterDetails.this, EditMeter.class)));

        // the sharedPref for token
        validSharedPref = getSharedPreferences("estateVendPref", Context.MODE_PRIVATE);
        prefEditor = validSharedPref.edit();
        utilityPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        vat = utilityPref.getString("vat", null);

        // get and set the text and status
        theMeterName = validSharedPref.getString("meterName", "");
        theMeterNum = validSharedPref.getString("meterID", "");
        theMeterEmail = validSharedPref.getString("email", "");
        theMeterLastDate = validSharedPref.getString("lastPurchase", "");
        String theMeterAmount = validSharedPref.getString("totalAmount", "");
        theMeterStatus = validSharedPref.getString("vendStatus", "");
        String theMeterUnits = validSharedPref.getString("totalUnits", "");
        PhoneText = validSharedPref.getString("phone", "");
        theMeterPhone = validSharedPref.getString("phone", "");
        theMeterBlockNo = validSharedPref.getString("blockNo", "");
        theMeterFlatNo = validSharedPref.getString("flatNo", "");

        meterNum.setText(theMeterNum);
        meterName.setText(theMeterName);
        meterEmail.setText(theMeterEmail);
        meterDate.setText(theMeterLastDate);
        meterUnits.setText(theMeterUnits);
        meterAmount.setText(theMeterAmount);
        meterPhone.setText(theMeterPhone);
        meterBlockNo.setText(theMeterBlockNo);
        meterFlatNo.setText(theMeterFlatNo);

        // check if enabled or disabled
        assert theMeterStatus != null;
        if (theMeterStatus.equals("Enabled")){
            meterStatus.setText(theMeterStatus);
            meterStatus.setBackgroundResource(R.drawable.text_background);
            //meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
            // set the text colour to success
            meterStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
            // put on the switch
            theSwitch.setChecked(true);
        } else if (theMeterStatus.equals("Disabled")) {
            meterStatus.setText(theMeterStatus);
            meterStatus.setBackgroundResource(R.drawable.text_error_background);
            //meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.disableBackground));
            meterStatus.setTextColor(ContextCompat.getColor(this, R.color.disableText));
            // put on the switch
            theSwitch.setChecked(false);
        }


        // work on the switch
        theSwitch.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            // check the switch status
            String status = isChecked ? "Enabled" : "Disabled";
            updateStatus(status, theMeterNum);
        }));

        // bring out the bottom sheet
        generateButton.setOnClickListener(v ->
            startActivity(new Intent(MeterDetails.this, EstatePurchase.class))
        );


    }

    // update status
    private void updateStatus (String stat, String metID) {
        preloaderLogo.show();
        String apiURL = baseUrl+"/estate/updateMeterStatus";
        String token =  utilityPref.getString("token", null);
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("status", stat);
            requestData.put("meterID", metID );
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, apiURL, requestData, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        String status = response.getString("status");
                        preloaderLogo.dismiss();
                        // if it works change the text
                        if (status.equals("Enabled")){
                            meterStatus.setText(theMeterStatus);
                            meterStatus.setBackgroundResource(R.drawable.text_background);
                            //meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.successBackground));
                            // set the text colour to success
                            meterStatus.setTextColor(ContextCompat.getColor(this, R.color.success));
                            // put on the switch
                            theSwitch.setChecked(true);
                            // call api to change the database
                        } else { // disabled
                            meterStatus.setText(theMeterStatus);
                            meterStatus.setBackgroundResource(R.drawable.text_error_background);
                            //meterStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.disableBackground));
                            meterStatus.setTextColor(ContextCompat.getColor(this, R.color.disableText));
                            // put on the switch
                            theSwitch.setChecked(false);
                        }
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(MeterDetails.this, "No Meter Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(MeterDetails.this, "Error: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(MeterDetails.this, "Please try again", Toast.LENGTH_LONG).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            // Add the request to the RequestQueue
            VolleySingleton.getInstance(this).addToRequestQueue(objectRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(MeterDetails.this, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}