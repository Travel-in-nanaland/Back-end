package com.jeju.nanaland;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestPropertySource(locations = "classpath:test/application.yml")
class NanalandApplicationTests {

	@Test
	void contextLoads() {
	}

}
