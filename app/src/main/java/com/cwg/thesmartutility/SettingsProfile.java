package com.cwg.thesmartutility;

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

public class SettingsProfile extends AppCompatActivity {

    RelativeLayout nameRelative, emailRelative, phoneRelative, roleRelative, joinRelative, lastRelative;
    TextView nameText, emailText, phoneText, roleText, joinText, lastText;
    SharedPreferences validPref;
    String name, email, phone, role, join, last;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.settings_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // ids
        nameRelative = findViewById(R.id.profileNameRelative);
        emailRelative = findViewById(R.id.profileEmailRelative);
        phoneRelative = findViewById(R.id.profilePhoneRelative);
        roleRelative = findViewById(R.id.profileRoleRelative);
        joinRelative = findViewById(R.id.profileJoinRelative);
        lastRelative = findViewById(R.id.profileLastRelative);
        nameText = findViewById(R.id.profileNameText);
        emailText = findViewById(R.id.profileEmailText);
        phoneText = findViewById(R.id.profilePhoneText);
        roleText = findViewById(R.id.profileRoleText);
        joinText = findViewById(R.id.profileJoinText);
        lastText = findViewById(R.id.profileLastText);
        imageView = findViewById(R.id.profileBackImage);

        // go back to profile
        imageView.setOnClickListener(v -> finish());

        // get the strings from sharedPref
        validPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        name = validPref.getString("username", "");
        email = validPref.getString("email", "");
        phone = validPref.getString("phone", "");
        role = validPref.getString("role", "");
        join = validPref.getString("dateCreated", "");
        last = validPref.getString("lastLogin", "");

        // set the text to the textViews
        nameText.setText(name);
        emailText.setText(email);
        phoneText.setText(phone);
        // check if role is admin or user
        if (role.equals("admin")) {
            roleText.setText("Estate Admin");
        } else {
            roleText.setText("User");
        }
        joinText.setText(join);
        lastText.setText(last);

    }
}