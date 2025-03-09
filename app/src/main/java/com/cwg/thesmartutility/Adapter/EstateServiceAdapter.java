package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.estateAdmin.EstateServiceFeeDetail;
import com.cwg.thesmartutility.model.EstateServiceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EstateServiceAdapter extends RecyclerView.Adapter<EstateServiceAdapter.PostHolder>{
    Context context;
    ArrayList<EstateServiceModel> estateServiceList;
    public EstateServiceAdapter(Context context, ArrayList<EstateServiceModel> estateServiceList) {
        this.context = context;
        this.estateServiceList = estateServiceList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the layout
        View mView = LayoutInflater.from(context).inflate(R.layout.estate_service_card, parent, false);
        // call the postHolder function to pass in the view
        return new EstateServiceAdapter.PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        EstateServiceModel estateServiceModel = estateServiceList.get(position);
        // i changed to meterID instead of the ref as it plays no significance
        holder.setRef(estateServiceModel.getServiceMeter());
        holder.setServiceAmount("₦" + estateServiceModel.getServiceAmount());
        holder.setServiceDate(estateServiceModel.getServicePaymentDate());
        holder.setServiceExpiry(estateServiceModel.getServiceExpiryDate());
        holder.serviceRelative.setOnClickListener(v -> {
             Intent intent = new Intent(context, EstateServiceFeeDetail.class);
             intent.putExtra("serviceRefID", estateServiceModel.getServiceID());
             context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return estateServiceList.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        TextView mRef, mAmount, mDate, mExpiry;
        RelativeLayout serviceRelative;
        View view;

        public PostHolder(View itemView) {
            super(itemView);
            view = itemView;
            serviceRelative = view.findViewById(R.id.estateServiceRelative);
        }

        public void setRef(String Ref) {
            mRef = view.findViewById(R.id.estateServiceID);
            mRef.setText(Ref);
        }

        public void setServiceAmount(String Amount) {
            mAmount = view.findViewById(R.id.serviceAmount);
            mAmount.setText(Amount);
        }

        public void setServiceDate(String dateStr) {
            mDate = view.findViewById(R.id.servicePaymentDate);
            mDate.setText(dateStr);
        }

        public void setServiceExpiry(String Expiry) {
            mExpiry = view.findViewById(R.id.serviceExpiryDate);
            mExpiry.setText(Expiry);
        }
    }
}
