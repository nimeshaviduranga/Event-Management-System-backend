package com.eventmanagement;

import org.springframework.boot.SpringApplication;

public class TestEventManagementSystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(EventManagementSystemBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
