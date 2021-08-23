package com.crypto.engine.cryptoarbitrage.triangularprocessors;

import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXOrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WazirXTriangularArbitrageProcessor {
    /*
    *let's say you are trying to find arb opportunity for the below 5 currencies
    *
    * BTC, ETH, MATIC
    * You define a start currency and end currency - in this case let it be INR
    *
    * Possible triangulations - 3p2=6
    *
    * Sell Currency -> Buy Currency
    * leg 1     leg 2    leg 3
    * INR->BTC BTC->ETH ETH->INR
    * INR->BTC BTC->MATIC MATIC->INR
    * INR->ETH ETH->BTC BTC->INR
    * INR->ETH ETH->MATIC MATIC->INR
    * INR->MATIC MATIC->BTC BTC->INR
    * INR->MATIC MATIC->ETH ETH->INR
    *
    *
    * for leg 1 when start currency is INR and the "quote currency" in the API response is INR
    * check for Ask Price
    * Amount in target currency = (Amount/Ask Price)
    * Final amount after fee(leg 1) = Amount in target currency(1-fee%)
    * question -> whether to check for all Ask prices in the order book or have some logic to check for min Ask Price,
    * will we miss arbitrage opportunities if we filter out?
    *
    * for leg 2
    *   - when the "Sell Currency" is the "quote currency" in the API response
    *   - check for Ask Price
    *   - Amount in target currency = (Amount/Ask Price)
    *   -Final amount after fee(leg 2) = Amount in target currency(1-fee%)
    *
    *   - when the "Buy Currency" is the "quote currency" in the API response
    *   - check for Bid price
    *   - Amount in target currency = (Amount*Bid Price)
    *   - Final amount after fee(leg 2) = Amount in target currency(1-fee%)
    *
    *
    * for leg 3 when end currency is INR and the "quote currency" in the API response is INR
    * check for Bid Price
    * Amount in target currency = (Amount*Bid Price)
    * Final amount after fee(leg 3) = Amount in target currency(1-fee%)
    *
    * Arbitrage Decision
    * If Final amount after fee(leg 3) > initial amount -> positive
    * else -> negative
    *
    * */

    @Autowired
    private RestTemplate restTemplate;

    private WazirXOrderBook orderBookCr1_Cr2;
    private WazirXOrderBook orderBookCr3_Cr1;
    private WazirXOrderBook orderBookCr2_Cr3;;

    public void process(double initialInvestment){
        orderBookCr1_Cr2 =null;
        orderBookCr2_Cr3 =null;
        orderBookCr3_Cr1 =null;

        makeWazirXCall();
        GeneralOrderBook ordBk1 = generalizeWazirX(orderBookCr1_Cr2);
        GeneralOrderBook ordBk2 = generalizeWazirX(orderBookCr2_Cr3);
        GeneralOrderBook ordBk3 = generalizeWazirX(orderBookCr3_Cr1);

        //INR->BTC BTC->MATIC MATIC->INR
        //entry-> (ask price/bid price, volume)
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
                .filter(element ->initialInvestment<element)
                .forEach(x -> System.out.println(new java.util.Date() + " opportunity -> " + x));
    }

    public void makeWazirXCall() throws HttpClientErrorException {
        orderBookCr1_Cr2 = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=btcinr&limit=50", WazirXOrderBook.class);
        orderBookCr2_Cr3 = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=maticbtc&limit=50", WazirXOrderBook.class);
        orderBookCr3_Cr1 = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market=maticinr&limit=50", WazirXOrderBook.class);
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
