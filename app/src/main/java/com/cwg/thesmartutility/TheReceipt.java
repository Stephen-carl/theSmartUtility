package com.cwg.thesmartutility;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cwg.thesmartutility.estateAdmin.EstateDashboard;
import com.cwg.thesmartutility.user.UserDashboard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TheReceipt extends AppCompatActivity {

    TextView transText, meterText, tokenText, amountText, chargeText, vatText, tariffText, vendedText, unitText, dateText, receiptAmount;
    String TransText, MeterText, TokenText, AmountText, ChargeText, VatText, TariffText, VendedText, UnitText, DateText, TimeText;
    ImageView shareImage;
    RelativeLayout copyRelative;
    SharedPreferences validPref;
    List<ReceiptItems> repItems;
    DecimalFormat decimalFormat = new DecimalFormat("#.00");
    Double charges, vended;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.the_receipt);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // shared pref
        validPref = getSharedPreferences("UtilityPref", MODE_PRIVATE);

        // ids
        transText = findViewById(R.id.receiptTransID);
        meterText = findViewById(R.id.receiptMeterNum);
        tokenText = findViewById(R.id.receiptToken);
        amountText = findViewById(R.id.receiptAmount);
        chargeText = findViewById(R.id.receiptTransCharge);
        vatText = findViewById(R.id.receiptVat);
        tariffText = findViewById(R.id.receiptTariff);
        vendedText = findViewById(R.id.receiptTokenValue);
        unitText = findViewById(R.id.receiptUnit);
        dateText = findViewById(R.id.receiptDate);
        shareImage = findViewById(R.id.shareIcon);
        copyRelative = findViewById(R.id.receiptTokenRelative);
        receiptAmount = findViewById(R.id.receiptAmountText);

        // GET THE INFO NEEDED
        Intent intent = getIntent();
        TransText = intent.getStringExtra("repTransID");
        MeterText = intent.getStringExtra("repMeterID");
        TokenText = intent.getStringExtra("repToken");
        AmountText = intent.getStringExtra("repAmount");
        ChargeText = intent.getStringExtra("repCharge");
        VatText = intent.getStringExtra("repVat");
        TariffText = intent.getStringExtra("repTariff");
        VendedText = intent.getStringExtra("redVendedAmount");
        UnitText = intent.getStringExtra("repUnits");
        DateText = intent.getStringExtra("repDate");
        TimeText = intent.getStringExtra("repTime");

        // make the ChargeText to be in .00 decimal format
        charges = Double.parseDouble(ChargeText);
        ChargeText = decimalFormat.format(charges);
        vended = Double.parseDouble(VendedText);
        VendedText = decimalFormat.format(vended);

        Log.d("my log", "ChargeText: " + ChargeText);

        // PUT INTO IN THE TEXT VIEWS ACCORDINGLY
        transText.setText(TransText);
        meterText.setText(MeterText);
        tokenText.setText(TokenText);
        amountText.setText(String.format("₦ %s", AmountText));
        chargeText.setText(String.format("₦ %s", ChargeText));
        vatText.setText(String.format("%s %%", VatText));
        tariffText.setText(String.format("₦ %s", TariffText));
        vendedText.setText(String.format("₦ %s", VendedText));
        unitText.setText(String.format("%s kWh", UnitText));
        dateText.setText(getCurrentTimeInDefaultTimeZone());
        receiptAmount.setText(String.format("NGN %s", AmountText));

        // make the copyRelative copy the text in its textview
        copyRelative.setOnClickListener(v -> {
            // Copy the text to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", TokenText);
            clipboard.setPrimaryClip(clip);

            // Show a confirmation message
            Toast.makeText(TheReceipt.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // WHEN THEN SHARE BUTTON IS CLICKED, CREATE  A PDF AND CALL THE ANDROID MODULE TO BRING UP THE SHARE DIALOG
        shareImage.setOnClickListener(v -> {
            createPDF();
        });

        // fix the on back pressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                String role = validPref.getString("role", null);
                assert role != null;
                if (role.equals("user")) {
                    Intent in = new Intent(TheReceipt.this, UserDashboard.class);
                    startActivity(in);
                    finish();
                } else if (role.equals("admin")) {
                    Intent in = new Intent(TheReceipt.this, EstateDashboard.class);
                    startActivity(in);
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
    }

    public String getCurrentTimeInDefaultTimeZone() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Automatically uses the system's default timezone
        return sdf.format(new Date());
    }

    // create pdf
    private void createPDF() {
        // Create a new PdfDocument instance
        PdfDocument document = new PdfDocument();

            View view = getLayoutInflater().inflate(R.layout.receipt_layout, null);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Objects.requireNonNull(this.getDisplay()).getRealMetrics(displayMetrics);
            } else this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            TextView refItem, meterItem, tokenItem, amountItem, tariffItem, unitItem, dateItem, chargeItem, vendItem, vatItem, receiptAmountText;
            refItem = view.findViewById(R.id.shareTransID);
            meterItem = view.findViewById(R.id.shareMeterNum);
            tokenItem = view.findViewById(R.id.shareToken);
            amountItem = view.findViewById(R.id.shareAmount);
            tariffItem = view.findViewById(R.id.shareTariff);
            unitItem = view.findViewById(R.id.shareUnit);
            dateItem = view.findViewById(R.id.shareDate);
            chargeItem = view.findViewById(R.id.shareTransCharge);
            vendItem = view.findViewById(R.id.shareTokenValue);
            vatItem = view.findViewById(R.id.shareVat);
            receiptAmountText = view.findViewById(R.id.shareAmountText);


            refItem.setText(TransText);
            meterItem.setText(MeterText);
            tokenItem.setText(TokenText);
            amountItem.setText(String.format("₦ %s", AmountText));
            tariffItem.setText(String.format("₦ %s", TariffText));
            unitItem.setText(String.format("%s kWh", UnitText));
            dateItem.setText(getCurrentTimeInDefaultTimeZone());
            chargeItem.setText(String.format("₦ %s", ChargeText));
            vendItem.setText(String.format("₦ %s",VendedText));
            vatItem.setText(String.format("%s %%", VatText));
            receiptAmountText.setText(String.format("NGN %s", AmountText));

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
            String fileName = "TokenReceipt_" + formatDate +".pdf";
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
}