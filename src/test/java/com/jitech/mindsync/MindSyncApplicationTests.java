package com.jitech.mindsync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = TestJwtPropertiesInitializer.class)
@ActiveProfiles("test")
class MindSyncApplicationTests {

	@Test
	void contextLoads() {
	}

}
