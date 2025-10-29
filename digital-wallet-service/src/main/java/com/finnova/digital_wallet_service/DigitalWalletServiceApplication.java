package com.finnova.digital_wallet_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class DigitalWalletServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalWalletServiceApplication.class, args);
	}

}
