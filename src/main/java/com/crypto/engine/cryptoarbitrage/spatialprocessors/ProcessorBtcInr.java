package com.crypto.engine.cryptoarbitrage.spatialprocessors;

<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
import com.crypto.engine.cryptoarbitrage.exchangetemplates.CoinDCXOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.TransactionNode;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXOrderBook;
=======
import com.crypto.engine.cryptoarbitrage.templates.CoinDCXOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.WazirXOrderBook;
import com.crypto.engine.cryptoarbitrage.templates.unocoin.UnocoinCachedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class ProcessorBtcInr {

    Logger log = LoggerFactory.getLogger(ProcessorBtcInr.class);
    @Autowired
    UnocoinCachedResponse unocoinCachedResponse;

    @Autowired
    private RestTemplate restTemplate;

    public void process() throws HttpClientErrorException {
<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
        CoinDCXOrderBook coinDCXOrderBook = makeCoinDCXCall();
=======
//        CoinDCXOrderBook coinDCXOrderBook = makeCoinDCXCall();
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java
        WazirXOrderBook waxirXOrderBook = makeWazirXCall();
//        GeneralOrderBook ordBk1 = generalizeCoinDCX(coinDCXOrderBook);
//        GeneralOrderBook ordBk1 = generalizeCoinDCX(coinDCXOrderBook);
        GeneralOrderBook ordBk1 = unocoinCachedResponse.getGeneralOrderBook();
        GeneralOrderBook ordBk2 = generalizeWazirX(waxirXOrderBook);

<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
        List<TransactionNode> favourableOpportunities = new ArrayList<>();
=======
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java

        //check if ask price in one is lower than the bid price in other
        //if yes, consider the opportunity and mark the available value as the Min of both ask and bid volume
        //store all the opportunities to the variable and print the opportunity with the highest volume

        //leg1
<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
        ordBk1.getAsks().entrySet()
                .forEach(entry1 -> {
                    ordBk2.getBids().entrySet()
                            .stream()
                            .filter(entry2 -> entry1.getKey() < entry2.getKey())
                            .max((entry1_, entry2_) -> Double.compare(entry1_.getValue(), entry2_.getValue()))
                            .stream().forEach(entry2 -> {
                        System.out.println("Ask CoinDCX: " + entry1.getKey() + " Bid WazirX: " + entry2.getKey()
                                + " Volume: " + Math.min(entry1.getValue(), entry2.getValue()));
                        favourableOpportunities.add(new TransactionNode(entry2.getKey(), entry1.getKey(), Math.min(entry1.getValue(), entry2.getValue())));
                    });
                });

        //leg2
        ordBk2.getAsks().entrySet()
                .forEach(entry1 -> {
                    ordBk1.getBids().entrySet()
                            .stream()
                            .filter(entry2 -> entry1.getKey() < entry2.getKey())
                            .max((entry1_, entry2_) -> Double.compare(entry1_.getValue(), entry2_.getValue()))
                            .stream().forEach(entry2 -> {
                        System.out.println("Ask WazirX: " + entry1.getKey() + " Bid CoinDCX: " + entry2.getKey()
                                + " Volume: " + Math.min(entry1.getValue(), entry2.getValue()));
                        favourableOpportunities.add(new TransactionNode(entry2.getKey(), entry1.getKey(), Math.min(entry1.getValue(), entry2.getValue())));
                    });
                });

        System.out.println(".......................................................");
        System.out.println(".......................................................");
=======
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
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java
    }

    public CoinDCXOrderBook makeCoinDCXCall() throws HttpClientErrorException {

<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
        CoinDCXOrderBook orderBook = restTemplate.getForObject(
                "https://public.coindcx.com/market_data/orderbook?pair=I-XRP_INR", CoinDCXOrderBook.class);
        return orderBook;
    }

    public WazirXOrderBook makeWazirXCall() throws HttpClientErrorException {
        WazirXOrderBook orderBook = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=xrpinr", WazirXOrderBook.class);
        return orderBook;
=======
        return restTemplate.getForObject(
                "https://public.coindcx.com/market_data/orderbook?pair=I-BTC_INR", CoinDCXOrderBook.class);
    }

    public WazirXOrderBook makeWazirXCall() throws HttpClientErrorException {
        return restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=zecinr", WazirXOrderBook.class);
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java
    }

    public GeneralOrderBook generalizeCoinDCX(CoinDCXOrderBook coinDCXOrderBook) {
        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
        HashMap<Double, Double> asks = new HashMap<>();
        HashMap<Double, Double> bids = new HashMap<>();
<<<<<<< HEAD:src/main/java/com/crypto/engine/cryptoarbitrage/spatialprocessors/ProcessorBtcInr.java
        coinDCXOrderBook.getAsks().entrySet().forEach(entry -> {
            Double askPrice = Double.parseDouble(entry.getKey());
            Double volume = Double.parseDouble(entry.getValue());
            asks.put(askPrice, volume);
        });
        coinDCXOrderBook.getBids().entrySet().forEach(entry -> {
            Double bidPrice = Double.parseDouble(entry.getKey());
            Double volume = Double.parseDouble(entry.getValue());
=======
        coinDCXOrderBook.getAsks().forEach((key, value) -> {
            Double askPrice = Double.parseDouble(key);
            Double volume = Double.parseDouble(value);
            asks.put(askPrice, volume);
        });
        coinDCXOrderBook.getBids().forEach((key, value) -> {
            Double bidPrice = Double.parseDouble(key);
            Double volume = Double.parseDouble(value);
>>>>>>> 027274cd8529a550fb245b3ab8f0ef3f4fe580b2:src/main/java/com/crypto/engine/cryptoarbitrage/processor/ProcessorBtcInr.java
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
