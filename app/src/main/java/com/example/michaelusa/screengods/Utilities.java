package com.example.michaelusa.screengods;

/**
 * Created by michaelusa on 4/24/17.
 */

class Utilities {

    static long userCount = 1;
    static long alertCount;

    static final String ULIM_STR = "UPPER LIMIT: $";
    static final String LLIM_STR = "LOWER LIMIT: $";

    static final int RC_SIGN_IN = 9001;
    static final String GOOG_FINANCE_URL = "http://finance.google.com/finance/info?client=ig&q=NASDAQ%3A";

    static boolean tickerRegexCheck(String ticker){
        return ticker.matches("[a-zA-Z]+");
    }
}
