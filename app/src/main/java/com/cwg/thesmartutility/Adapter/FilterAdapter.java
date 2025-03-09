package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.model.FilterModel;

import java.util.ArrayList;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.PostHolder>{
    private ArrayList<FilterModel> filterList;

    private Context context;

    private MeterSelectionListener listener;

    public interface MeterSelectionListener {
        void onMeterSelected(FilterModel meter);
    }

    public FilterAdapter(ArrayList<FilterModel> filterList, Context context, MeterSelectionListener listener) {
        this.filterList = filterList;
        this.context = context;
        this.listener = listener;
    }


    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(context).inflate(R.layout.search_bottom_card, parent, false);
        return new FilterAdapter.PostHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {
        FilterModel filterModel = filterList.get(position);
        holder.setMeterName(filterModel.getMeterName());
        holder.setMeterNum(filterModel.getMeterNumber());
        // add the block and flat no
        holder.setBlockNo(filterModel.getBlockNo());
        holder.setFlatNo(filterModel.getFlatNo());
        // select the meter number of the item clicked
        holder.itemView.setOnClickListener(v -> listener.onMeterSelected(filterModel));
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    // update the meterList
    public void updateData(ArrayList<FilterModel> newMeterList) {
        filterList = newMeterList;
        notifyDataSetChanged();
    }


    public static class PostHolder extends RecyclerView.ViewHolder {
        //the textViews to use
        TextView mMeter, mName, mBlockNo, mFlatNo;
        View view;

        public PostHolder( View itemView) {
            super(itemView);
            //pass the view(context) to this one i defined
            view = itemView;
        }
        //functions for setting the items and set the text
        public void setMeterName(String MeterName) {
            mName = view.findViewById(R.id.resultMeterName);
            mName.setText(MeterName);
        }

        // work on this
        public void setMeterNum(String MeterID) {
            mMeter = view.findViewById(R.id.resultNameMeter);
            mMeter.setText(MeterID);
        }

        public void setBlockNo(String BlockNo) {
            mBlockNo = view.findViewById(R.id.resultBlockNo);
            mBlockNo.setText(BlockNo);
        }

        public void setFlatNo(String FlatNo) {
            mFlatNo = view.findViewById(R.id.resultFlatNo);
            mFlatNo.setText(FlatNo);
        }
    }
}
