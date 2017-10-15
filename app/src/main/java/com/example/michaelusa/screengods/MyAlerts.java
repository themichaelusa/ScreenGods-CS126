package com.example.michaelusa.screengods;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.michaelusa.screengods.Utilities.alertCount;

public class MyAlerts extends AppCompatActivity {

    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseListAdapter<StockAlert> mAdapter;
    TextView activeAlerts;

    /**
     * OnCreate's purpose in this activity is to:
     *
     * 1. Initialize all necessary instance variables and UI elements.
     * 2. Populate (myalertslist) using FirebaseListAdapter
     * 3. Provide interactivity with newAlert Button
     * 4. SingleValueEventListener for dataChange in User's Alert Directory using
     * AsyncTask for parsing Google Finance Data and removing necessary alerts.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_alerts);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gilroy-ExtraBold.otf");
        final Query mRef = mReference.child(currentUser.getUid()).child("Alerts");

        initMyAlertsTheme(typeface);
        populateFirebaseAlertsList(typeface, mRef);

        final Button newAlert = (Button) findViewById(R.id.btn_newalert);
        newAlert.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchToAddAlertActivity(getCurrentFocus());
            }
        });

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snap) {

                alertCount = snap.getChildrenCount();
                checkAlertCount();
                if (alertCount == 0) return;

                for(DataSnapshot alert : snap.getChildren()) {
                    asyncTickerParamCheck(alert);
                }
            }

            @Override public void onCancelled(DatabaseError de) {}
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    public void onBackPressed() {

        finish();
        Intent intent = new Intent(MyAlerts.this, MainMenu.class);
        startActivity(intent);
    }

    /**
     * Method to iterate through a StockAlert's childen and generate/execute
     * a new AsyncTask to compare them with the the current stock price.
     *
     * @param alert (Contains ticker, upperLimit, lowerLimit)
     */

    public void asyncTickerParamCheck(DataSnapshot alert) {

        ArrayList<String> tickerData = new ArrayList<>();

        for (DataSnapshot stockData: alert.getChildren()){
            tickerData.add(stockData.getValue().toString());
        }

        AsyncTask<ArrayList<String>, Void, String[]> t = new CheckCurrentTickersAsyncTask();
        t.execute(tickerData);
    }

    /**
     * Asynctask for creating a GoogleFinanceAPI object, getting the live value of the ticker,
     * and on completion, comparing that to the current lower/upper limits and triggering a push
     * notification and removing the offending StockAlert if that is the case.
     */

    private class CheckCurrentTickersAsyncTask extends AsyncTask<ArrayList<String>, Void, String[]> {

        @Override
        protected String[] doInBackground(ArrayList<String>... params) {

            ArrayList<String> tickerData = params[0];
            String ticker = tickerData.get(1), lLim = tickerData.get(0), uLim = tickerData.get(2);
            GoogleFinanceAPI currentTicker = new GoogleFinanceAPI(ticker);

            return new String[] {String.valueOf(currentTicker.getLivePrice()), uLim, lLim, ticker};
        }

        @Override
        protected void onPostExecute(String[] result) {

            String ticker = result[3];
            double currentPrice = Double.parseDouble(result[0]);
            double uLim =  Double.parseDouble(result[1]), lLim = Double.parseDouble(result[2]);

            if (currentPrice >= uLim || currentPrice <= lLim){
                DatabaseReference mRef = mReference.child(currentUser.getUid()).child("Alerts");
                mRef.child(ticker).removeValue();

                //deploy push notification (and boom, we done)
            }
        }
    }

    /**
     * Initializes theme and design elements like typeface for MyAlerts.
     */

    private void initMyAlertsTheme(Typeface typeface) {

        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView myAlerts = (TextView) findViewById(R.id.tv_myalerts);
        myAlerts.setTypeface(typeface);

        activeAlerts = (TextView) findViewById(R.id.tv_active);
        activeAlerts.setTypeface(typeface);

        TextView btnAddAlert = (TextView) findViewById(R.id.btn_newalert);
        btnAddAlert.setTypeface(typeface);
    }

    /**
     * Uses FirebaseListAdapter to populate the local ListView using the StockAdapter XML File
     * as a Design Framework.
     *
     * @param typeface Reference to Gilroy Font.
     * @param mRef Query reference to user's Alerts directory.
     */

    private void populateFirebaseAlertsList(final Typeface typeface, final Query mRef) {

        ListView alertsList = (ListView) findViewById(R.id.myalertslist);
        mAdapter = new FirebaseListAdapter<StockAlert>(this, StockAlert.class, R.layout.activity_stock_adapter, mRef) {

            @Override
            protected void populateView(View v, StockAlert alert, int position) {

                ((TextView)v.findViewById(R.id.tickerListItem)).setText(alert.getTicker());
                ((TextView)v.findViewById(R.id.ulimListItem)).setText(String.valueOf(alert.getUpperLimit()));
                ((TextView)v.findViewById(R.id.llimListItem)).setText(String.valueOf(alert.getLowerLimit()));

                TextView tickerListItem = (TextView)v.findViewById(R.id.tickerListItem);
                tickerListItem.setText(alert.getTicker());
                tickerListItem.setTypeface(typeface);

                TextView ulimListItem = (TextView)v.findViewById(R.id.ulimListItem);
                ulimListItem.setText(String.valueOf(Utilities.ULIM_STR + alert.getUpperLimit()));
                ulimListItem.setTypeface(typeface);

                TextView llimListItem = (TextView)v.findViewById(R.id.llimListItem);
                llimListItem.setText(String.valueOf(Utilities.LLIM_STR + alert.getLowerLimit()));
                llimListItem.setTypeface(typeface);
            }
        };

        alertsList.setAdapter(mAdapter);
    }

    private void checkAlertCount() {

        if (alertCount == 0){
            activeAlerts.setVisibility(View.VISIBLE);
        }
        else{
            activeAlerts.setVisibility(View.INVISIBLE);
        }
    }

    private void switchToAddAlertActivity(View view){
        Intent intent = new Intent(MyAlerts.this, AddAlert.class);
        startActivity(intent);
    }
}
