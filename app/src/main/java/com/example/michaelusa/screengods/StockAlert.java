package com.example.michaelusa.screengods;

/**
 * Created by michaelusa on 4/18/17.
 */

/**
 * All that's really needed for this app! Just a ticker, and upper and lower bounds for
 * the Google Finance API to work it's magic and trigger push notifications.
 */

public class StockAlert {

    public String ticker;
    public long upperLimit;
    public long lowerLimit;

    public StockAlert() {}

    public StockAlert(String ticker, long upperLimit, long lowerLimit) {

        this.ticker = ticker;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    public String getTicker() {
        return ticker;
    }

    public long getUpperLimit() {
        return upperLimit;
    }

    public long getLowerLimit() {
        return lowerLimit;
    }

}
