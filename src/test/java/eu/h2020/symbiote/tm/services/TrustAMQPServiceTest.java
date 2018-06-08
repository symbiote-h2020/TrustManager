package eu.h2020.symbiote.tm.services;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.federation.model.FederationHistoryResponse;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;

@RunWith(SpringRunner.class)
public class TrustAMQPServiceTest {

	@Mock
	private RabbitTemplate template;

	@Mock
	private Queue federationHistoryQueue;

	@Mock
	private TopicExchange trustTopic;

	@InjectMocks
	private final TrustAMQPService service = new TrustAMQPService();

	@Before
	public void setup() throws Exception {
		Mockito.when(federationHistoryQueue.getName()).thenReturn("symbIoTe.federation.get_federation_history");
		Mockito.when(trustTopic.getName()).thenReturn("trustTopic");
		ReflectionTestUtils.setField(service, "routingKeyResTrustUpdated", "routingKeyResTrustUpdated");
		ReflectionTestUtils.setField(service, "routingKeyPlatfRepUpdated", "routingKeyPlatfRepUpdated");
		ReflectionTestUtils.setField(service, "routingKeyAdaptiveResTrustUpdated", "routingKeyAdaptiveResTrustUpdated");
	}

	@Test
	public void testFetchFederationHistoryFail() throws Exception {
		String pId = "p-123";
		Mockito.when(template.convertSendAndReceive(Mockito.anyString(), Mockito.anyString())).thenReturn(null);

		List<FederationHistory> history = service.fetchFederationHistory(pId);

		Mockito.verify(federationHistoryQueue, Mockito.times(1)).getName();
		Mockito.verify(template, Mockito.times(1)).convertSendAndReceive(Mockito.eq("symbIoTe.federation.get_federation_history"), Mockito.eq(pId));

		assertEquals(0, history.size());
	}

	@Test
	public void testFetchFederationHistorySuccess() throws Exception {
		String pId = "p-456";
		FederationHistoryResponse fhr = new FederationHistoryResponse(pId);
		fhr.getEvents().add(new FederationHistory("abc"));
		Mockito.when(template.convertSendAndReceive(Mockito.anyString(), Mockito.anyString())).thenReturn(fhr);

		List<FederationHistory> history = service.fetchFederationHistory(pId);

		Mockito.verify(federationHistoryQueue, Mockito.times(1)).getName();
		Mockito.verify(template, Mockito.times(1)).convertSendAndReceive(Mockito.eq("symbIoTe.federation.get_federation_history"), Mockito.eq(pId));

		assertEquals(1, history.size());
		assertEquals("abc", history.get(0).getFederationId());
	}

	@Test
	public void testPublishResourceTrustUpdate() throws Exception {
		TrustEntry te = new TrustEntry();
		service.publishResourceTrustUpdate(te);

		Mockito.verify(trustTopic, Mockito.times(1)).getName();
		Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.eq("trustTopic"), Mockito.eq("routingKeyResTrustUpdated"), Mockito.eq(te));
	}

	@Test
	public void testPublishPlatformReputationUpdate() throws Exception {
		TrustEntry te = new TrustEntry();
		service.publishPlatformReputationUpdate(te);

		Mockito.verify(trustTopic, Mockito.times(1)).getName();
		Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.eq("trustTopic"), Mockito.eq("routingKeyPlatfRepUpdated"), Mockito.eq(te));
	}

	@Test
	public void testPublishAdaptiveResourceTrustUpdate() throws Exception {
		TrustEntry te = new TrustEntry();
		service.publishAdaptiveResourceTrustUpdate(te);

		Mockito.verify(trustTopic, Mockito.times(1)).getName();
		Mockito.verify(template, Mockito.times(1)).convertAndSend(Mockito.eq("trustTopic"), Mockito.eq("routingKeyAdaptiveResTrustUpdated"), Mockito.eq(te));
	}
}