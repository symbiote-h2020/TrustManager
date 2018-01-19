package eu.h2020.symbiote.tm;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest()
@TestPropertySource(locations = "classpath:test.properties")
public class TrustManagerTests {

	@Ignore
	@Test
	public void contextLoads() {
		// intentionally empty
	}
}