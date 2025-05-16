package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.model.EditMeterModel;

import java.util.List;

public class EditMeterAdapter extends ArrayAdapter<EditMeterModel> {
    private List<EditMeterModel> editMeterModelList;
    private Context context;
    private int layout;

    public EditMeterAdapter(Context context, List<EditMeterModel> editMeterModelList) {
        super(context, 0, editMeterModelList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        // Inflate the view only if it's null to optimize performance
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.edit_meter_card, parent, false);
        }

        // Find TextView and set the item name
        TextView textViewOne = convertView.findViewById(R.id.textOne);
        TextView textViewTwo = convertView.findViewById(R.id.textTwo);
        EditMeterModel editMeterModel = getItem(position);

        if (editMeterModel != null) {
            textViewOne.setText(editMeterModel.getTextOne());
            textViewTwo.setText(editMeterModel.getTextTwo());
        }

        return convertView;
    }
}
