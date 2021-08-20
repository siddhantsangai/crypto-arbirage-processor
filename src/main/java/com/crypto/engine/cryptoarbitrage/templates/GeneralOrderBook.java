package com.crypto.engine.cryptoarbitrage.templates;

import java.util.Map;

public class GeneralOrderBook {
    private Map<Double, Double> asks;
    private Map<Double, Double> bids;

    public Map<Double, Double> getAsks() {
        return asks;
    }

    public void setAsks(Map<Double, Double> asks) {
        this.asks = asks;
    }

    public Map<Double, Double> getBids() {
        return bids;
    }

    public void setBids(Map<Double, Double> bids) {
        this.bids = bids;
    }
}
