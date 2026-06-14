package com.trip.staysync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StaySyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(StaySyncApplication.class, args);
	}

}
