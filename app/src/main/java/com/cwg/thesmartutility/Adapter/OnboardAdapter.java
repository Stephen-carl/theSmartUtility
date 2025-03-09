package com.cwg.thesmartutility.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardAdapter extends RecyclerView.Adapter<OnboardAdapter.OnboardingViewHolder>{
    private final Context context;
    private final int[] layouts;

    public OnboardAdapter(Context context, int[] layouts) {
        this.context = context;
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layouts[viewType], parent, false);
        if (viewType == layouts.length - 1) {
            view.setTag("lastScreen");
        } else {
            view.setTag("firstScreen");
        }
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }



    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        OnboardingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
