package com.cwg.thesmartutility.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.SettingsProfile;
import com.cwg.thesmartutility.auth.UpdatePassword;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.estateAdmin.EstateHistory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserProfile extends AppCompatActivity {
    BottomNavigationView userProfileBottom;
    RelativeLayout profileRelative, passwordRelative, logoutRelative;
    TextView nameText, emailText;
    SharedPreferences validPref;
    String email, name, role;
    ImageView back;

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

        // ids
        profileRelative = findViewById(R.id.profileRelative);
        passwordRelative = findViewById(R.id.passwordRelative);
        logoutRelative = findViewById(R.id.logoutRelative);
        nameText = findViewById(R.id.settingNameText);
        emailText = findViewById(R.id.settingEmailText);
        back = findViewById(R.id.settingBack);

        // sharedPref
        validPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        email = validPref.getString("email", "");
        name = validPref.getString("username", "");
        role = validPref.getString("role", "");

        nameText.setText(name);
        emailText.setText(email);

        // go to settings profile
        profileRelative.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsProfile.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // go to change password
        passwordRelative.setOnClickListener(v -> {
                    startActivity(new Intent(this, UpdatePassword.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // go to logout
        logoutRelative.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();

        });

        // back pressed
        back.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboard.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        // Nav bar
        userProfileBottom = findViewById(R.id.userProfileNav);
        userProfileBottom.setSelectedItemId(R.id.profileIcon);

        userProfileBottom.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homeIcon){
                if (role.equals("user")) {
                    startActivity(new Intent(this, UserDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if (role.equals("admin")) {
                    startActivity(new Intent(this, EstateDashboard.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                return true;
            } else if (itemID == R.id.historyIcon) {
                if (role.equals("user")) {
                    startActivity(new Intent(this, History.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if (role.equals("admin")) {
                    startActivity(new Intent(this, EstateHistory.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                return true;
            } else if (itemID == R.id.profileIcon) {
                return true;
            }
            return false;
        });
    }
}