package com.crypto.engine.cryptoarbitrage;

import com.crypto.engine.cryptoarbitrage.triangularprocessors.binance.UniversalTriangularArbitrageProcessor;
import com.crypto.engine.cryptoarbitrage.triangularprocessors.statics.LegBlockInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CryptoarbitrageApplication {

	public static void main(String[] args) throws InterruptedException {
		ApplicationContext context = SpringApplication.run(CryptoarbitrageApplication.class, args);
		UniversalTriangularArbitrageProcessor processor = context.getBean(UniversalTriangularArbitrageProcessor.class);
		processor.initializeProcessor();
		while(true){
			try {
				processor.startProcessing();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(400);
		}
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
