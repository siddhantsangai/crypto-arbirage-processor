package com.crypto.engine.cryptoarbitrage.exchangetemplates;

import java.util.List;
import java.util.Map;

public class UniversalOrderBook {
    //{"data":{"asks":{"28":145,"30":100,"27.16":1000}}}
    private Map<String, Double> asks;
    private Map<String, Double> bids;
    private String ticker;
    private String exchangeSource;
    private long timestamp;

    public Map<String, Double> getAsks() {
        return asks;
    }

    public void setAsks(Map<String, Double> asks) {
        this.asks = asks;
    }

    public Map<String, Double> getBids() {
        return bids;
    }

    public void setBids(Map<String, Double> bids) {
        this.bids = bids;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getExchangeSource() {
        return exchangeSource;
    }

    public void setExchangeSource(String exchangeSource) {
        this.exchangeSource = exchangeSource;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
