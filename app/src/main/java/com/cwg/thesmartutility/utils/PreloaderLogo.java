package com.cwg.thesmartutility.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.cwg.thesmartutility.R;

import java.util.Objects;

public class PreloaderLogo {
    private Dialog dialog;
    private Animation rotateAnimation;
    ImageView preloadLogo;

    public PreloaderLogo(Activity activity){
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.preloader_activity);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        preloadLogo = dialog.findViewById(R.id.logoPreloader);
        rotateAnimation = AnimationUtils.loadAnimation(activity, R.anim.rotate);
        preloadLogo.startAnimation(rotateAnimation);
    }

    public void show(){
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss(){
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
