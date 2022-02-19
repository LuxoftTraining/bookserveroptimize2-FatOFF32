package com.luxoft.highperformance.bookserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
public class BookServerApplication {
    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }

    public static void main(String[] args) {
        SpringApplication.run(BookServerApplication.class, args);
    }

}
