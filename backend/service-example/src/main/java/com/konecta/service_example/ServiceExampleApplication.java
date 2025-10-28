package com.konecta.service_example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ServiceExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceExampleApplication.class, args);
	}

}
