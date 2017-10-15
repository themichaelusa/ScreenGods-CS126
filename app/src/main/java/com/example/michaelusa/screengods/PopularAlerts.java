package com.example.michaelusa.screengods;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;

import static com.example.michaelusa.screengods.Utilities.userCount;

public class PopularAlerts extends AppCompatActivity {

    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
    FirebaseListAdapter<StockAlert> mAdapter;
    TextView popBool;

    /**
     * OnCreate's purpose in this activity is to:
     *
     * 1. Initialize all necessary instance variables and UI elements.
     * 2. Populate (popalertslist) using FirebaseListAdapter
     * 4. ValueEventListener for dataChange in User's Alert Directory to generate
     * the Popular Alerts directory in the FB DB.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_alerts);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gilroy-ExtraBold.otf");
        initPopularAlertsTheme(typeface);
        checkUserCount();

        // Update Popular Alerts Object in Firebase Database
        mReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                userCount = dataSnapshot.getChildrenCount();
                generatePopularAlerts(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        populatePopularAlertsList(typeface);
    }

    /**
     * Initializes theme and design elements like typeface for PopularAlerts.
     *
     * @param typeface Reference to Gilroy Font.
     */

    private void initPopularAlertsTheme(Typeface typeface) {

        getSupportActionBar().hide();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView popAlerts = (TextView) findViewById(R.id.tv_popalerts);
        popAlerts.setTypeface(typeface);

        popBool = (TextView) findViewById(R.id.tv_popbool);
        popBool.setTypeface(typeface);
    }

    private void checkUserCount() {

        if (userCount == 1){
            popBool.setVisibility(View.VISIBLE);
        }
        else{
            popBool.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Without being very redundant (considering the amount of for loops),
     * this method will combine all the alerts of all users and take the average of
     * their upper and lower limits, as well store them in firebase based on the amount
     * of occurrences as StockAlerts, so we can reuse the code for populating the ListView.
     *
     * @param dataSnapshot Reference to directory of Users in Firebase DB.
     */

    private void generatePopularAlerts(DataSnapshot dataSnapshot) {

        ArrayList<String> topTickers = new ArrayList<>();
        HashMap<String, Long> upperLimitsMap = new HashMap<>();
        HashMap<String, Long> lowerLimitsMap = new HashMap<>();

        for (DataSnapshot user: dataSnapshot.getChildren()){

            for (DataSnapshot alert: user.child("Alerts").getChildren()){

                StockAlert stock = alert.getValue(StockAlert.class);
                String currentTicker = stock.getTicker();
                topTickers.add(currentTicker);

                long currentULim = stock.getUpperLimit();
                long currentLLim = stock.getLowerLimit();

                if (upperLimitsMap.containsKey(currentTicker)){

                    long currentULimAvg = upperLimitsMap.get(currentTicker);
                    currentULimAvg += currentULim;
                    currentULimAvg /= userCount;
                    upperLimitsMap.put(currentTicker, currentULimAvg);
                }
                else {
                    upperLimitsMap.put(currentTicker, currentULim);
                }

                if (lowerLimitsMap.containsKey(currentTicker)){

                    long currentLLimAvg = lowerLimitsMap.get(currentTicker);
                    currentLLimAvg += currentLLim;
                    currentLLimAvg /= userCount;
                    lowerLimitsMap.put(currentTicker, currentLLimAvg);
                }
                else {
                    lowerLimitsMap.put(currentTicker, currentLLim);
                }
            }
        }

        ArrayList<String> refTopTickers = topTickers;
        TreeMap<Integer, String> tickerMap = new TreeMap<>();

        for (String ticker: topTickers){
            int occurrences = Collections.frequency(refTopTickers, ticker);
            tickerMap.put(occurrences, ticker);
        }

        Collection<String> mostPopAlerts = tickerMap.values();
        String[] alerts = new String[mostPopAlerts.size()];
        ArrayList<StockAlert> stockAlertstoPush = new ArrayList<>();
        mostPopAlerts.toArray(alerts);

        for (String alert: alerts){

            long alertAvgUL = upperLimitsMap.get(alert);
            long alertAvgLL = lowerLimitsMap.get(alert);
            stockAlertstoPush.add(new StockAlert(alert, alertAvgUL, alertAvgLL));
        }

        for (StockAlert popAlert: stockAlertstoPush){
            mReference.child("Popular Alerts").child(popAlert.getTicker()).setValue(popAlert);
        }
    }

    /**
     * Uses FirebaseListAdapter to populate the local ListView using the StockAdapter XML File
     * as a Design Framework, using the data from the Popular Alerts Directory in the FB DB.
     *
     * @param typeface Reference to Gilroy Font.
     */

    private void populatePopularAlertsList(final Typeface typeface) {

        ListView popAlertsList = (ListView) findViewById(R.id.popalertslist);
        Query mRef = mReference.child("Popular Alerts");
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

        popAlertsList.setAdapter(mAdapter);
    }
}
