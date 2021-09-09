package com.crypto.engine.cryptoarbitrage.exchangetemplates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties
public class UniversalMarketTickerTemplate {

    private List<UniversalMarketTicker> data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String message;

    public List<UniversalMarketTicker> getData() {
        return data;
    }

    public void setData(List<UniversalMarketTicker> data) {
        this.data = data;
    }
}


