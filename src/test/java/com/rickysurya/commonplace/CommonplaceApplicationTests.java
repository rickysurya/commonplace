package com.rickysurya.commonplace;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest
class CommonplaceApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer  postgres = new PostgreSQLContainer("pgvector/pgvector:pg16");

	@Test
	void contextLoads() {
	}

}
