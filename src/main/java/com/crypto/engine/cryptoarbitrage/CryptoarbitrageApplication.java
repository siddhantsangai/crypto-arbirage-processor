package com.crypto.engine.cryptoarbitrage;

import com.crypto.engine.cryptoarbitrage.processor.ProcessorBtcInr;
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
		ProcessorBtcInr processor = context.getBean(ProcessorBtcInr.class);

		while(true){
			try {
				processor.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(10000);
		}
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
