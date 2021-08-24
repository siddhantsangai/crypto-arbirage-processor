package com.crypto.engine.cryptoarbitrage.templates.unocoin;

import com.crypto.engine.cryptoarbitrage.templates.GeneralOrderBook;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class UnocoinCachedResponse {
    Instant lastUpdateTime = Instant.now();
    GeneralOrderBook generalOrderBook;

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public GeneralOrderBook getGeneralOrderBook() {
        Instant currentTime = Instant.now();
        Duration timeElapsed = Duration.between(getLastUpdateTime(), currentTime);
        if(timeElapsed.toSeconds() > 60 )
        {
            throw new IllegalStateException("OrderBook data not up to date.");
        }

        return generalOrderBook;
    }

    public void setGeneralOrderBook(GeneralOrderBook generalOrderBook) {
        this.generalOrderBook = generalOrderBook;
        setLastUpdateTime(Instant.now());
    }
}
