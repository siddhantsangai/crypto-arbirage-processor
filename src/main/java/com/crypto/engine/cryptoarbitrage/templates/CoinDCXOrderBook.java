package com.crypto.engine.cryptoarbitrage.templates;

import java.util.Map;

public class CoinDCXOrderBook {

    private Map<String, String> bids;
    private Map<String, String> asks;

    public Map<String, String> getBids() {
        return bids;
    }

    public Map<String, String> getAsks() {
        return asks;
    }

    public void setBids(Map<String, String> bids) {
        this.bids = bids;
    }

    public void setAsks(Map<String, String> asks) {
        this.asks = asks;
    }

}
