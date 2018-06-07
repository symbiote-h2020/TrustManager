package eu.h2020.symbiote.tm.interfaces.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;

@RunWith(SpringRunner.class)
public class RestConsumerTest {
	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private final RestConsumer service = new RestConsumer();

	@Before
	public void setup() throws Exception {
		ReflectionTestUtils.setField(service, "monitoringUrl", "https://monitoringUrl");
		ReflectionTestUtils.setField(service, "coreAdUrl", "https://coreAdUrl");
		ReflectionTestUtils.setField(service, "coreBarteringUrl", "https://coreBarteringUrl");
		ReflectionTestUtils.setField(service, "ownPlatformId", "123");
	}

	@Test
	public void testFetchResourceAvailabilityMetrics() throws Exception {
		String resId = "r134";
		List<AggregatedMetrics> resp = new ArrayList<>();
		AggregatedMetrics am = new AggregatedMetrics();
		Map<String, Double> statistics = new HashMap<>();
		statistics.put("avg", 12.0);
		am.setStatistics(statistics);
		resp.add(am);
		Mockito.when(restTemplate.getForObject("https://monitoringUrl?metric=availability&operation=avg&device=" + resId, List.class)).thenReturn(resp);

		Double val = service.fetchResourceAvailabilityMetrics(resId);

		assertEquals(Double.valueOf(12.0), val);
	}

	@Test
	public void testFetchPlatformADStats() throws Exception {
		String platformId = "p134";

		Mockito.when(restTemplate.getForObject("https://coreAdUrl?platformId=" + platformId + "&searchOriginPlatformId=123", Map.class))
				.thenReturn(new HashMap<>());

		Double val = service.fetchPlatformADStats(platformId);

		assertEquals(null, val);
	}

	@Test
	public void testFetchBarteringStats() throws Exception {
		String platformId = "p134";

		Mockito.when(restTemplate.getForObject("https://coreBarteringUrl", Map.class)).thenReturn(new HashMap<>());

		Double val = service.fetchBarteringStats(platformId);

		assertEquals(null, val);
	}
}
