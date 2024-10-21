package com.cwg.thesmartutility.user;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cwg.thesmartutility.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfile extends AppCompatActivity {
    BottomNavigationView userProfileBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0,0);
            return insets;
        });

        userProfileBottom = findViewById(R.id.userProfileNav);
        userProfileBottom.setSelectedItemId(R.id.profileIcon);

        userProfileBottom.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homeIcon){
                startActivity(new Intent(this, UserDashboard.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemID == R.id.historyIcon) {
                startActivity(new Intent(this, History.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemID == R.id.profileIcon) {
                return true;
            }
            return false;
        });
    }
}