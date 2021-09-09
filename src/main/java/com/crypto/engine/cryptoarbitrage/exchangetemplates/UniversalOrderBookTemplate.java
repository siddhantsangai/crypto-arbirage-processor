package com.crypto.engine.cryptoarbitrage.exchangetemplates;

import java.util.Map;

public class UniversalOrderBookTemplate {

    private UniversalOrderBook data;
    private String message;

    public UniversalOrderBook getData() {
        return data;
    }

    public void setData(UniversalOrderBook data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}