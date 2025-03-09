package com.cwg.thesmartutility.estateAdmin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.utils.PreloaderLogo;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateTariff extends AppCompatActivity {

    TextInputEditText tariffAMountInput;
    TextInputLayout tariffInputLayout;
    TextView tariff100, tariff200, tariff500, tariff1000, tariff2000, tariff3000, tariff5000, tariff10000;
    String TariffAmount, token, baseUrl;
    int estateID;
    SharedPreferences validPref;
    SharedPreferences.Editor prefEditor;
    Button tariffButton;
    ImageView tariffBack;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_tariff);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        tariffInputLayout = findViewById(R.id.tariffAmountLayout);
        tariffAMountInput = findViewById(R.id.tariffAmountInput);
        tariffButton = findViewById(R.id.tariffUpdateButton);
        tariff100 = findViewById(R.id.estateTar100);
        tariff200 = findViewById(R.id.estateTar200);
        tariff500 = findViewById(R.id.estateTar500);
        tariff1000 = findViewById(R.id.estateTar1000);
        tariff2000 = findViewById(R.id.estateTar2000);
        tariff3000 = findViewById(R.id.estateTar3000);
        tariff5000 = findViewById(R.id.estateTar5000);
        tariff10000 = findViewById(R.id.estateTar10000);
        tariffBack = findViewById(R.id.updateTariffBack);
        preloaderLogo = new PreloaderLogo(this);

        validPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        prefEditor = validPref.edit();
        estateID = validPref.getInt("estateID", 0 );
        token = validPref.getString("token", null);

        tariff100.setOnClickListener(v -> tariffAMountInput.setText("100"));
        tariff200.setOnClickListener(v -> tariffAMountInput.setText("200"));
        tariff500.setOnClickListener(v -> tariffAMountInput.setText("500"));
        tariff1000.setOnClickListener(v -> tariffAMountInput.setText("1000"));
        tariff2000.setOnClickListener(v -> tariffAMountInput.setText("2000"));
        tariff3000.setOnClickListener(v -> tariffAMountInput.setText("3000"));
        tariff5000.setOnClickListener(v -> tariffAMountInput.setText("5000"));
        tariff10000.setOnClickListener(v -> tariffAMountInput.setText("10000"));

        tariffBack.setOnClickListener(v -> finish());

        tariffButton.setOnClickListener(v -> init());
    }

    private void init() {
        TariffAmount = Objects.requireNonNull(tariffAMountInput.getText()).toString();
        if (TariffAmount.isEmpty()) {
            tariffInputLayout.setErrorEnabled(true);
            tariffInputLayout.setError("Amount is required");
        } else {
            updateTariff(TariffAmount);
        }
    }

    private void updateTariff(String TariffAmount) {
        preloaderLogo.show();
        String updateTariffURL = baseUrl+"/estate/updateTariff";
        try {
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("estateID", estateID);
                requestData.put("tariff", TariffAmount);
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest updateRequest = new JsonObjectRequest(Request.Method.POST, updateTariffURL, requestData, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")){
                        String newTariff = response.getString("tariff");
                        prefEditor.putString("tariff", newTariff);
                        prefEditor.apply();
                        preloaderLogo.dismiss();
                        showAlertDialog(true);
                    } else {
                        preloaderLogo.dismiss();
                        Toast.makeText(this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        showAlertDialog(false);
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(updateRequest);
        } catch (Exception e){
            preloaderLogo.dismiss();
            Toast.makeText(this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showAlertDialog(boolean isSuccess) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.success_failed_alert, null);
        builder.setView(dialogView);

        // Get the Ids of the components
        ImageView statusIcon = dialogView.findViewById(R.id.dialog_status_icon);
        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button actionButton = dialogView.findViewById(R.id.dialog_action_button);

        // set the dialog properties based on success or failed
        if (isSuccess) {
            statusIcon.setImageResource(R.drawable.success_circle);
            title.setText("VAT Updated Successfully");
            message.setText("The updated VAT rate has been applied\n to all transactions and will reflect in future calculations.");
            actionButton.setText("View Updated Tariff");
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.primary));
        } else {
            statusIcon.setImageResource(R.drawable.failed_icon);
            title.setText("VAT Update Failed");
            message.setText("We couldn’t update the VAT rate.\n Verify the information and your network, then try again.");
            actionButton.setText("Return Update");
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.red));
        }

        // show the dialog
        AlertDialog dialog = builder.create();
        actionButton.setOnClickListener(v -> {
            if (isSuccess) {
                dialog.dismiss();
                // go to the estateDashboard
                startActivity(new Intent(UpdateTariff.this, EstateDashboard.class));
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}