package com.crypto.engine.cryptoarbitrage.dao;

public class TransactionNode {

    private double bid;
    private double ask;
    private double volume;

    public TransactionNode(double bid, double ask, double volume) {
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
    }

}
