package com.cwg.thesmartutility;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cwg.thesmartutility.auth.Login;

public class AppSplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1500;
    SharedPreferences validSharedPref;
    boolean isLoggedIn, isRegistered;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.app_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        setContentView(R.layout.splash_one);

        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        isLoggedIn = validSharedPref.getBoolean("isLoggedIn", false);
        isRegistered = validSharedPref.getBoolean("isRegistered", false);

        // Show the white background for 1.5 seconds, then switch to the blue background
        new Handler().postDelayed(() -> {
            setContentView(R.layout.splash_two);

            // Show the blue background for another 1.5 seconds, then move to walkthrough
            new Handler().postDelayed(() -> {
                // check if isLoggedIn is true then go to login else go to walkThrough
                if (isLoggedIn||isRegistered){
                    startActivity(new Intent(AppSplashScreen.this, Login.class));
                    finish();
                } else {
                    startActivity(new Intent(AppSplashScreen.this, WalkThrough.class));
                    finish();
                }

            }, SPLASH_DELAY);

        }, SPLASH_DELAY);
    }
}