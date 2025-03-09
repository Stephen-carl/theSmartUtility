package com.cwg.thesmartutility.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OnBoardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<View> onboardingScreens;

    public OnBoardingAdapter(List<View> onboardingScreens) {
        this.onboardingScreens = onboardingScreens;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(onboardingScreens.get(viewType)) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return onboardingScreens.size();
    }
}
