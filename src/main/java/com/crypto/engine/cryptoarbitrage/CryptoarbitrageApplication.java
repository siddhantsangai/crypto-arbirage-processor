package com.crypto.engine.cryptoarbitrage;

import com.crypto.engine.cryptoarbitrage.processor.ProcessorBtcInr;
import com.crypto.engine.cryptoarbitrage.templates.unocoin.UnocoinDataExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CryptoarbitrageApplication {

	@PostConstruct
	public void afterContextInitialized(){

	}


	public static void main(String[] args) throws InterruptedException {
		ApplicationContext context = SpringApplication.run(CryptoarbitrageApplication.class, args);

		//Need to wait for Selenium to establish connection with firebase.
		Thread.sleep(50000);
		ProcessorBtcInr processor = context.getBean(ProcessorBtcInr.class);

		while(true){
			try {
				processor.process();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Thread.sleep(40000);
		}

	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

}
