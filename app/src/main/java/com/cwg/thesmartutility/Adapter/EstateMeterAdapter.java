package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.MeterDetails;
import com.cwg.thesmartutility.model.EstateMeterModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EstateMeterAdapter extends RecyclerView.Adapter<EstateMeterAdapter.PostHolder>{
    Context context;
    ArrayList<EstateMeterModel> estateMeterList;

    public EstateMeterAdapter(Context context, ArrayList<EstateMeterModel> estateMeterList) {
        this.context = context;
        this.estateMeterList = estateMeterList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.meter_card, parent, false);
        //call the postHolder function to pass in the view
        return new EstateMeterAdapter.PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        EstateMeterModel estateMeterModel = estateMeterList.get(position);
        holder.setMeterID(estateMeterModel.getMeterID());
        holder.setMeterName(estateMeterModel.getUserName());
        holder.mButton.setOnClickListener(v -> SearchForMeter(estateMeterModel.getMeterID(), estateMeterModel.getEstateID(), estateMeterModel.getEmail()));
    }

    @Override
    public int getItemCount() {
        return estateMeterList.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        //the textViews to use
        TextView mMeter, mName;
        RelativeLayout mButton;
        View view;

        public PostHolder( View itemView) {
            super(itemView);
            //pass the view(context) to this one i defined
            view = itemView;
            mButton = view.findViewById(R.id.viewButton);
        }
        //functions for setting the items and set the text
        public void setMeterName(String MeterName) {
            mName = view.findViewById(R.id.meterNameText);
            mName.setText(MeterName);
        }

        // work on this
        public void setMeterID(String MeterID) {
            mMeter = view.findViewById(R.id.meterNumberText);
            mMeter.setText(MeterID);
        }
    }

    private void SearchForMeter(String meterIn, int EstateID, String userEmail) {

        SharedPreferences validSharedPref = context.getSharedPreferences("estateVendPref", Context.MODE_PRIVATE);
        SharedPreferences SharedPref = context.getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor= validSharedPref.edit();
        String token = SharedPref.getString("token", null);
        int estate = SharedPref.getInt("estateID", 0);

        int estateID;
        if (EstateID == 0){
            estateID = estate;
        } else {
            estateID = EstateID;
        }

        String baseUrl = context.getString(R.string.managementBaseURL);
        String userUrl = baseUrl+"/estate/meterDetails";
        //String userUrl = "http://41.78.157.215:4173/estate/meterDetails";
        try {
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("meterID", meterIn);
                jsonRequest.put("estateID", estateID);
                jsonRequest.put("email", userEmail);
            } catch (JSONException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, userUrl, jsonRequest, response -> {
                try {
                    //get the array with the name data
                    String message = response.getString("message");
                    if (message.equals("success")){
                        if (response.length() != 0){
                            JSONObject dataObject = response.getJSONObject("data");
                            // get the data
                            String meterNum = dataObject.getString("meterID");
                            String customerID = dataObject.getString("customerID");
                            String meterName = dataObject.getString("userName");
                            String email = dataObject.getString("email");
                            String phone = dataObject.getString("phone");
                            String blockNo = dataObject.getString("blockNo");
                            String flatNo = dataObject.getString("flatNo");
                            // FOR USER
                            String vendStatus = dataObject.getString("vendStatus");
                            String tariff = dataObject.getString("tariff");
                            String brand = dataObject.getString("brand");
                            // NEED TO KNOW IF THE ADMIN HAS PRIVILEGE TO USE THE GATEWAY AND VEND
                            String estateVendStatus = dataObject.getString("eVendStatus");
                            String estateGatewayStatus = dataObject.getString("gatewayStatus");
                            String hasPayAcct = dataObject.getString("hasPayAcct");

                            JSONObject totalObject = response.getJSONObject("theTotals");
                            String lastPurchase = totalObject.getString("last_transaction_Date");
                            String totalUnit = totalObject.getString("totalUnits");
                            String amount = totalObject.getString("totalAmount");

                            prefEditor.putString("meterID", meterNum);
                            prefEditor.putString("meterName", meterName);
                            prefEditor.putString("customerID", customerID);
                            prefEditor.putString("email", email);
                            prefEditor.putString("phone", phone);
                            prefEditor.putString("lastPurchase", lastPurchase);
                            prefEditor.putString("totalUnits", totalUnit);
                            prefEditor.putString("totalAmount", amount);
                            prefEditor.putString("tariff", tariff);
                            prefEditor.putString("vendStatus", vendStatus);
                            prefEditor.putString("estateVendStatus", estateVendStatus);
                            prefEditor.putString("estateGatewayStatus", estateGatewayStatus);
                            prefEditor.putString("brand", brand);
                            prefEditor.putString("blockNo", blockNo);
                            prefEditor.putString("flatNo", flatNo);
                            prefEditor.putString("hasPayAcct", hasPayAcct);

                            prefEditor.apply();

                            // Go to Details Screen
                            context.startActivity(new Intent(context, MeterDetails.class));
                        } else {
                            Toast.makeText(context, "No Transaction Found", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(context, "No Transaction Found", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }, error -> Toast.makeText(context, "Could not connect", Toast.LENGTH_LONG).show())
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
        }catch (Exception e) {
            Toast.makeText(context, "Pls connect to internet", Toast.LENGTH_LONG).show();
        }
    }
}
