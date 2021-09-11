package com.crypto.engine.cryptoarbitrage.triangularprocessor.statics;

import com.crypto.engine.cryptoarbitrage.dao.Leg;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTicker;
import com.crypto.engine.cryptoarbitrage.exchangetemplates.UniversalMarketTickerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class TriangularArbitrageLegBlockInitializer {
    @Value("${currencies}")
    private String listOfCurrencies;

    @Value("${listOfStartAndEndCurrencies}")
    private String listOfStartAndEndCurrencies;

    @Value("${exchanges}")
    private String listOfExchanges;

    @Autowired
    private RestTemplate restTemplate;
    private HashMap<String, UniversalMarketTicker> marketTickers;
    private List<List<Leg>> legBlocks = new ArrayList<>();

    private Set<String> listOfSymbols;

    public List<List<Leg>> getLegBlocks() {
        return legBlocks;
    }

    public void createLegBlocks(String startAndEndCurrency, String exchange){
        String[] currencies=listOfCurrencies.split(",");
        for(int i=0; i<currencies.length-1;i++){
            Leg leg1=createLeg(currencies[i],startAndEndCurrency,exchange);
            for(int j=i+1;j<currencies.length;j++){
                Leg leg2=createLeg(currencies[j],currencies[i],exchange);
                Leg leg3=createLeg(startAndEndCurrency,currencies[j],exchange);
                //If direct trading between any of the pairs is not possible, leg would be null and we don't create the block.
                if(leg1!=null && leg2!=null  && leg3!=null)
                    legBlocks.add(Arrays.asList(leg1, leg2, leg3));
            }
        }
    }

    public Leg createLeg(String buyCurrency, String sellCurrency, String exchange){
        String ticker=(buyCurrency+sellCurrency);
        String reverseTicker=(sellCurrency+buyCurrency);
        if(marketTickers.containsKey(ticker)){
            UniversalMarketTicker tickerDetails = marketTickers.get(ticker);
            return new Leg(ticker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset(),exchange);
        }
        else if(marketTickers.containsKey(reverseTicker)){
            UniversalMarketTicker tickerDetails = marketTickers.get(reverseTicker);
            return new Leg(reverseTicker,buyCurrency,sellCurrency,tickerDetails.getQuoteAsset(),exchange);
        }
        return null;
    }

    public void getAllMarketTickers(String exchange){
        UniversalMarketTickerTemplate tickerData = restTemplate.getForObject(
                String.format("http://localhost:3000/%s/listings",exchange), UniversalMarketTickerTemplate.class);
        tickerData.getData().forEach(ticker-> marketTickers.put(ticker.getSymbol(),ticker));
    }

    public void initializeLegBlocks(){
        String[] startAndEndCurrencies = listOfStartAndEndCurrencies.split(",");
        Arrays.stream(listOfExchanges.split(",")).forEach(exchange -> {
            marketTickers=new HashMap<>();
            getAllMarketTickers(exchange);
            Arrays.stream(startAndEndCurrencies)
                        .forEach(currency -> createLegBlocks(currency,exchange));
        });
        System.out.println("List of arbitrage currency candidates " + listOfCurrencies.toString());
        System.out.println("List of start and end currencies " + listOfStartAndEndCurrencies);

        legBlocks.forEach(block-> {
            System.out.println("Block created: ");
            System.out.println(block.get(0).toString());
            System.out.println(block.get(1).toString());
            System.out.println(block.get(2).toString());
        });

        //TODO fetching unique tickers per exchange - for addition to VS config
        Arrays.stream(listOfExchanges.split(",")).forEach(exchange -> {
            listOfSymbols=new HashSet<>();
            legBlocks.forEach(block-> {
                if(block.get(0).getExchange().equals(exchange))
                    listOfSymbols.add(block.get(0).getTicker());
                if(block.get(1).getExchange().equals(exchange))
                    listOfSymbols.add(block.get(1).getTicker());
                if(block.get(2).getExchange().equals(exchange))
                    listOfSymbols.add(block.get(2).getTicker());
            });
            System.out.println("Tickers for exchange - " + exchange + ": " + listOfSymbols.toString());
        });
    }
}
