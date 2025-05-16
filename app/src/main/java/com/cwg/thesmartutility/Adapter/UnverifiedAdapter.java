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
import com.cwg.thesmartutility.UnverifiedPayment;
import com.cwg.thesmartutility.model.UnverifiedModel;
import com.cwg.thesmartutility.user.VerifyingPay;

import java.util.ArrayList;

public class UnverifiedAdapter extends RecyclerView.Adapter<UnverifiedAdapter.PostHolder>{
    private Context context;
    private ArrayList<UnverifiedModel> unverifiedList;

    public UnverifiedAdapter(Context context, ArrayList<UnverifiedModel> unverifiedList) {
        this.context = context;
        this.unverifiedList = unverifiedList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.unverified_card, parent, false);
        //call the postHolder function to pass in the view
        return new UnverifiedAdapter.PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        UnverifiedModel unverifiedModel = unverifiedList.get(position);
        holder.setAmount(unverifiedModel.getAmount());
        holder.setRef(unverifiedModel.getTrans_ref());
        holder.mButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, VerifyingPay.class);
            intent.putExtra("purAmount", unverifiedModel.getAmount());
            intent.putExtra("ref", unverifiedModel.getTrans_ref());
            intent.putExtra("customerID", unverifiedModel.getCustomerID());
            intent.putExtra("brand", unverifiedModel.getBrand());
            intent.putExtra("pay_id", unverifiedModel.getPay_id());
            intent.putExtra("payment_type", unverifiedModel.getPayment_type());
            intent.putExtra("duration", unverifiedModel.getDuration());
            context.startActivity(intent);
            // close activity
            ((UnverifiedPayment) context).finish();
        });
    }

    @Override
    public int getItemCount() {
        return unverifiedList.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        //the textViews to use
        TextView mAmount, mRef;
        View view;
        RelativeLayout mButton;

        public PostHolder(View itemView) {
            super(itemView);
            view = itemView;
            mButton = view.findViewById(R.id.verifyButton);
        }

        //functions for setting the items and set the text
        public void setAmount(String Amount) {
            mAmount = view.findViewById(R.id.verifyAmountText);
            mAmount.setText(Amount);
        }
        public void setRef(String Ref) {
            mRef = view.findViewById(R.id.verifyRefText);
            mRef.setText(Ref);
        }
    }
}
