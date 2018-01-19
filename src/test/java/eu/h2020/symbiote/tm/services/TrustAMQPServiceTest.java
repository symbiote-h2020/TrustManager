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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.federation.model.FederationHistoryResponse;

@RunWith(SpringRunner.class)
public class TrustAMQPServiceTest {

	@Mock
	private RabbitTemplate template;

	@Mock
	private Queue federationHistoryQueue;

	@InjectMocks
	private final TrustAMQPService service = new TrustAMQPService();

	@Before
	public void setup() throws Exception {
		Mockito.when(federationHistoryQueue.getName()).thenReturn("symbIoTe.federation.get_federation_history");
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
}