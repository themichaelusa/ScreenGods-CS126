package com.example.michaelusa.screengods;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by michaelusa on 4/29/17.
 */

class GoogleFinanceAPI {

    private double livePrice;

    GoogleFinanceAPI(String ticker) {
        this.livePrice = parseLiveTickerPrice(ticker);
    }

    public double getLivePrice() {
        return livePrice;
    }

    /**
     * My usual parseJson function with an additional conversion to a ByteArray so that an
     * InputStreamReader can be used for the creation of the JsonReader.
     *
     * @param JsonAsString The raw JSON data passed in as a String to be converted to JsonReader.
     */

    private static JsonReader parseJson (String JsonAsString) {

        InputStream stream = new ByteArrayInputStream(JsonAsString.getBytes(StandardCharsets.UTF_8));
        return new JsonReader(new InputStreamReader(stream));
    }

    /**
     * Basic parser function for the Google Finance API. Need to substring the contents
     * first, as it is in a non-standard format that isn't JSON to begin with.
     *
     * Got some code from the Oracle Javadocs for pulling a string from a URL:
     * https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
     *
     * @param ticker stock to parse from Google Finance API
     * @return JsonReader (raw JSON data from API)
     */

    private static JsonReader getGoogleFinanceAPIData(String ticker) throws IOException {

        URL gFinance = new URL(Utilities.GOOG_FINANCE_URL + ticker);
        BufferedReader in = new BufferedReader(new InputStreamReader(gFinance.openStream()));

        String currentURLLine, outJSONString = "";
        while ((currentURLLine = in.readLine()) != null)
            outJSONString += currentURLLine;
        in.close();

        return parseJson(outJSONString.substring(4, outJSONString.length() - 1));
    }

    /**
     * Basic try catch (for IOException) to use GSON to parse the JsonReader object
     * into something that can be evaluated as a primitive type.
     *
     * @param ticker stock to parse from Google Finance API
     * @return currentPrice (current price of requested stock ticker)
     */

    private static double parseLiveTickerPrice(String ticker) {

        Gson gson = new Gson();
        double currentPrice = 0;

        try {
            JsonReader rawJson = getGoogleFinanceAPIData(ticker);
            GF_API_Stock stock = gson.fromJson(rawJson, GF_API_Stock.class);
            currentPrice = stock.getL_cur();

        }catch (IOException e) {
            e.getCause();
        }

        return currentPrice;
    }

}