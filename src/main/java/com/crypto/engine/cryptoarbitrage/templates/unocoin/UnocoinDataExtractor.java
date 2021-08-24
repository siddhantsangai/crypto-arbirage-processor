package com.crypto.engine.cryptoarbitrage.templates.unocoin;

import com.crypto.engine.cryptoarbitrage.templates.GeneralOrderBook;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.util.HashMap;
import java.util.logging.Level;

@Component
@Scope("application")
public class UnocoinDataExtractor implements Runnable {

    @Autowired
    UnocoinCachedResponse unocoinCachedResponse;

    Logger log = LoggerFactory.getLogger(UnocoinDataExtractor.class);
    private static WebDriver driver;
    private boolean stop = false;


    public void run() {
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "/drivers/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(options);
        driver.navigate().to("http://localhost:8080/");
        LogEntries logEntries;

        while (true && !stop) {
            logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
            boolean foundEntries = logEntries.getAll().size() > 0;
            logEntries.forEach(entry -> {
                JSONObject messageJSON = new JSONObject(entry.getMessage());
                String method = messageJSON.getJSONObject("message").getString("method");
                if (method.equalsIgnoreCase("Network.webSocketFrameReceived")) {
                    String payLoadString = messageJSON.getJSONObject("message").getJSONObject("params").getJSONObject("response").getString("payloadData");
                    log.info("Message Received: " + payLoadString);

                    if (payLoadString.contains("market_makers")) {

                        GeneralOrderBook generalOrderBook = new GeneralOrderBook();
                        HashMap<Double, Double> asks = new HashMap<>();
                        HashMap<Double, Double> bids = new HashMap<>();
                        JSONObject payloadObject = null;

                        try {
                            payloadObject =  new JSONObject(payLoadString).getJSONObject("d").getJSONObject("b").getJSONObject("d");
                        } catch (Exception e){
                            log.error("Error parsing OrderBook: {}" , e.getMessage());
                            payloadObject = new JSONObject();
                        }

//                        some Java stream big
                        JSONObject finalPayloadObject = payloadObject;
                        payloadObject.keySet().forEach(e -> {

                            JSONObject quoteItem = finalPayloadObject.getJSONObject(e);
                            String orderType = quoteItem.getString("order_type");

                            Double rate = quoteItem.getDouble("rate");
                            Double volume = quoteItem.getDouble("volume");

                            if ("BID".equals(orderType)) {
                                bids.put(rate, volume);
                            } else if ("ASK".equals(orderType)) {
                                asks.put(rate, volume);
                            }
                            generalOrderBook.setAsks(asks);
                            generalOrderBook.setBids(bids);
                            unocoinCachedResponse.setGeneralOrderBook(generalOrderBook);
                        });

                    }
                }
            });
            if (foundEntries)
                clearConsoleErrors();
        }
        driver.close();
        driver.quit();
    }

    public void clearConsoleErrors() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "console.clear();";
        js.executeScript(script);
    }
}
