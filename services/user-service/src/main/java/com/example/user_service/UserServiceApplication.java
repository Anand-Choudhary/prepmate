package com.example.user_service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

    @Bean
    public ApplicationRunner listEndpoints(RequestMappingHandlerMapping mapping) {
        return args -> {
            System.out.println("\n========================================");
            System.out.println("REGISTERED ENDPOINTS:");
            System.out.println("========================================");
            mapping.getHandlerMethods().forEach((info, method) -> {
                System.out.println(info);
            });
            System.out.println("========================================\n");
        };
    }

}
