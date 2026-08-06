package com.task_managment_api.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("=== DIAGNOSTIC: SPRING_DATASOURCE_URL is: " + System.getenv("SPRING_DATASOURCE_URL") + " ===");
		SpringApplication.run(DemoApplication.class, args);
	}

}
