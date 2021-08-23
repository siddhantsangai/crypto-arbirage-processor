package com.crypto.engine.cryptoarbitrage.triangularprocessors;

import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.CoinDCXOrderBook;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXOrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CoinDCXTriangularArbitrage {

    @Autowired
    private RestTemplate restTemplate;

    private WazirXOrderBook orderBookCr1_Cr2;
    private CoinDCXOrderBook orderBookCr2_Cr3;;
    private WazirXOrderBook orderBookCr3_Cr1;


    public void process(double initialInvestment) throws HttpClientErrorException, InterruptedException {
        orderBookCr1_Cr2 =null;
        orderBookCr2_Cr3 =null;
        orderBookCr3_Cr1 =null;

        makeCoinDCXCall();
        GeneralOrderBook ordBk1 = generalizeWazirX(orderBookCr1_Cr2);
        GeneralOrderBook ordBk2 = generalizeCoinDCX(orderBookCr2_Cr3);
        GeneralOrderBook ordBk3 = generalizeWazirX(orderBookCr3_Cr1);

        //INR->BTC BTC->MATIC MATIC->INR

        List<Double> leg1= ordBk1.getAsks().entrySet().stream()
                .filter(entry -> entry.getValue()>(initialInvestment/entry.getKey()))
                .map(entry -> (initialInvestment/entry.getKey())*0.998)
                .collect(Collectors.toList());

        //System.out.println("leg1 :" + leg1.toString());

        List<Double> leg2= new ArrayList<>();

        //- when the "Buy Currency" is the "quote currency" in the API response
//        leg1.forEach(entry -> {
//            ordBk2.getBids().entrySet().stream()
//                    .filter(entry2 -> entry2.getValue()>entry)
//                    .map(entry2 -> (entry*entry2.getKey())*0.998)
//                    .forEach(entry2 -> leg2.add(entry2));
//        });

        //- when the "Sell Currency" is the "quote currency" in the API response
        leg1.forEach(entry -> {
            ordBk2.getAsks().entrySet().stream()
                    .filter(entry2 -> entry2.getValue()>(entry/entry2.getKey()))
                    .map(entry2 -> (entry/entry2.getKey())*0.998)
                    .forEach(entry2 -> leg2.add(entry2));
        });

        //System.out.println("leg2 :" + leg2.toString());

        List<Double> leg3= new ArrayList<>();

        leg2.forEach(entry -> {
            ordBk3.getBids().entrySet().stream()
                    .filter(entry2 -> entry2.getValue()>entry)
                    .map(entry2 -> (entry*entry2.getKey())*0.998)
                    .forEach(entry2 -> leg3.add(entry2));
        });

        leg3.stream()
                .filter(element -> initialInvestment<element)
                .forEach(x -> System.out.println(new java.util.Date() + " opportunity -> " + x));
    }


    public void makeCoinDCXCall() throws HttpClientErrorException, InterruptedException {

        orderBookCr1_Cr2 = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=btcinr&limit=50", WazirXOrderBook.class);

        orderBookCr2_Cr3 = restTemplate.getForObject(
                "https://public.coindcx.com/market_data/orderbook?pair=B-MATIC_BTC", CoinDCXOrderBook.class);

        orderBookCr3_Cr1 = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=maticinr&limit=50", WazirXOrderBook.class);

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
