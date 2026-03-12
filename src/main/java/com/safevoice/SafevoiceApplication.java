package com.safevoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.safevoice")
public class SafevoiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafevoiceApplication.class, args);
	}

}
