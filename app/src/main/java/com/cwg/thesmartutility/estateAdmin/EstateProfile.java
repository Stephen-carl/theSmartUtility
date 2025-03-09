package com.cwg.thesmartutility.estateAdmin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.SettingsProfile;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.auth.UpdatePassword;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EstateProfile extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    RelativeLayout profileRelative, passwordRelative, logoutRelative;
    TextView nameText, emailText;
    SharedPreferences validPref;
    String email, name;
    private boolean isBackPressed = false;
    ImageView settingsBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.estate_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // ids
        profileRelative = findViewById(R.id.estateProfileRelative);
        passwordRelative = findViewById(R.id.estatePasswordRelative);
        logoutRelative = findViewById(R.id.estateLogoutRelative);
        nameText = findViewById(R.id.settingsNameText);
        emailText = findViewById(R.id.settingsEmailText);
        settingsBack = findViewById(R.id.settingsBack);

        // get the email from the utility pref
        validPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        email = validPref.getString("email", "");
        name = validPref.getString("username", "");

        // SET THE NAME AND EMAIL
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

        // go back
        settingsBack.setOnClickListener(v -> {
            startActivity(new Intent(this, EstateDashboard.class));
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // nav bar
        // bottom Nav
        bottomNavigationView = findViewById(R.id.estateProfileNav);
        bottomNavigationView.setSelectedItemId(R.id.estateProfileMenu);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.estateHomeMenu){
                startActivity(new Intent(this, EstateDashboard.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateHistoryMenu) {
                startActivity(new Intent(this, EstateHistory.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateMeterMenu) {
                startActivity(new Intent(this, EstateMeters.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (itemID == R.id.estateProfileMenu) {
                return true;
            }
            return false;
        });

        // on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the login activity
                if (isBackPressed) {
                    // Navigate to the login activity only on second press
                    Intent intent = new Intent(EstateProfile.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
                    startActivity(intent);
                    finish();
                } else {
                    // Set flag and show Toast on first press
                    isBackPressed = true;
                    Toast.makeText(EstateProfile.this, "Press again to log out.", Toast.LENGTH_SHORT).show();

                    // Reset the flag after 2 seconds
                    new Handler().postDelayed(() -> isBackPressed = false, 2000);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }
}