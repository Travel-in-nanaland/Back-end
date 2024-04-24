package com.jeju.nanaland;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(properties = "classpath:application-test.yml")
class NanalandApplicationTests {

  @Test
  void contextLoads() {
  }

}
