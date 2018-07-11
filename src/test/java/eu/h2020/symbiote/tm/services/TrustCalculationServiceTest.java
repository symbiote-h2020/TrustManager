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
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.tm.interfaces.rest.TrustStatsLoader;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

@RunWith(SpringRunner.class)
public class TrustCalculationServiceTest {

	@Mock
	private TrustAMQPService amqpService;

	@Mock
	private TrustStatsLoader trustStatsLoader;

	@Mock
	private TrustRepository repository;

	@InjectMocks
	private final TrustCalculationService service = new TrustCalculationService();

	@Test
	public void testGetPlatformReputationNull() {
		String pId = "p-123";
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(null).thenReturn(new ArrayList<>());
		Mockito.when(trustStatsLoader.getBarteringStats(Mockito.anyString(), Mockito.any())).thenReturn(null);
		Mockito.when(trustStatsLoader.getPlatformADStats(Mockito.anyString())).thenReturn(null);

		Double val = service.calcPlatformReputation(pId);

		Mockito.verify(amqpService, Mockito.times(1)).fetchFederationHistory(pId);
		assertEquals(null, val);

		val = service.calcPlatformReputation(pId);
		assertEquals(null, val);
	}

	@Test
	public void testGetPlatformReputationFh() {
		String pId = "p-123";

		List<FederationHistory> fhList = generateHistory();
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(fhList);
		Mockito.when(trustStatsLoader.getBarteringStats(Mockito.anyString(), Mockito.any())).thenReturn(null);
		Mockito.when(trustStatsLoader.getPlatformADStats(Mockito.anyString())).thenReturn(null);

		Double val = service.calcPlatformReputation(pId);
		Mockito.verify(amqpService, Mockito.times(1)).fetchFederationHistory(pId);
		assertEquals(Double.valueOf(10), val);
	}

	@Test
	public void testGetPlatformReputationBt() {
		String pId = "p-123";
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(null);
		Mockito.when(trustStatsLoader.getBarteringStats(Mockito.anyString(), Mockito.any())).thenReturn(101).thenReturn(51).thenReturn(26).thenReturn(13)
				.thenReturn(7).thenReturn(6).thenReturn(null);
		Mockito.when(trustStatsLoader.getPlatformADStats(Mockito.anyString())).thenReturn(null);

		assertEquals(Double.valueOf(100), service.calcPlatformReputation(pId));
		assertEquals(Double.valueOf(95), service.calcPlatformReputation(pId));
		assertEquals(Double.valueOf(80), service.calcPlatformReputation(pId));
		assertEquals(Double.valueOf(60), service.calcPlatformReputation(pId));
		assertEquals(Double.valueOf(30), service.calcPlatformReputation(pId));
		assertEquals(Double.valueOf(10), service.calcPlatformReputation(pId));
		assertEquals(null, service.calcPlatformReputation(pId));
	}

	@Test
	public void testGetPlatformReputationAd() {
		String pId = "p-123";
		Mockito.when(amqpService.fetchFederationHistory(Mockito.anyString())).thenReturn(null);
		Mockito.when(trustStatsLoader.getBarteringStats(Mockito.anyString(), Mockito.any())).thenReturn(null);
		Mockito.when(trustStatsLoader.getPlatformADStats(Mockito.anyString())).thenReturn(9).thenReturn(99).thenReturn(999).thenReturn(9999).thenReturn(99999)
				.thenReturn(100000);

		Double val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(100), val);
		val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(95), val);
		val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(80), val);
		val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(60), val);
		val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(30), val);
		val = service.calcPlatformReputation(pId);
		assertEquals(Double.valueOf(10), val);
	}

	@Test
	public void testCalcResourceTrust() {
		Mockito.when(trustStatsLoader.getResourceAvailabilityMetrics(Mockito.anyString())).thenReturn(null).thenReturn(0.8);

		Double val = service.calcResourceTrust("r-123");
		assertEquals(null, val);

		val = service.calcResourceTrust("r-123");
		assertEquals(Double.valueOf(80.0), val);
	}

	@Test
	public void testCalcAdaptiveResourceTrustEmpty() {
		Mockito.when(repository.getRTEntryByResourceId(Mockito.anyString())).thenReturn(null).thenReturn(new TrustEntry());
		Mockito.when(repository.getPREntryByPlatformId(Mockito.anyString())).thenReturn(null).thenReturn(new TrustEntry());
		Double val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(null, val);
		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(null, val);
		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(null, val);
		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(null, val);
	}

	@Test
	public void testCalcAdaptiveResourceTrustCalc() {
		TrustEntry rt = new TrustEntry();
		rt.updateEntry(10.0);
		Mockito.when(repository.getRTEntryByResourceId(Mockito.anyString())).thenReturn(rt);

		TrustEntry pr1 = new TrustEntry();
		pr1.updateEntry(90.1);
		TrustEntry pr2 = new TrustEntry();
		pr2.updateEntry(70.1);
		TrustEntry pr3 = new TrustEntry();
		pr3.updateEntry(50.1);
		TrustEntry pr4 = new TrustEntry();
		pr4.updateEntry(30.1);
		TrustEntry pr5 = new TrustEntry();
		pr5.updateEntry(10.1);
		TrustEntry pr6 = new TrustEntry();
		pr6.updateEntry(10.0);

		Mockito.when(repository.getPREntryByPlatformId(Mockito.anyString())).thenReturn(pr1).thenReturn(pr2).thenReturn(pr3).thenReturn(pr4).thenReturn(pr5)
				.thenReturn(pr6);
		Double val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(25), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(24.75), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(24.0), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(23.0), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(21.5), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(20.5), val);

		val = service.calcAdaptiveResourceTrust(40.0, "p-123", "r-123");
		assertEquals(Double.valueOf(20.5), val);
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