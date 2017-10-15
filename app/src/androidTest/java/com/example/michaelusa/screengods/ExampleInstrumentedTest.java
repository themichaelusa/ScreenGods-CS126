package com.example.michaelusa.screengods;

import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void tryStockAlert() throws Exception {

        String ticker = "GM";
        String upperLimit = "45.66";
        String lowerLimit = "42.17";

        final long uLim = (long) Double.parseDouble(upperLimit);
        final long lLim = (long) Double.parseDouble(lowerLimit);
        StockAlert newAlert = new StockAlert(ticker.toUpperCase(), uLim, lLim);

        String musachenkoUID = "5NPPorodIYUn5WQj9MkCn3MtMmY2";
        DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference();
        mUsersReference.child(musachenkoUID).child("Alerts").child(ticker).setValue(newAlert);
    }
}
