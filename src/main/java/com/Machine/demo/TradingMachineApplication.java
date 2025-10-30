package com.Machine.demo;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class TradingMachineApplication {
	
	    public static void main(String[] args) {
	        SpringApplication.run(TradingMachineApplication.class, args);
	    }
	}
