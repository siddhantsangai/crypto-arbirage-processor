package com.crypto.engine.cryptoarbitrage;

import com.crypto.engine.cryptoarbitrage.triangularprocessors.WazirXTriangularArbitrageProcessor;
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
		WazirXTriangularArbitrageProcessor processor = context.getBean(WazirXTriangularArbitrageProcessor.class);
		while(true){
			try {
				processor.process(5000);
				//processor.process();
			} catch (Exception e) {
				System.out.println("429 : [{\"message\":\"Too Many Requests\"}]");
			}
			Thread.sleep(4000);
		}
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
