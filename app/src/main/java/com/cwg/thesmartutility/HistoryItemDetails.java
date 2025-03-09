package com.cwg.thesmartutility;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.model.EstateMeterModel;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HistoryItemDetails extends AppCompatActivity {

    TextView itemAmountText, itemAmount,  itemMeterNum, itemToken, itemTransID, itemTransCharge, itemTariff, itemTokenValue, itemVat, itemDateTime, itemUnit;
    ImageView itemBackImage;
    String TokenText, token, baseUrl;
    SharedPreferences validSharedPref;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.history_item_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        itemAmountText = findViewById(R.id.itemAmountText);
        itemAmount = findViewById(R.id.itemAmount);
        itemMeterNum  = findViewById(R.id.itemMeterNum);
        itemToken = findViewById(R.id.itemToken);
        itemTransID = findViewById(R.id.itemTransID);
        itemTransCharge = findViewById(R.id.itemTransCharge);
        itemTariff = findViewById(R.id.itemTariff);
        itemTokenValue = findViewById(R.id.itemTokenValue);
        itemVat = findViewById(R.id.itemVat);
        itemUnit = findViewById(R.id.itemUnit);
        itemBackImage = findViewById(R.id.itemBackImage);
        itemDateTime = findViewById(R.id.itemDate);

        preloaderLogo = new PreloaderLogo(this);

        // shared pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = validSharedPref.getString("token", "");

        // implement back image pressed
        itemBackImage.setOnClickListener(v -> finish());

        // make itemToken copyable
        itemToken.setOnClickListener(v -> {
            // Copy the text to clipboard
            TokenText = itemToken.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", TokenText);
            clipboard.setPrimaryClip(clip);

            // Show a confirmation message
            Toast.makeText(HistoryItemDetails.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // get the data from the intent
        Intent intent = getIntent();
        String transRef = intent.getStringExtra("itemTransRef");
        assert transRef != null;
        Log.d("The Ref" , transRef);
        // get the details
        getItemDetails(transRef);




    }
     private void getItemDetails(String transRef) {
         preloaderLogo.show();
        // string url
         //String detailsUrl = "http://41.78.157.215:4173/g/transactionDetail/" + transRef;
         String detailsUrl = baseUrl+"/g/transactionDetail/" + transRef;
         // get the item details
         JsonObjectRequest fetchRequest = new JsonObjectRequest(Request.Method.GET, detailsUrl, null, response -> {
             try {
                 String message = response.getString("message");
                 if (message.equals("success")){
                     JSONArray jsonArray = response.getJSONArray("data");
                     // check the array
                     if (jsonArray.length() > 0) {
                         //loop through to get the data i need from the array
                         for (int i = 0; i < jsonArray.length(); i++){
                             //get the first item in the array
                             JSONObject dataObject = jsonArray.getJSONObject(i);
                             //get string or int with the name that corresponds with the one in the database
                             String transID = dataObject.getString("transRef");
                             String meterID = dataObject.getString("meterID");
                             String amount = dataObject.getString("amount");
                             String date = dataObject.getString("date");
                             String time = dataObject.getString("time");
                             String units = dataObject.getString("units");
                             String tariff = dataObject.getString("tariff");
                             String tokenText = dataObject.getString("token");
                             String vat = dataObject.getString("vat");
                             String transCharge = dataObject.getString("chargeAmount");
                             String vendedAmount = dataObject.getString("vendedAmount");
                             Log.d("token", tokenText);

                             // set the text
                             itemTransID.setText(transID);
                             itemMeterNum.setText(meterID);
                             itemAmount.setText("₦" + amount);
                             itemDateTime.setText(date + ", " + time);
                             itemUnit.setText(units + " KwH");
                             itemTariff.setText("₦" +tariff);
                             itemToken.setText(tokenText);
                             itemVat.setText("₦" + vat);
                             itemTransCharge.setText("₦" + transCharge);
                             itemTokenValue.setText("₦" + vendedAmount);
                             itemAmountText.setText("₦" + amount);
                             preloaderLogo.dismiss();
                         }
                     } else {
                         preloaderLogo.dismiss();
                         Toast.makeText(HistoryItemDetails.this, "No transaction.", Toast.LENGTH_LONG).show();
                     }
                 }
             } catch (JSONException e) {
                 // show a toast
                 preloaderLogo.dismiss();
                 Toast.makeText(HistoryItemDetails.this, "Error: Could not get response", Toast.LENGTH_SHORT).show();
//                 throw new RuntimeException(e);
             }
         }, error -> {
             preloaderLogo.dismiss();
             // show a toast
             Toast.makeText(HistoryItemDetails.this, "Error: " + error, Toast.LENGTH_SHORT).show();
         }){
             @Override
             public Map<String, String> getHeaders() {
                 Map<String, String> headers = new HashMap<>();
                 headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                 return headers;
             }
         };
         VolleySingleton.getInstance(this).addToRequestQueue(fetchRequest);

     }
}