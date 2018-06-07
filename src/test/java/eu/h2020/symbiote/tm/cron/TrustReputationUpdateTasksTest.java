package eu.h2020.symbiote.tm.cron;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.tm.services.TrustAMQPService;
import eu.h2020.symbiote.tm.services.TrustCalculationService;

@RunWith(SpringRunner.class)
public class TrustReputationUpdateTasksTest {
	@Mock
	private TrustAMQPService amqpService;

	@Mock
	private TrustCalculationService trustService;

	@Mock
	private TrustRepository trustRepository;

	@InjectMocks
	private final TrustReputationUpdateTasks service = new TrustReputationUpdateTasks();

	@Before
	public void setup() throws Exception {
		ReflectionTestUtils.setField(service, "interval", 30);
	}

	@Test
	public void testScheduleResourceTrustUpdate0() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		entries.add(new TrustEntry(Type.RESOURCE_TRUST, "p-1", "sr-1"));
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcResourceTrust("sr-1")).thenReturn(55.2);

		service.scheduleResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testScheduleResourceTrustUpdate1() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, "p-1", "sr-2");
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcResourceTrust("sr-2")).thenReturn(44.0);

		service.scheduleResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.never()).publishResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testScheduleResourceTrustUpdate2() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, "p-1", "sr-3");
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcResourceTrust("sr-3")).thenReturn(44.01);

		service.scheduleResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testSchedulePlatformReputationUpdate0() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		entries.add(new TrustEntry(Type.PLATFORM_REPUTATION, "p-1", null));
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcPlatformReputation("p-1")).thenReturn(55.2);

		service.schedulePlatformReputationUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishPlatformReputationUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testSchedulePlatformReputationUpdate1() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.PLATFORM_REPUTATION, "p-2", null);
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcPlatformReputation("p-2")).thenReturn(44.0);

		service.schedulePlatformReputationUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.never()).publishPlatformReputationUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testSchedulePlatformReputationUpdate2() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.PLATFORM_REPUTATION, "p-3", null);
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcPlatformReputation("p-3")).thenReturn(44.01);

		service.schedulePlatformReputationUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishPlatformReputationUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testScheduleAdaptiveResourceTrustUpdate0() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.ADAPTIVE_RESOURCE_TRUST, "p-1", "sr-1");
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcAdaptiveResourceTrust(te.getValue(), te.getResourceId(), te.getPlatformId())).thenReturn(44.0);

		service.scheduleAdaptiveResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishAdaptiveResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testScheduleAdaptiveResourceTrustUpdate1() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.ADAPTIVE_RESOURCE_TRUST, "p-1", "sr-1");
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcAdaptiveResourceTrust(te.getValue(), te.getResourceId(), te.getPlatformId())).thenReturn(44.0);

		service.scheduleAdaptiveResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.never()).publishAdaptiveResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}

	@Test
	public void testScheduleAdaptiveResourceTrustUpdate2() throws Exception {
		List<TrustEntry> entries = new ArrayList<>();
		TrustEntry te = new TrustEntry(Type.ADAPTIVE_RESOURCE_TRUST, "p-1", "sr-1");
		te.setValue(44.0);
		entries.add(te);
		Mockito.when(trustRepository.findEntriesUpdatedAfter(Mockito.any(Date.class), Mockito.any(Type.class))).thenReturn(entries);
		Mockito.when(trustService.calcAdaptiveResourceTrust(te.getValue(), te.getResourceId(), te.getPlatformId())).thenReturn(44.1);

		service.scheduleAdaptiveResourceTrustUpdate();

		Mockito.verify(trustRepository, Mockito.times(1)).save(Mockito.any(TrustEntry.class));
		Mockito.verify(amqpService, Mockito.times(1)).publishAdaptiveResourceTrustUpdate(Mockito.any(TrustEntry.class));
	}
}
