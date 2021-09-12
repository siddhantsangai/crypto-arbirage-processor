package com.crypto.engine.cryptoarbitrage.spatialprocessor.statics;

import com.crypto.engine.cryptoarbitrage.dao.Leg;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTicker;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTickerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class SpatialArbitrageLegBlockInitializer {

    @Value("${spatialArbitrage.currencies}")
    private String currencies;

    @Value("${spatialArbitrage.startAndEndCurrencies}")
    private String startAndEndCurrencies;

    @Value("${spatialArbitrage.exchanges}")
    private String exchanges;

    private List<List<String>> exchangePairs;

    @Autowired
    private RestTemplate restTemplate;

    //{ExchangeName: {Ticker: Market Ticker}}
    private Map<String,Map<String, UniversalMarketTicker>> marketTickersPerExchange;

    private List<List<Leg>> legBlocks = new ArrayList<>();


    public List<List<Leg>> getLegBlocks() {
        return legBlocks;
    }

    public void createLegBlocks(String startAndEndCurrency){
        String[] listOfCurrencies=currencies.split(",");
        for(int i=0; i<listOfCurrencies.length-1;i++){
            for (List<String> exchangePair : exchangePairs) {
                //Buy Cryp1 on exchange 1
                Leg leg1=createLeg(listOfCurrencies[i],startAndEndCurrency,exchangePair.get(0));
                Leg leg2=createLeg(startAndEndCurrency,listOfCurrencies[i],exchangePair.get(1));
                if(leg1!=null && leg2!=null)
                    legBlocks.add(Arrays.asList(leg1, leg2));

                //Buy Cryp1 on exchange 2 - creating a mirror block for the above case
                leg1=createLeg(listOfCurrencies[i],startAndEndCurrency,exchangePair.get(1));
                leg2=createLeg(startAndEndCurrency,listOfCurrencies[i],exchangePair.get(0));
                if(leg1!=null && leg2!=null)
                    legBlocks.add(Arrays.asList(leg1, leg2));
            }
        }
    }

    public Leg createLeg(String buyCurrency, String sellCurrency, String exchange){
        String ticker=(buyCurrency+sellCurrency);
        String reverseTicker=(sellCurrency+buyCurrency);
        if(marketTickersPerExchange.get(exchange).containsKey(ticker)){
            UniversalMarketTicker tickerDetails = marketTickersPerExchange.get(exchange).get(ticker);
            return new Leg(ticker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset(),exchange);
        }
        else if(marketTickersPerExchange.get(exchange).containsKey(reverseTicker)){
            UniversalMarketTicker tickerDetails = marketTickersPerExchange.get(exchange).get(reverseTicker);
            return new Leg(reverseTicker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset(),exchange);
        }
        return null;
    }

    public void getAllMarketTickers(String exchange){
        Map<String, UniversalMarketTicker> temp=marketTickersPerExchange.get(exchange);
        UniversalMarketTickerTemplate tickerData = restTemplate.getForObject(
                String.format("http://localhost:3000/%s/listings",exchange), UniversalMarketTickerTemplate.class);
        tickerData.getData().forEach(ticker-> temp.put(ticker.getSymbol(),ticker));
    }

    public void createExchangePairs(){
        String[] listOfExchanges = exchanges.split(",");
        for(int i=0; i<listOfExchanges.length-1; i++){
            for(int j=i+1; j<listOfExchanges.length; j++){
                exchangePairs.add(Arrays.asList(listOfExchanges[i],listOfExchanges[j]));
            }
        }
    }

    public void initializeLegBlocks(){
        createExchangePairs();
        String[] listOfExchanges = exchanges.split(",");
        Arrays.stream(listOfExchanges).forEach(exchange -> {
            marketTickersPerExchange.put(exchange, new HashMap<>());
            getAllMarketTickers(exchange);
        });
        Arrays.stream(startAndEndCurrencies.split(",")).forEach(this::createLegBlocks);
    }
}
