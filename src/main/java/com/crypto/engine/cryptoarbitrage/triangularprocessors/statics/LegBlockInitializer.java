package com.crypto.engine.cryptoarbitrage.triangularprocessors.statics;

import com.crypto.engine.cryptoarbitrage.dao.Leg;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTicker;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTickerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class LegBlockInitializer {
    @Value("${currencies}")
    private String listOfCurrencies;

    @Value("${listOfStartAndEndCurrencies}")
    private String listOfStartAndEndCurrencies;

    @Autowired
    private RestTemplate restTemplate;
    private HashMap<String, UniversalMarketTicker> marketTickers = new HashMap<>();
    private List<List<Leg>> legBlocks = new ArrayList<>();

    public List<List<Leg>> getLegBlocks() {
        return legBlocks;
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
        String ticker=(buyCurrency+sellCurrency);
        String reverseTicker=(sellCurrency+buyCurrency);
        if(marketTickers.containsKey(ticker)){
            UniversalMarketTicker tickerDetails = marketTickers.get(ticker);
            return new Leg(ticker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset());
        }
        else if(marketTickers.containsKey(reverseTicker)){
            UniversalMarketTicker tickerDetails = marketTickers.get(reverseTicker);
            return new Leg(reverseTicker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset());
        }
        return null;
    }

    public void getAllMarketTickers(){
        UniversalMarketTickerTemplate tickerData = restTemplate.getForObject(
                "http://localhost:3000/binance/listings", UniversalMarketTickerTemplate.class);
        tickerData.getData().forEach(ticker-> marketTickers.put(ticker.getSymbol(),ticker));
    }

    public void initializeLegBlocks(){
        getAllMarketTickers();
        String[] startAndEndCurrencies = listOfStartAndEndCurrencies.split(",");
        Arrays.stream(startAndEndCurrencies).forEach(currency -> createLegBlocks(currency));
        System.out.println("List of arbitrage currency candidates " + listOfCurrencies.toString());
        System.out.println("List of start and end currencies " + listOfStartAndEndCurrencies);
        legBlocks.forEach(block-> {
            System.out.println("Block created: ");
            System.out.println(block.get(0).toString());
            System.out.println(block.get(1).toString());
            System.out.println(block.get(2).toString());
        });
    }
}
