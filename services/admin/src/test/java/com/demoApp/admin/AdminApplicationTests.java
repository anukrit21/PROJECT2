package com.demoApp.admin;

import com.demoApp.admin.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
class AdminApplicationTests {

	@Test
	void contextLoads() {
	}

}
