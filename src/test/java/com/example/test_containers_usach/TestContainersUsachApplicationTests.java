package com.example.test_containers_usach;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TestContainersUsachApplicationTests {

	@Test
	void contextLoads() {
	}

}
