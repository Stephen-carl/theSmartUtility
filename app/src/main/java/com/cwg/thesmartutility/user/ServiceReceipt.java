package com.cwg.thesmartutility.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.cwg.thesmartutility.R;
import com.cwg.thesmartutility.TheReceipt;
import com.cwg.thesmartutility.VolleySingleton;
import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.utils.PreloaderLogo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServiceReceipt extends AppCompatActivity {

    private TextView serviceTransID, serviceMeterNum, serviceAmount, servicePayDate, serviceExpiryDate, serviceHeadAmount;
    String serviceRefID, baseUrl, token;
    private ImageView shareIcon, emailIcon;
    SharedPreferences validPref;
    PreloaderLogo preloaderLogo;
    String TransText, MeterText, AmountText, PayDateText, ExpiryDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.service_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ids
        serviceTransID = findViewById(R.id.serviceTransID);
        serviceMeterNum = findViewById(R.id.serviceMeterNum);
        serviceAmount = findViewById(R.id.serviceAmount);
        servicePayDate = findViewById(R.id.servicePayDate);
        serviceExpiryDate = findViewById(R.id.serviceExpiryDate);
        serviceHeadAmount = findViewById(R.id.serviceAmountText);
        shareIcon = findViewById(R.id.shareIcon);
        emailIcon = findViewById(R.id.emailIcon);

        // preloader
        preloaderLogo = new PreloaderLogo(this);

        // get the service ref id from the intent
        serviceRefID = getIntent().getStringExtra("serviceRefID");

        // base url
        baseUrl = getString(R.string.managementBaseURL);

        // get token from shared preferences
        validPref = getSharedPreferences("UtilityPref", Context.MODE_PRIVATE);
        token = validPref.getString("token", "");

        // fetch service
        fetchServiceDetails();

        // share button
        shareIcon.setOnClickListener(v -> createPDF());

        // email icon
        emailIcon.setOnClickListener(v -> {
            sendMail();
        });

        // fix the on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String role = validPref.getString("role", null);
                assert role != null;
                if (role.equals("user")) {
                    Intent in = new Intent(ServiceReceipt.this, UserDashboard.class);
                    startActivity(in);
                    finish();
                } else if (role.equals("admin")) {
                    Intent in = new Intent(ServiceReceipt.this, EstateDashboard.class);
                    startActivity(in);
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }

    // fetch the service details from the database using the service ref id
    private void fetchServiceDetails() {
        preloaderLogo.show();
        String fetchURl = baseUrl+"/user/oneservicetrans/?refID="+serviceRefID;
        try {
            JsonObjectRequest fetchService = new JsonObjectRequest(Request.Method.GET, fetchURl, null, response -> {
                try {
                    preloaderLogo.dismiss();
                    String message = response.getString("message");
                    if (message.equals("success")) {
                        JSONObject data = response.getJSONObject("data");
                        TransText = data.getString("subscription_id");
                        MeterText = data.getString("meterID");
                        AmountText = data.getString("sub_amount");
                        PayDateText = data.getString("payment_date");
                        ExpiryDateText = data.getString("expiry_date");

                        // set the text views
                        serviceTransID.setText(TransText);
                        serviceMeterNum.setText(MeterText);
                        serviceAmount.setText("NGN "+ AmountText);
                        serviceHeadAmount.setText("NGN "+ AmountText);
                        servicePayDate.setText(PayDateText);
                        serviceExpiryDate.setText(ExpiryDateText);
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }, error -> {
                preloaderLogo.dismiss();
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            // Set the RetryPolicy here
            int socketTimeout = 10000;  // 10 seconds
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            fetchService.setRetryPolicy(policy);
            VolleySingleton.getInstance(this).addToRequestQueue(fetchService);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // to create PDF and share receipt
    private void createPDF() {
        // Create a new PdfDocument instance
        PdfDocument document = new PdfDocument();

        View view = getLayoutInflater().inflate(R.layout.share_service_receipt, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Objects.requireNonNull(this.getDisplay()).getRealMetrics(displayMetrics);
        } else this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        TextView refItem, meterItem, amountItem, payDateItem, endDateItem, receiptAmountText;
        refItem = view.findViewById(R.id.shareTransID);
        meterItem = view.findViewById(R.id.shareMeterNum);
        amountItem = view.findViewById(R.id.shareAmount);
        receiptAmountText = view.findViewById(R.id.shareAmountText);
        payDateItem = view.findViewById(R.id.sharePayDate);
        endDateItem = view.findViewById(R.id.shareExpiryDate);



        refItem.setText(TransText);
        meterItem.setText(MeterText);
        amountItem.setText(String.format("₦ %s", AmountText));
        receiptAmountText.setText(String.format("NGN %s", AmountText));
        payDateItem.setText(PayDateText);
        endDateItem.setText(ExpiryDateText);

        //this helps  to get the phone's width and height, instead of defining it
        view .measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY));

        Log.d("my log", "Width Now" + view.getMeasuredWidth());
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);

        // Obtain the width and height of the view
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();

        // Create a PageInfo object and specify the page attributes
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();

        // Start a new page
        PdfDocument.Page page = document.startPage(pageInfo);
        // Get the Canvas object to draw on the page
        Canvas canvas = page.getCanvas();
        // Create a Paint object for styling the view
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        // Draw the view on the canvas
        view.draw(canvas);
        // Finish the page
        document.finishPage(page);

        // Specify the path and filename of the output PDF file
        String receiptFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/TransactionReports/";
        File directory = new File(receiptFile);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        //here, i am passing the dataTime  of the transaction, so that the users can easily know which is which.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime currentTiming = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String formatDate = currentTiming.format(formatter);
            String fileName = "ServiceFeeReceipt_" + formatDate +".pdf";
            File filePath = new File(receiptFile, fileName);
            try {
                // Save the document to a file
                FileOutputStream fos = new FileOutputStream(filePath);
                document.writeTo(fos);
                document.close();
                fos.close();
                // PDF conversion successful
                Toast.makeText(this, "Saved Successful", Toast.LENGTH_LONG).show();

                // now share the receipt
                sharePDF(filePath);
                // go back to the respective dashboard

            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        //close pdf stream
        document.close();
    }

    // share pdf
    // Function to share the generated PDF
    private void sharePDF(File file) {
        // Create a Uri for the PDF file
        Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

        // Create an Intent to share the file
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

        // Grant temporary read permission to the intent receiver
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Launch the share intent
        startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
    }

    private void sendMail() {
        preloaderLogo.show();
        String mailURL = baseUrl + "/g/sendTransMail/?serviceID="+TransText;
        String token = validPref.getString("token", "");
        JsonObjectRequest mailRequest = new JsonObjectRequest(Request.Method.GET, mailURL, null, response -> {
            try {
                preloaderLogo.dismiss();
                String message = response.getString("message");
                if (message.equals("success")) {
                    Toast.makeText(this, "Sent Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Couldn't send to email", Toast.LENGTH_SHORT).show();
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
        // Set the RetryPolicy here
        int socketTimeout = 10000;  // 10 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        mailRequest.setRetryPolicy(policy);
        VolleySingleton.getInstance(this).addToRequestQueue(mailRequest);
    }
}