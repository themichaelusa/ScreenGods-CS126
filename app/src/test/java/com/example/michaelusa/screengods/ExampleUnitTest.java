package com.example.michaelusa.screengods;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRegexChecker() throws Exception {

        String ticker = "AMD";
        String wrongTicker = "AM0";
        String puncTicker = "!mD";

        assertTrue(Utilities.tickerRegexCheck(ticker));
        assertFalse(Utilities.tickerRegexCheck(wrongTicker));
        assertFalse(Utilities.tickerRegexCheck(puncTicker));
    }

    @Test
    public void testBadInputs() throws Exception {

        String ticker = "";
        double upperLimit = 10.44;
        double lowerLimit = 11.54;

        boolean badTicker = ticker.trim().length() == 0;
        boolean badULim =  String.valueOf(upperLimit).trim().length() == 0;
        boolean badLLim =  String.valueOf(lowerLimit).trim().length() == 0;
        boolean emptyFields = badTicker || badULim || badLLim;

        assertTrue(emptyFields);

        boolean badLimits = upperLimit <= lowerLimit;
        assertTrue(badLimits);
    }

    @Test
    public void testGFIObject() throws Exception {

        GoogleFinanceAPI test = new GoogleFinanceAPI("AMD");
        double livePrice = test.getLivePrice();

        assertTrue(livePrice > 0);
    }
}