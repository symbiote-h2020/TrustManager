package eu.h2020.symbiote.tm.services;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.tm.interfaces.rest.RestConsumer;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

@RunWith(SpringRunner.class)
public class TrustCalculationServiceTest {

	@Mock
	private TrustAMQPService amqpService;

	@Mock
	private RestConsumer restConsumer;

	@Mock
	private TrustRepository repository;

	@InjectMocks
	private final TrustCalculationService service = new TrustCalculationService();

	@Test
	public void TestGetPlatformReputationNull() {
		String pId = "p-123";
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(null);

		Double val = service.calcPlatformReputation(pId);

		Mockito.verify(amqpService, Mockito.times(1)).fetchFederationHistory(pId);
		assertEquals(null, val);
	}

	@Test
	public void TestGetPlatformReputationNoEntries() {
		String pId = "p-123";
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(new ArrayList<>());

		Double val = service.calcPlatformReputation(pId);

		Mockito.verify(amqpService, Mockito.times(1)).fetchFederationHistory(pId);
		assertEquals(null, val);
	}

	@Test
	public void TestGetPlatformReputationEntries() {
		String pId = "p-123";

		List<FederationHistory> fhList = generateHistory();
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(fhList);

		Double val = service.calcPlatformReputation(pId);
		Mockito.verify(amqpService, Mockito.times(1)).fetchFederationHistory(pId);
		assertEquals(Double.valueOf(10), val);
	}

	private List<FederationHistory> generateHistory() {
		List<FederationHistory> fhList = new ArrayList<>();

		FederationHistory fh1 = new FederationHistory("f-1");
		fh1.setDateFederationCreated(new Date(1));
		fh1.setDateFederationRemoved(new Date(101));
		fh1.setDatePlatformJoined(new Date(10));
		fh1.setDatePlatformLeft(new Date(20));
		fhList.add(fh1);

		FederationHistory fh2 = new FederationHistory("f-2");
		fh2.setDateFederationCreated(new Date(1));
		fh2.setDateFederationRemoved(new Date(201));
		fh2.setDatePlatformJoined(new Date(21));
		fh2.setDatePlatformLeft(new Date(41));
		fhList.add(fh2);

		return fhList;
	}
}