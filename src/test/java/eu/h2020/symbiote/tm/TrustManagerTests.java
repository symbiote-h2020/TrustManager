package eu.h2020.symbiote.tm;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

@RunWith(SpringRunner.class)
@SpringBootTest()
@TestPropertySource(locations = "classpath:test.properties")
public class TrustManagerTests {

	@Autowired
	TrustRepository trustRepository;

	@Ignore
	@Test
	public void insertSampleData() {
		TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, "p-11", "sr-21");
		te.setValue(45.0);

		TrustEntry te1 = new TrustEntry(Type.PLATFORM_REPUTATION, "p-11a", null);
		te.setValue(99.0);

		TrustEntry te2 = new TrustEntry(Type.ADAPTIVE_RESOURCE_TRUST, "p-11b", "sr-21b");
		te.setValue(11.0);

		trustRepository.save(te);
		trustRepository.save(te1);
		trustRepository.save(te2);
	}
}