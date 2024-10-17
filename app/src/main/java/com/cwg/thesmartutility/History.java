package com.cwg.thesmartutility;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class History extends AppCompatActivity {
    BottomNavigationView userHistoryDash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userHistoryDash = findViewById(R.id.userHistoryNav);
        userHistoryDash.setSelectedItemId(R.id.historyIcon);

        userHistoryDash.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homeIcon){
                startActivity(new Intent(this, UserDashboard.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemID == R.id.historyIcon) {
                return true;
            } else if (itemID == R.id.profileIcon) {
                startActivity(new Intent(this, UserProfile.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }
}