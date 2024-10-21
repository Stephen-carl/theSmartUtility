package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.model.UserTransModel;

import java.util.ArrayList;

public class UserTransAdapter extends RecyclerView.Adapter<UserTransAdapter.PostHolder> {

    Context context;
    ArrayList<UserTransModel> userTransList;

    // Constructor
    public UserTransAdapter(Context context, ArrayList<UserTransModel> userTransList) {
        this.context = context;
        this.userTransList = userTransList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //to inflate the layout to use
        View mView = LayoutInflater.from(context).inflate(R.layout.transaction_card, parent, false);
        //call the postHolder function to pass in the view
        return new PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        UserTransModel userTranModel = userTransList.get(position);
        holder.setAmountID(userTranModel.getAmount());
        holder.setUnit(userTranModel.getUnits());
        holder.setTokenID(userTranModel.getToken());
        holder.setDateID(userTranModel.getDate(), userTranModel.getTime());

    }

    @Override
    public int getItemCount() {
        return userTransList.size();
    }


    public static class PostHolder extends RecyclerView.ViewHolder {
        //the textViews to use
        TextView mAmount, mToken, mDate, mUnits;
        View view;

        public PostHolder( View itemView) {
            super(itemView);
            //pass the view(context) to this one i defined
            view = itemView;
        }
        //functions for setting the items and set the text

        public void setUnit(String Units) {
            mUnits = view.findViewById(R.id.transUnit);
            mUnits.setText(Units);
        }

        //this amount has to be the vended amount or we could leave it to be the main Amount, so at least the user can imagine what he can get
        public void setAmountID(String AmountID) {
            mAmount = view.findViewById(R.id.transAmount);
            mAmount.setText(AmountID);
        }

        public void setTokenID(String TokenID){
            mToken = view.findViewById(R.id.transToken);
            mToken.setText(TokenID);
        }

        public void setDateID(String DateID, String Time) {
            // Add the time and the date
            String dateTime = DateID + " " + Time;
            mDate = view.findViewById(R.id.transDate);
            mDate.setText(dateTime);
        }
    }
}
