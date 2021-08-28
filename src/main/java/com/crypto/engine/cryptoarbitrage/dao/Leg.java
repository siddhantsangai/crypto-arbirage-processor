package com.crypto.engine.cryptoarbitrage.dao;

public class Leg {
    private String ticker;
    private String buyCurrency;
    private String sellCurrency;
    private String quoteCurrency;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getBuyCurrency() {
        return buyCurrency;
    }

    public void setBuyCurrency(String buyCurrency) {
        this.buyCurrency = buyCurrency;
    }

    public String getSellCurrency() {
        return sellCurrency;
    }

    public void setSellCurrency(String sellCurrency) {
        this.sellCurrency = sellCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public Leg(String ticker, String buyCurrency, String sellCurrency, String quoteCurrency) {
        this.ticker = ticker;
        this.buyCurrency = buyCurrency;
        this.sellCurrency = sellCurrency;
        this.quoteCurrency = quoteCurrency;
    }

    @Override
    public String toString() {
        return "Leg{" +
                "ticker='" + ticker + '\'' +
                ", buyCurrency='" + buyCurrency + '\'' +
                ", sellCurrency='" + sellCurrency + '\'' +
                ", quoteCurrency='" + quoteCurrency + '\'' +
                '}';
    }
}
