package com.example.michaelusa.screengods;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.michaelusa.screengods.Utilities.alertCount;

public class AddAlert extends AppCompatActivity {

    private EditText ticker;
    private EditText upperLimit;
    private EditText lowerLimit;

    /**
     * onCreate is responsible for:
     *
     * 1. Basic UI handling (EditText, Button Listeners)
     * 2. Verifying correctness of user inputs (see badInputs/regexCheck methods)
     * 3. If all inputs correct: creates StockAlert and upload it to FB DB, and
     * changes the current activity back to My Alerts.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);
        initAddAlertTheme();

        ticker = (EditText) findViewById(R.id.et_ticker);
        upperLimit = (EditText) findViewById(R.id.et_ulim);
        lowerLimit = (EditText) findViewById(R.id.et_llim);

        final Button cancel = (Button) findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToMyAlerts(getCurrentFocus());
            }
        });

        final Button create = (Button) findViewById(R.id.btn_create);
        create.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (alertCount >= 7){
                    throwErrorToast("SIZE");
                    return;
                }

                String tickerStr = ticker.getText().toString();
                String uLimStr = upperLimit.getText().toString();
                String lLimStr = lowerLimit.getText().toString();

                if (Utilities.tickerRegexCheck(tickerStr) && badInputs(tickerStr, uLimStr, lLimStr)) {
                    return;
                }

                addStockAlert(tickerStr, uLimStr, lLimStr);
                switchToMyAlerts(getCurrentFocus());
            }
        });
    }

    @Override
    public void onBackPressed() {

        finish();
        switchToMyAlerts(getCurrentFocus());
    }

    /**
     * Initializes UI Styling for Add Alert Activity.
     */

    public void initAddAlertTheme() {

        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gilroy-ExtraBold.otf");

        TextView newAlert = (TextView) findViewById(R.id.new_alert);
        newAlert.setTypeface(typeface);

        TextView tv_ticker = (TextView) findViewById(R.id.tv_addticker);
        tv_ticker.setTypeface(typeface);

        TextView tv_uLim = (TextView) findViewById(R.id.tv_addulim);
        tv_uLim.setTypeface(typeface);

        TextView tv_lLim = (TextView) findViewById(R.id.tv_addllim);
        tv_lLim.setTypeface(typeface);

        TextView btn_cancel = (TextView) findViewById(R.id.btn_cancel);
        btn_cancel.setTypeface(typeface);

        TextView btn_create = (TextView) findViewById(R.id.btn_create);
        btn_create.setTypeface(typeface);
    }

    /**
     * Finds empty EditText fields and warns the user.
     *
     * @param ticker Stock Ticker Field.
     * @param upperLimit Price UpperLimit Field.
     * @param lowerLimit Price LowerLimit Field.
     *
     * @return True/False (to prompt user to retry/ or just continue)
     */

    public boolean badInputs(String ticker, String upperLimit, String lowerLimit) {

        boolean badTicker =  ticker.trim().length() == 0;
        boolean badULim =  String.valueOf(upperLimit).trim().length() == 0;
        boolean badLLim =  String.valueOf(lowerLimit).trim().length() == 0;
        boolean emptyFields = badTicker || badULim || badLLim;

        if (emptyFields) {
            throwErrorToast("EMPTY");
            return true;
        }

        else {

            final double uLim = Double.parseDouble(upperLimit);
            final double lLim = Double.parseDouble(lowerLimit);
            boolean badLimits = uLim <= lLim;

            if (badLimits){
                throwErrorToast("LIM");
                return true;
            }
        }

        return false;
    }

    /**
     * Method that throws specific toast based on what the user screwed up,
     * whether it be that they are missing input fields, or they mismatched their
     * upper and lower limits, or they exceeded their total amount of alerts.
     *
     * @param errorType
     */

    public void throwErrorToast(String errorType) {

        switch (errorType) {

            case "EMPTY":

                String emptyStr = "EMPTY FIELD(S)! TRY AGAIN!";
                Toast emptyField = Toast.makeText(getApplicationContext(), emptyStr, Toast.LENGTH_SHORT);
                emptyField.show();
                break;

            case "LIM":

                String limError = "MISMATCHED LIMITS! TRY AGAIN!";
                Toast badLims = Toast.makeText(getApplicationContext(), limError, Toast.LENGTH_SHORT);
                badLims.show();
                break;

            case "SIZE":

                String sizeExceeded = "ALERT COUNT EXCEEDED! TRY LATER!";
                Toast sizeEx = Toast.makeText(getApplicationContext(), sizeExceeded, Toast.LENGTH_SHORT);
                sizeEx.show();
                break;
        }
    }

    /**
     * Adds 1 Stock Alert object to current user's Alert list.
     * @param ticker String that indicates which stock to watch (AMD, GOOG, AAPL)
     * @param lowerLimit Double that indicates lower notification bound (60.33)
     * @param upperLimit Double that indicates upper notification bound (75.91)
     */

    public void addStockAlert(String ticker, String upperLimit, String lowerLimit) {

        final long uLim = (long) Double.parseDouble(upperLimit);
        final long lLim = (long) Double.parseDouble(lowerLimit);
        StockAlert newAlert = new StockAlert(ticker.toUpperCase(), uLim, lLim);

        DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersReference.child(currentUser.getUid()).child("Alerts").child(ticker).setValue(newAlert);
    }

    private void switchToMyAlerts(View view) {
        Intent intent = new Intent(AddAlert.this, MyAlerts.class);
        startActivity(intent);
    }
}
