package com.example.bankDemo.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(name = "virtualExecutor")
    public Executor virtualTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}




