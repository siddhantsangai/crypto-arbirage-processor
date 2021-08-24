package com.crypto.engine.cryptoarbitrage.processor;

import com.crypto.engine.cryptoarbitrage.templates.CoinDCXOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.WazirXOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.unocoin.UnocoinCachedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessorBtcInr {

    Logger log = LoggerFactory.getLogger(ProcessorBtcInr.class);
    @Autowired
    UnocoinCachedResponse unocoinCachedResponse;

    @Autowired
    private RestTemplate restTemplate;

    public void process() throws HttpClientErrorException {
//        CoinDCXOrderBook coinDCXOrderBook = makeCoinDCXCall();
        WazirXOrderBook waxirXOrderBook = makeWazirXCall();
//        GeneralOrderBook ordBk1 = generalizeCoinDCX(coinDCXOrderBook);
//        GeneralOrderBook ordBk1 = generalizeCoinDCX(coinDCXOrderBook);
        GeneralOrderBook ordBk1 = unocoinCachedResponse.getGeneralOrderBook();
        GeneralOrderBook ordBk2 = generalizeWazirX(waxirXOrderBook);


        //check if ask price in one is lower than the bid price in other
        //if yes, consider the opportunity and mark the available value as the Min of both ask and bid volume
        //store all the opportunities to the variable and print the opportunity with the highest volume

        //leg1
        ordBk1.getAsks().forEach((key, value) -> ordBk2.getBids().entrySet()
                                                       .stream()
                                                       .filter(entry2 -> key < entry2.getKey())
                                                       .max(Comparator.comparingDouble(Map.Entry::getValue)).ifPresent(entry2 -> log.info("Ask UnoCoin: " + key + " Bid WazirX: " + entry2.getKey()
                        + " Volume: " + Math.min(value, entry2.getValue()))));

        //leg2
        ordBk2.getAsks().forEach((key, value) -> ordBk1.getBids().entrySet()
                                                       .stream()
                                                       .filter(entry2 -> key < entry2.getKey())
                                                       .max(Comparator.comparingDouble(Map.Entry::getValue)).ifPresent(entry2 -> log.info("Ask WazirX: " + key + " Bid UnoCoin: " + entry2.getKey()
                        + " Volume: " + Math.min(value, entry2.getValue()))));

        log.info(".......................................................");
        log.info(".......................................................");
    }

    public CoinDCXOrderBook makeCoinDCXCall() throws HttpClientErrorException {

        return restTemplate.getForObject(
                "https://public.coindcx.com/market_data/orderbook?pair=I-BTC_INR", CoinDCXOrderBook.class);
    }

    public WazirXOrderBook makeWazirXCall() throws HttpClientErrorException {
        return restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=zecinr", WazirXOrderBook.class);
    }

    public GeneralOrderBook generalizeCoinDCX(CoinDCXOrderBook coinDCXOrderBook) {
        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
        HashMap<Double, Double> asks = new HashMap<>();
        HashMap<Double, Double> bids = new HashMap<>();
        coinDCXOrderBook.getAsks().forEach((key, value) -> {
            Double askPrice = Double.parseDouble(key);
            Double volume = Double.parseDouble(value);
            asks.put(askPrice, volume);
        });
        coinDCXOrderBook.getBids().forEach((key, value) -> {
            Double bidPrice = Double.parseDouble(key);
            Double volume = Double.parseDouble(value);
            bids.put(bidPrice, volume);
        });
        generalOrderBook.setAsks(asks);
        generalOrderBook.setBids(bids);
        return generalOrderBook;
    }

    public GeneralOrderBook generalizeWazirX(WazirXOrderBook wazirXOrderBook) {
        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
        HashMap<Double, Double> asks = new HashMap<>();
        HashMap<Double, Double> bids = new HashMap<>();
        Arrays.stream(wazirXOrderBook.getAsks()).forEach(entry -> {
            Double askPrice = Double.parseDouble(entry[0]);
            Double volume = Double.parseDouble(entry[1]);
            asks.put(askPrice, volume);
        });
        Arrays.stream(wazirXOrderBook.getBids()).forEach(entry -> {
            Double bidPrice = Double.parseDouble(entry[0]);
            Double volume = Double.parseDouble(entry[1]);
            bids.put(bidPrice, volume);
        });
        generalOrderBook.setAsks(asks);
        generalOrderBook.setBids(bids);
        return generalOrderBook;
    }
}
