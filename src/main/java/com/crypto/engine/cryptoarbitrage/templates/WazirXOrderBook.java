package com.crypto.engine.cryptoarbitrage.templates;

public class WazirXOrderBook {

    private long timestamp;
    private String[][] asks;
    private String[][] bids;

    public long getTimestamp() {
        return timestamp;
    }

    public String[][] getAsks() {
        return asks;
    }

    public String[][] getBids() {
        return bids;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAsks(String[][] asks) {
        this.asks = asks;
    }

    public void setBids(String[][] bids) {
        this.bids = bids;
    }
}
