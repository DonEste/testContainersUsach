package com.example.test_containers_usach;

import org.springframework.boot.SpringApplication;

public class TestTestContainersUsachApplication {

	public static void main(String[] args) {
		SpringApplication.from(TestContainersUsachApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
