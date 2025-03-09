package com.cwg.thesmartutility;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.cwg.thesmartutility.Adapter.OnboardAdapter;
import com.cwg.thesmartutility.auth.Login;
import com.cwg.thesmartutility.auth.ValidateMeter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class WalkThrough extends AppCompatActivity {

    ViewPager2 viewPager;
    Button getStartedOne, getStartedTwo;
    CardView loginCardButton, createAccountCardButton;
    ImageView signSelect, signAddPerson, logSelect, logPerson;
    //List<View> onboardingScreens;
    OnboardAdapter onboardAdapter;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    Drawable signDrawable, logdrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.walk_through);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.viewPager);
        // add the onboarding screens
        int[] layouts = {
                R.layout.onboarding_one,
                R.layout.onboarding_two
        };

//        onboardingScreens = new ArrayList<>();
//        onboardingScreens.add(getLayoutInflater().inflate(R.layout.onboarding_one, null));
//        onboardingScreens.add(getLayoutInflater().inflate(R.layout.onboarding_two, null));
//        onboardingScreens.add(getLayoutInflater().inflate(R.layout.onboarding_three, null));

        onboardAdapter = new OnboardAdapter(this, layouts);
        viewPager.setAdapter(onboardAdapter);
        //adapter = new OnBoardingAdapter(onboardingScreens);
        //viewPager.setAdapter(onboardAdapter);

        // set the navigation
        setupNavigation();

    }

    private void setupNavigation() {
        // Go to Login or validate activity on the last screen
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                // the last onboarding screen
                if (position == onboardAdapter.getItemCount() - 1) {
                    // the login and create account buttons
                    View lastScreenView = viewPager.findViewWithTag("lastScreen");
                    if (lastScreenView != null) {
                        getStartedTwo = lastScreenView.findViewById(R.id.get_started_two);
                        getStartedTwo.setOnClickListener(v -> selectBottomSheet());

                        // set onClick
//                        loginTextButton.setOnClickListener(v -> startActivity(new Intent(WalkThrough.this, Login.class)));
                        // create account goes to the validation activity
//                        createAccountButton.setOnClickListener(v -> startActivity(new Intent(WalkThrough.this, ValidateMeter.class)));
                    }
                } else {
                    // for the first screen
                    View firstScreen = viewPager.findViewWithTag("firstScreen");
                    if (firstScreen != null) {
                        getStartedOne = firstScreen.findViewById(R.id.get_started_one);
                        getStartedOne.setOnClickListener(v -> selectBottomSheet());
                    }
                }
            }
        });

    }

    // show the bottom sheet for the login and signup
    private void selectBottomSheet() {
        // show get started bottom sheet
        bottomSheetDialog = new BottomSheetDialog(WalkThrough.this);
        //inflate the bottomSheet
        bottomSheetView = getLayoutInflater().inflate(R.layout.get_started_bottom_sheet, findViewById(R.id.getStartedContainer), false);

        bottomSheetDialog.setContentView(bottomSheetView);

        signDrawable = getResources().getDrawable(R.drawable.add_user);
        logdrawable = getResources().getDrawable(R.drawable.person_icon);

        //ids for the cards
        createAccountCardButton = bottomSheetView.findViewById(R.id.signUpCard);
        loginCardButton = bottomSheetView.findViewById(R.id.loginCard);
        //ids for the images
        signSelect = bottomSheetView.findViewById(R.id.signUpToggle);
        logSelect = bottomSheetView.findViewById(R.id.loginToggle);
        signAddPerson = bottomSheetView.findViewById(R.id.addUser);
        logPerson = bottomSheetView.findViewById(R.id.personUser);

        // when both buttons are clicked take some actions, then go to the respectively areas
        createAccountCardButton.setOnClickListener(v -> {
            signSelect.setImageResource(R.drawable.toggle_on);
            logSelect.setImageResource(R.drawable.toggle_off);
            signDrawable.setTint(ContextCompat.getColor(this, R.color.primary));
            logdrawable.setTint(ContextCompat.getColor(this, R.color.black));
            signAddPerson.setImageDrawable(signDrawable);
            logPerson.setImageDrawable(logdrawable);
            createAccountCardButton.setBackgroundResource(R.drawable.button_white);
            loginCardButton.setBackgroundResource(R.drawable.button_trasparent);

            // go to validate activity
            Intent signIntent = new Intent(WalkThrough.this, ValidateMeter.class);
            signIntent.putExtra("walkThroughLogin", "from_walk_through");
            startActivity(signIntent);
            //finish();
        });

        loginCardButton.setOnClickListener(v -> {
            logSelect.setImageResource(R.drawable.toggle_on);
            signSelect.setImageResource(R.drawable.toggle_off);
            logdrawable.setTint(ContextCompat.getColor(this, R.color.primary));
            signDrawable.setTint(ContextCompat.getColor(this, R.color.black));
            logPerson.setImageDrawable(logdrawable);
            signAddPerson.setImageDrawable(signDrawable);
            loginCardButton.setBackgroundResource(R.drawable.button_white);
            createAccountCardButton.setBackgroundResource(R.drawable.button_trasparent);

            // go to login activity
            Intent logIntent = new Intent(WalkThrough.this, Login.class);
            //logIntent.putExtra("walkThroughLogin", "from_walk_through");
            startActivity(logIntent);
            finish();
        });

        bottomSheetDialog.show();
    }
}