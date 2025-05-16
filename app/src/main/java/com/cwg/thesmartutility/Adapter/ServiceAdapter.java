package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.EditService;
import com.cwg.thesmartutility.estateAdmin.UpdateService;
import com.cwg.thesmartutility.model.ServiceModel;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.PostHolder>{
    private Context context;
    private ArrayList<ServiceModel> serviceList;

    public ServiceAdapter(Context context, ArrayList<ServiceModel> serviceList) {
        this.context = context;
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.estate_service_list_card, parent, false);
        //call the postHolder function to pass in the view
        return new ServiceAdapter.PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        ServiceModel serviceModel = serviceList.get(position);
        holder.setAmount(serviceModel.getServiceAmount());
        holder.setApartment(serviceModel.getTypeName());
        // make some actions
        holder.mEdit.setOnClickListener(v -> editItem(position));
        holder.mDelete.setOnClickListener(v -> deleteItem(position));

    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }


    public static class PostHolder extends RecyclerView.ViewHolder {
        //the textViews to use
        TextView mAmount, mApartment;
        ImageView mEdit, mDelete;
        View view;

        public PostHolder(@NonNull View itemView) {
            super(itemView);
            //pass the view(context) to this one i defined
            view = itemView;
            mEdit = view.findViewById(R.id.editImage);
            mDelete = view.findViewById(R.id.deleteImage);
        }


        //functions for setting the items and set the text and imageViews
        public void setAmount(String Amount) {
            mAmount = view.findViewById(R.id.feeText);
            mAmount.setText(Amount);
        }
        public void setApartment(String Apartment) {
            mApartment = view.findViewById(R.id.apartmentText);
            mApartment.setText(Apartment);
        }
    }

    // delete function
    public void deleteItem(int position){

//        SharedPreferences validSharedPref = context.getSharedPreferences("estateVendPref", Context.MODE_PRIVATE);
        SharedPreferences SharedPref = context.getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
//        SharedPreferences.Editor prefEditor= validSharedPref.edit();
        String token = SharedPref.getString("token", null);
        ServiceModel serviceModel = serviceList.get(position);


        String baseUrl = context.getString(R.string.managementBaseURL);
        String userUrl = baseUrl+"/estate/deleteservice?service_id="+serviceModel.getServiceId();;

        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, userUrl, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    serviceList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, serviceList.size());
                    Toast.makeText(context, "Service Deleted", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(context, "Error: " +e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, error -> {
            Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                return headers;
            }
        };
        VolleySingleton.getInstance(context).addToRequestQueue(deleteRequest);
    }

    public void editItem(int position){
        // Go to edit service, while passing the typeID and the serviceID
        ServiceModel serviceModel = serviceList.get(position);
        Intent intent = new Intent(context, UpdateService.class);
        intent.putExtra("typeID", serviceModel.getTypeId());
        intent.putExtra("serviceID", serviceModel.getServiceId());
        context.startActivity(intent);
    }
}
