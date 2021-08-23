package com.crypto.engine.cryptoarbitrage.spatialprocessors;

import com.crypto.engine.cryptoarbitrage.exchangetemplates.CoinDCXOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.TransactionNode;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXOrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class ProcessorBtcInr {

    @Autowired
    private RestTemplate restTemplate;

    public void process() throws HttpClientErrorException {
        CoinDCXOrderBook coinDCXOrderBook = makeCoinDCXCall();
        WazirXOrderBook waxirXOrderBook = makeWazirXCall();
        GeneralOrderBook ordBk1 = generalizeCoinDCX(coinDCXOrderBook);
        GeneralOrderBook ordBk2 = generalizeWazirX(waxirXOrderBook);

        List<TransactionNode> favourableOpportunities = new ArrayList<>();

        //check if ask price in one is lower than the bid price in other
        //if yes, consider the opportunity and mark the available value as the Min of both ask and bid volume
        //store all the opportunities to the variable and print the opportunity with the highest volume

        //leg1
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
    }

    public CoinDCXOrderBook makeCoinDCXCall() throws HttpClientErrorException {

        CoinDCXOrderBook orderBook = restTemplate.getForObject(
                "https://public.coindcx.com/market_data/orderbook?pair=I-XRP_INR", CoinDCXOrderBook.class);
        return orderBook;
    }

    public WazirXOrderBook makeWazirXCall() throws HttpClientErrorException {
        WazirXOrderBook orderBook = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=xrpinr", WazirXOrderBook.class);
        return orderBook;
    }

    public GeneralOrderBook generalizeCoinDCX(CoinDCXOrderBook coinDCXOrderBook) {
        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
        HashMap<Double, Double> asks = new HashMap<>();
        HashMap<Double, Double> bids = new HashMap<>();
        coinDCXOrderBook.getAsks().entrySet().forEach(entry -> {
            Double askPrice = Double.parseDouble(entry.getKey());
            Double volume = Double.parseDouble(entry.getValue());
            asks.put(askPrice, volume);
        });
        coinDCXOrderBook.getBids().entrySet().forEach(entry -> {
            Double bidPrice = Double.parseDouble(entry.getKey());
            Double volume = Double.parseDouble(entry.getValue());
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
