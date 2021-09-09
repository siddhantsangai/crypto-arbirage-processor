package com.crypto.engine.cryptoarbitrage.triangularprocessors.binance;

import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.Leg;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalOrderBook;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalOrderBookTemplate;
import com.crypto.engine.cryptoarbitrage.triangularprocessors.statics.LegBlockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UniversalTriangularArbitrageProcessor {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LegBlockInitializer legBlockInitializer;

    @Value("${feeFactor}")
    private double feeFactor;

    @Value("${initialInvestment}")
    private double initialInvestment;

    private HashMap<String, GeneralOrderBook> orderBooks;

    public void processLegs(Leg leg1, Leg leg2, Leg leg3, double initialInvestment, double feeFactor){
        List<Double> valueAfterLeg1;
        List<Double> valueAfterLeg2=new ArrayList<>();
        List<Double> valueAfterLeg3=new ArrayList<>();

        List<Double> priceLeg1;
        List<Double> priceLeg2;
        List<Double> priceLeg3;
        //System.out.println("New cycle - Fetching order book...");
        fetchOrderBook(leg1.getTicker());
        fetchOrderBook(leg2.getTicker());
        fetchOrderBook(leg3.getTicker());
        //System.out.println("Start Computation...");
        if(leg1.getBuyCurrency().equalsIgnoreCase(leg1.getQuoteCurrency())){
            valueAfterLeg1=processLegWhereBuyCurrencyIsEqualToQuoteCurrency(initialInvestment,feeFactor,leg1);
            priceLeg1=new ArrayList<>(orderBooks.get(leg1.getTicker()).getBids().keySet());
        }
        else{
            valueAfterLeg1=processLegWhereSellCurrencyIsEqualToQuoteCurrency(initialInvestment,feeFactor,leg1);
            priceLeg1=new ArrayList<>(orderBooks.get(leg1.getTicker()).getAsks().keySet());
        }

        if(leg2.getBuyCurrency().equalsIgnoreCase(leg2.getQuoteCurrency())){
            valueAfterLeg1.forEach(
                    investment -> valueAfterLeg2.addAll(processLegWhereBuyCurrencyIsEqualToQuoteCurrency(investment,feeFactor,leg2))
            );
            priceLeg2=new ArrayList<>(orderBooks.get(leg2.getTicker()).getBids().keySet());
        }
        else{
            valueAfterLeg1.forEach(
                    investment -> valueAfterLeg2.addAll(processLegWhereSellCurrencyIsEqualToQuoteCurrency(investment,feeFactor,leg2))
            );
            priceLeg2=new ArrayList<>(orderBooks.get(leg2.getTicker()).getAsks().keySet());
        }

        if(leg3.getBuyCurrency().equalsIgnoreCase(leg3.getQuoteCurrency())){
            valueAfterLeg2.forEach(
                    investment -> valueAfterLeg3.addAll(processLegWhereBuyCurrencyIsEqualToQuoteCurrency(investment,feeFactor,leg3))
            );
            priceLeg3=new ArrayList<>(orderBooks.get(leg3.getTicker()).getBids().keySet());
        }
        else{
            valueAfterLeg2.forEach(
                    investment -> valueAfterLeg3.addAll(processLegWhereSellCurrencyIsEqualToQuoteCurrency(investment,feeFactor,leg3))
            );
            priceLeg3=new ArrayList<>(orderBooks.get(leg3.getTicker()).getAsks().keySet());
        }

        int index=0;
        int lenLeg1=priceLeg1.size();
        int lenLeg2=priceLeg2.size();
        int lenLeg3=priceLeg3.size();
        double maxOutputValue=0;
        int maxOutputValueIndex=0;
        for (Double value: valueAfterLeg3){
            if(maxOutputValue<value) {
                maxOutputValue = value;
                maxOutputValueIndex=index;
            }
            index++;
        }
        if(initialInvestment<maxOutputValue){
            int indexOfPriceInLeg1=(maxOutputValueIndex/(lenLeg2*lenLeg3))%lenLeg1;
            int indexOfPriceInLeg2=(maxOutputValueIndex/lenLeg3)%lenLeg2;
            int indexOfPriceInLeg3=maxOutputValueIndex%lenLeg3;
            System.out.println(new java.util.Date() + " ...Arb opportunity found...");
            System.out.println("Buy " + leg1.getBuyCurrency() + " at price " + priceLeg1.get(indexOfPriceInLeg1) + " " + leg1.getQuoteCurrency());
            System.out.println("Buy " + leg2.getBuyCurrency() + " at price " + priceLeg2.get(indexOfPriceInLeg2) + " " + leg2.getQuoteCurrency());
            System.out.println("Buy " + leg3.getBuyCurrency() + " at price " + priceLeg3.get(indexOfPriceInLeg3) + " " + leg3.getQuoteCurrency());
            System.out.println("If initial investment=" + initialInvestment + " return= " + maxOutputValue);
            System.out.println("...........................");
        }
        //System.out.println("Computation Complete...");
    }

    public List<Double> processLegWhereBuyCurrencyIsEqualToQuoteCurrency(double investment,double feeFactor, Leg leg){
        GeneralOrderBook orderBook = orderBooks.get(leg.getTicker());
        Function<Map.Entry<Double,Double>, Double> volumeCheck = entry -> {
            if(entry.getValue()>investment)
                return (investment*entry.getKey())*feeFactor;
            else
                return Double.valueOf(0);
        };

        return orderBook.getBids().entrySet().stream()
                //.filter(entry -> entry.getValue()>investment)
                .map(volumeCheck)
                .collect(Collectors.toList());
    }

    public List<Double> processLegWhereSellCurrencyIsEqualToQuoteCurrency(double investment,double feeFactor, Leg leg){
        GeneralOrderBook orderBook = orderBooks.get(leg.getTicker());
        Function<Map.Entry<Double,Double>, Double> volumeCheck = entry -> {
            if(entry.getValue()>(investment/entry.getKey()))
                return (investment/entry.getKey())*feeFactor;
            else
                return Double.valueOf(0);
        };
        return orderBook.getAsks().entrySet().stream()
                //.filter(entry -> entry.getValue()>(investment/entry.getKey()))
                .map(volumeCheck)
                .collect(Collectors.toList());
    }


    public void fetchOrderBook(String ticker){
        UniversalOrderBookTemplate orderBookTemplate = restTemplate.getForObject(
                "http://localhost:3000/binance/orderbook/"+ticker, UniversalOrderBookTemplate.class);
        orderBooks.put(ticker, parseOrderBook(orderBookTemplate.getData()));
    }

    public GeneralOrderBook parseOrderBook(UniversalOrderBook orderBook) {
        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
        HashMap<Double, Double> asks = new HashMap<>();
        HashMap<Double, Double> bids = new HashMap<>();
        orderBook.getAsks()
                .entrySet()
                .forEach(entry -> {
                    Double askPrice = Double.parseDouble(entry.getKey());
                    asks.put(askPrice, entry.getValue());
                });
        orderBook.getBids()
                .entrySet()
                .forEach(entry -> {
                    Double bidPrice = Double.parseDouble(entry.getKey());
                    bids.put(bidPrice, entry.getValue());
                });
        generalOrderBook.setAsks(asks);
        generalOrderBook.setBids(bids);
        return generalOrderBook;
    }

    public void initializeProcessor(){
        legBlockInitializer.initializeLegBlocks();
    }

    public void startProcessing(){
        orderBooks=new HashMap<>();
        legBlockInitializer.getLegBlocks()
                .forEach(block-> processLegs(block.get(0), block.get(1), block.get(2), initialInvestment, feeFactor));
    }
}
