package com.cwg.thesmartutility.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chaos.view.BuildConfig;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.user.UserDashboard;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class Login extends AppCompatActivity {

    TextView forgotPassword, signUpText, resendVerification;
    Button loginButton;
    TextInputEditText emailPhoneInput, passwordInputText;
    TextInputLayout emailPhoneLayout, passwordLayout;
    String EmailPhone, Pass, baseUrl;
    SharedPreferences validSharedPref;
    SharedPreferences.Editor prefEditor;
    private PreloaderLogo preloaderLogo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        forgotPassword = findViewById(R.id.forgotText);
//        resendVerification = findViewById(R.id.resendText);
        signUpText = findViewById(R.id.createAccount);
        loginButton = findViewById(R.id.loginButton);
        emailPhoneInput = findViewById(R.id.loginEmailInput);
        passwordInputText = findViewById(R.id.loginPasswordInput);
        emailPhoneLayout = findViewById(R.id.loginEmailInputLayout);
        passwordLayout = findViewById(R.id.loginPasswordInputLayout);

        preloaderLogo = new PreloaderLogo(this);

        //requestQueue = VolleySingleton.getmInstance(getApplicationContext()).getRequestQueue();
        // declare pref
        validSharedPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        // declare editor to edit
        prefEditor = validSharedPref.edit();

        // resend verification
//        resendVerification.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ResendVerificationLink.class);
//            startActivity(intent);
//        });

        forgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPassword.class);
            startActivity(intent);
        });

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(this, ValidateMeter.class);
            intent.putExtra("walkThroughLogin", "fromLogin");
            startActivity(intent);
        });

        // check for updates
        checkUpdate();

        // login
        loginButton.setOnClickListener(v -> init());

        // on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Navigate to the login activity
                finishAffinity();
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }

    private void init() {
        EmailPhone = Objects.requireNonNull(emailPhoneInput.getText()).toString().trim();
        Pass = Objects.requireNonNull(passwordInputText.getText()).toString().trim();
        if (EmailPhone.isEmpty() || Pass.isEmpty()) {
            checkFields(emailPhoneInput, emailPhoneLayout);
            checkFields(passwordInputText, passwordLayout);
        } else {
            preloaderLogo.show();
            login(EmailPhone, Pass);
        }
    }

    private void login(String emailPhone, String password) {
        //String loginUrl = "http://41.78.157.215:4173/auth/login";
        String loginUrl = baseUrl+"/auth/login";
        Log.d("the Url" , "came to the url");
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("emailPhone", emailPhone);
                jsonObject.put("password", password);
            }catch (JSONException e){
                preloaderLogo.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            Log.d("Looking" , "came to the request");
            JsonObjectRequest loginRequest = new JsonObjectRequest(Request.Method.POST, loginUrl, jsonObject, response -> {
                Log.d("the response" , "came to the response");
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){

                        String token = response.getString("token");
                        JSONObject userObject = response.getJSONObject("user");
                        String userid = userObject.getString("userid");
                        String username = userObject.getString("userName");
                        String email = userObject.getString("email");
                        String phone = userObject.getString("phone");
                        String meterID = userObject.getString("meterID");
                        int estateID = userObject.getInt("estateID");
                        String tariff = userObject.getString("tariff");
                        String vat = userObject.getString("vat");
                        String role = userObject.getString("role");
                        String brand = userObject.getString("brand");
                        String vendStatus = userObject.getString("vendStatus");
                        String customerID = userObject.getString("customerID");
                        String estateName = userObject.getString("estateName");
                        String chintUsername = userObject.getString("chintUsername");
                        String chintPassword = userObject.getString("chintPassword");
                        String gatewayStatus = userObject.getString("gatewayStatus");
                        String eVendStatus = userObject.getString("eVendStatus");
                        String lastLogin = userObject.getString("lastLogin");
                        String dateCreated = userObject.getString("dateCreated");
                        String hasPayAcct = userObject.getString("hasPayAcct");
                        String service_status = userObject.getString("service_status");
                        String min_amount = userObject.getString("min_amount");

                        //write to pref
                        prefEditor.putString("token", token);
                        prefEditor.putString("userid", userid);
                        prefEditor.putString("username", username);
                        prefEditor.putString("email", email);
                        prefEditor.putString("phone", phone);
                        prefEditor.putString("meterID", meterID);
                        prefEditor.putInt("estateID", estateID);
                        prefEditor.putString("customerID", customerID);
                        prefEditor.putString("tariff", tariff);
                        prefEditor.putString("vat", vat);
                        prefEditor.putString("role", role);
                        prefEditor.putString("brand", brand);
                        prefEditor.putString("vendStatus", vendStatus);
                        prefEditor.putString("estateName", estateName);
                        prefEditor.putString("chintUsername", chintUsername);
                        prefEditor.putString("chintPassword", chintPassword);
                        prefEditor.putString("gatewayStatus", gatewayStatus);
                        prefEditor.putString("eVendStatus", eVendStatus);
                        prefEditor.putString("lastLogin", lastLogin);
                        prefEditor.putString("dateCreated", dateCreated);
                        prefEditor.putString("hasPayAcct", hasPayAcct);
                        prefEditor.putString("service_status", service_status);
                        prefEditor.putString("min_amount", min_amount);


                        // to know if the user is signed in already
                        prefEditor.putBoolean("isLoggedIn", true);

                        prefEditor.apply();
                        preloaderLogo.dismiss();
//                        progressBar.setVisibility(View.GONE);
//                        logoPreloader.clearAnimation();

                        // check what the role is, either user, admin, super
                        if (role.equals("user")){
                            // go to userDashBoard
                            Intent intent = new Intent(this, UserDashboard.class);
                            startActivity(intent);
                            finish();
                        } else if (role.equals("admin")) {
                            Intent estateIntent = new Intent(this, EstateDashboard.class);
                            startActivity(estateIntent);
                            finish();
                        } else {
                            Toast.makeText(this, "Super Admin", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                        emailPhoneLayout.setErrorEnabled(true);
                        emailPhoneLayout.setError("Check your email address.");
                        passwordLayout.setErrorEnabled(true);
                        passwordLayout.setError("Check your password");
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Could not connect to server", Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                // check for error status code
                if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                    emailPhoneLayout.setErrorEnabled(true);
                    emailPhoneLayout.setError("Check your email address.");
                    passwordLayout.setErrorEnabled(true);
                    passwordLayout.setError("Check your password");
                } else if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                    showAlertDialog(false, "Verification Failed", "Kindly check your email and click the verification link.\n If you didn't get a link, kindly click the button.", "Resend", null);
                }else {
                    emailPhoneLayout.setErrorEnabled(true);
                    emailPhoneLayout.setError("Email not found.");
                }
            });
            Log.d("the request" , "came to the queue");
            int socketTimeout = 20000;  // 20 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            loginRequest.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(loginRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void checkFields(TextInputEditText editText, TextInputLayout layout) {
        String text = Objects.requireNonNull(editText.getText()).toString().trim();
        if (text.isEmpty()) {
            // Set error message on the TextInputLayout if field is empty
            layout.setErrorEnabled(true);
            layout.setError("This field cannot be empty");
        } else {
            // Clear the error if not empty
            layout.setError(null);
        }
    }

    public void showAlertDialog(boolean isSuccess, String theTitle, String theMessage, String theButton, String theUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.success_failed_alert, null);
        builder.setView(dialogView);


        // Get the Ids of the components
        ImageView statusIcon = dialogView.findViewById(R.id.dialog_status_icon);
        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button actionButton = dialogView.findViewById(R.id.dialog_action_button);

        // set the dialog properties based on success or failed
        if (!isSuccess) {
            builder.setCancelable(true);
            statusIcon.setImageResource(R.drawable.failed_icon);
            title.setText(theTitle);
            message.setText(theMessage);
            actionButton.setText(theButton);
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.primary));
        } else {
            builder.setCancelable(false);
            statusIcon.setImageResource(R.drawable.warning_icon);
            title.setText(theTitle);
            message.setText(theMessage);
            actionButton.setText(theButton);
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.primary));
        }

        // show the dialog
        AlertDialog dialog = builder.create();
        actionButton.setOnClickListener(v -> {
            if (isSuccess) {
                //dialog.dismiss();
                // go download the update with the url
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(theUrl)));
            } else {
                dialog.dismiss();
                // send the verification link
                startActivity(new Intent(Login.this, ResendVerificationLink.class));
            }
        });

        dialog.show();
    }

    // check for Update
    private void checkUpdate(){
        String checkUrl = baseUrl+"/g/getVersion";
        JsonObjectRequest checkRequest = new JsonObjectRequest(Request.Method.GET, checkUrl, null, response -> {
            try {
                String message = response.getString("message");
                if (message.equals("success")){
                    JSONObject data = response.getJSONObject("data");
                    String latestVersion = data.getString("version_name");
                    String latestURL = data.getString("update_url");
                    String text = data.getString("text");
                    String currentVersion = "";
                    // log the version code
                    // Log.d("Version Code", "Version Code: " + BuildConfig.VERSION_NAME);
                    try {
                        PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        currentVersion = packageInfo.versionName;
                        Log.d("Version Code", "Version Code: " + currentVersion);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (!currentVersion.equals(latestVersion)){
                        // update
                        showAlertDialog(true, "Update Available", text, "Update Now", latestURL);
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            // Toast.makeText(this, "No updates" , Toast.LENGTH_SHORT).show();
        });
        VolleySingleton.getInstance(this).addToRequestQueue(checkRequest);
    }

}