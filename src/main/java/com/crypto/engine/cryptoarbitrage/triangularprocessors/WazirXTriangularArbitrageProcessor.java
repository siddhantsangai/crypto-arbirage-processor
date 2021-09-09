package com.crypto.engine.cryptoarbitrage.triangularprocessors;

import com.crypto.engine.cryptoarbitrage.dao.GeneralOrderBook;
import com.crypto.engine.cryptoarbitrage.dao.Leg;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXMarketTicker;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.WazirXOrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.function.Function;
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

    @Value("${currencies}")
    private String listOfCurrencies;
    private HashMap<String, WazirXMarketTicker> marketTickers = new HashMap<>();
    private HashMap<String, GeneralOrderBook> orderBooks;
    private Set<String> setOfRequiredOrderBooks = new HashSet<>();
    private List<List<Leg>> legBlocks = new ArrayList<>();

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
        //check for volume - return 0, if there is volume mismatch. This will keep the size of the final output consistent.
        //additional condition specific to WazirX. If either buy or sell is WRX feefactor=1
        Function<Map.Entry<Double,Double>, Double> volumeCheck = entry -> {
            if(entry.getValue()>investment && (leg.getBuyCurrency()=="wrx" || leg.getSellCurrency()=="wrx"))
                return (investment*entry.getKey())*1;
            else if(entry.getValue()>investment)
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
            if(entry.getValue()>investment && (leg.getBuyCurrency()=="wrx" || leg.getSellCurrency()=="wrx"))
                return (investment/entry.getKey())*1;
            else if(entry.getValue()>(investment/entry.getKey()))
                return (investment/entry.getKey())*feeFactor;
            else
                return Double.valueOf(0);
        };
        return orderBook.getAsks().entrySet().stream()
                //.filter(entry -> entry.getValue()>(investment/entry.getKey()))
                .map(volumeCheck)
                .collect(Collectors.toList());
    }

    public void fetchOrderBooks(){
        setOfRequiredOrderBooks.forEach( ticker -> {
            WazirXOrderBook orderBook = restTemplate.getForObject(
                    "https://api.wazirx.com/uapi/v1/depth?market="+ticker+"&limit=50", WazirXOrderBook.class);
            orderBooks.put(ticker, generalizeWazirX(orderBook));
            System.out.println("Making call-> "+"https://api.wazirx.com/uapi/v1/depth?market="+ticker+"&limit=50");
        });
    }

    public void fetchOrderBook(String ticker){
        WazirXOrderBook orderBook = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/depth?market="+ticker+"&limit=50", WazirXOrderBook.class);
        orderBooks.put(ticker, generalizeWazirX(orderBook));
        //System.out.println("Making call-> "+"https://api.wazirx.com/uapi/v1/depth?market="+ticker+"&limit=50");
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

    public void createLegBlocks(String startAndEndCurrency){
        String[] currencies=listOfCurrencies.split(",");
        for(int i=0; i<currencies.length-1;i++){
            Leg leg1=createLeg(currencies[i],startAndEndCurrency);
            for(int j=i+1;j<currencies.length;j++){
                Leg leg2=createLeg(currencies[j],currencies[i]);
                Leg leg3=createLeg(startAndEndCurrency,currencies[j]);
                //If direct trading between any of the pairs is not possible, leg would be null and we dont create the block.
                if(leg1!=null && leg2!=null  && leg3!=null)
                    legBlocks.add(Arrays.asList(leg1, leg2, leg3));
            }
        }
    }

    public Leg createLeg(String buyCurrency, String sellCurrency){
        String ticker=(buyCurrency+sellCurrency).toLowerCase();
        String reverseTicker=(sellCurrency+buyCurrency).toLowerCase();
        if(marketTickers.containsKey(ticker)){
            setOfRequiredOrderBooks.add(ticker);
            WazirXMarketTicker tickerDetails = marketTickers.get(ticker);
            return new Leg(ticker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset());
        }
        else if(marketTickers.containsKey(reverseTicker)){
            setOfRequiredOrderBooks.add(reverseTicker);
            WazirXMarketTicker tickerDetails = marketTickers.get(reverseTicker);
            return new Leg(reverseTicker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset());
        }
        return null;
    }

    public void getAllMarketTickers(){
         WazirXMarketTicker[] tickers = restTemplate.getForObject(
                "https://api.wazirx.com/uapi/v1/tickers/24hr", WazirXMarketTicker[].class);
         Arrays.stream(tickers).forEach(ticker-> marketTickers.put(ticker.getSymbol(),ticker));
    }

    public void initializeProcessor(){
        getAllMarketTickers();
        createLegBlocks("INR");
        System.out.println("List of currencies " + listOfCurrencies.toString());
        legBlocks.forEach(block-> {
            System.out.println("Block created: ");
            System.out.println(block.get(0).toString());
            System.out.println(block.get(1).toString());
            System.out.println(block.get(2).toString());
        });
    }

    public void startProcessing(){
        orderBooks=new HashMap<>();

        //fetchOrderBooks();
        //System.out.println("Start Cycle...");
        legBlocks.forEach(block-> processLegs(block.get(0), block.get(1), block.get(2), 2000, 0.998));
        //System.out.println("Cycle Complete...");
    }
}
