package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Works as a microservice client, fetching data from the OMS-Service. Uses the
 * Discovery Server (Eureka) to find the microservice.
 * 
 */
@SpringBootApplication
@EnableAutoConfiguration(
	exclude = VelocityAutoConfiguration.class)
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableCaching
@EnableZuulProxy
@ComponentScan(
		basePackages = { "com.demo" })
@Configuration  
public class AggregatorServer {

	public static void main(String[] args) {
		SpringApplication.run(AggregatorServer.class, args);
	}

	@Bean
	@LoadBalanced
	RestTemplate restTemplateForUserDetails() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		return restTemplate;
	}
}
