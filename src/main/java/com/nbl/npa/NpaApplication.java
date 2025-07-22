package com.nbl.npa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(NpaApplication.class, args);
	}

}
