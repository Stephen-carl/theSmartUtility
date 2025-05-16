package com.cwg.thesmartutility.estateAdmin;

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

public class UpdateVAT extends AppCompatActivity {
    ImageView updateVATBack;
    TextInputEditText vatAmountInput;
    TextInputLayout vatAmountLayout;
    Button vatUpdateButton;
    SharedPreferences validPref;
    SharedPreferences.Editor prefEditor;
    String VATAmount, token, baseUrl;
    int estateID;
    private PreloaderLogo preloaderLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.update_vat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        baseUrl = this.getString(R.string.managementBaseURL);

        // ids
        updateVATBack = findViewById(R.id.updateVATBack);
        vatAmountInput = findViewById(R.id.vatAmountInput);
        vatAmountLayout = findViewById(R.id.vatAmountLayout);
        vatUpdateButton = findViewById(R.id.vatUpdateButton);
        preloaderLogo = new PreloaderLogo(this);

        // declare pref
        validPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);
        token = validPref.getString("token", "");
        // declare editor to edit
        prefEditor = validPref.edit();

        // listeners
        updateVATBack.setOnClickListener(v -> finish());

        vatUpdateButton.setOnClickListener(v -> init());

    }

    public void init() {
        // get the input and check if empty
        VATAmount = Objects.requireNonNull(vatAmountInput.getText()).toString().trim();
        if (VATAmount.isEmpty()) {
            // set the layout error
            vatAmountLayout.setErrorEnabled(true);
            vatAmountLayout.setError("This field cannot be empty");
        } else {

            updateVAT(VATAmount);
        }
    }

    public void updateVAT(String vat) {
        preloaderLogo.show();
        vatUpdateButton.setEnabled(false);
        // update the vat
        estateID = validPref.getInt("estateID", 0);
        String updateVATUrl = baseUrl+"/estate/updatevat";
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("estateID", estateID);
                jsonObject.put("vat", vat);
            } catch (JSONException e) {
                preloaderLogo.dismiss();
                vatUpdateButton.setEnabled(true);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            JsonObjectRequest updateVATRequest = new JsonObjectRequest(Request.Method.POST, updateVATUrl, jsonObject, response -> {
                try {
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        // get the new vat and save in the pref
                        String newVAT = response.getString("vat");
                        prefEditor.putString("vat", newVAT);
                        prefEditor.apply();
                        preloaderLogo.dismiss();
                        vatUpdateButton.setEnabled(true);
                        showAlertDialog(true);
//                        Toast.makeText(this, "VAT updated successfully", Toast.LENGTH_SHORT).show();
//                        finish();
                    } else {
                        preloaderLogo.dismiss();
                        vatUpdateButton.setEnabled(true);
                        showAlertDialog(false);
                    }
                } catch (JSONException e) {
                    preloaderLogo.dismiss();
                    vatUpdateButton.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                preloaderLogo.dismiss();
                vatUpdateButton.setEnabled(true);
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);  // Send JWT in Authorization header
                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(updateVATRequest);
        } catch (Exception e) {
            preloaderLogo.dismiss();
            vatUpdateButton.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            // update the background tint and the text color
            actionButton.setTextColor(getResources().getColor(R.color.white));

            actionButton.setBackgroundColor(getResources().getColor(R.color.primary));
        } else {
            statusIcon.setImageResource(R.drawable.failed_icon);
            title.setText("VAT Update Failed");
            message.setText("We couldn’t update the VAT rate.\n Verify the information and your network, then try again.");
            actionButton.setText("OK");
            actionButton.setTextColor(getResources().getColor(R.color.white));
            actionButton.setBackgroundColor(getResources().getColor(R.color.red));
        }

        // show the dialog
        AlertDialog dialog = builder.create();
        actionButton.setOnClickListener(v -> {
            if (isSuccess) {
                dialog.dismiss();
                // go to the estateDashboard
                startActivity(new Intent(UpdateVAT.this, EstateDashboard.class));
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}