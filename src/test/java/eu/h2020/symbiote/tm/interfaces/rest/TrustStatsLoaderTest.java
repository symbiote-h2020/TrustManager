package eu.h2020.symbiote.tm.interfaces.rest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import eu.h2020.symbiote.barteringAndTrading.FilterResponse;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;

@RunWith(SpringRunner.class)
public class TrustStatsLoaderTest {
	@Mock
	private RestTemplate restTemplate;

	@Mock
	private AuthManager authManager;

	@InjectMocks
	private final TrustStatsLoader service = new TrustStatsLoader();

	@Before
	public void setup() throws Exception {
		ReflectionTestUtils.setField(service, "monitoringUrl", "https://monitoringUrl");
		ReflectionTestUtils.setField(service, "coreBarteringUrl", "https://coreBarteringUrl");
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
		Mockito.when(restTemplate.exchange("https://monitoringUrl?metric=availability&operation=avg&device=" + resId, HttpMethod.GET, new HttpEntity<>(null),
				List.class)).thenReturn(new ResponseEntity<List>(resp, HttpStatus.OK));

		Double val = service.getResourceAvailabilityMetrics(resId);

		assertEquals(Double.valueOf(12.0), val);
	}

	@Test
	public void testFetchBarteringStats() throws Exception {
		String platformId = "p134";

		List<FilterResponse> resp = new ArrayList<>();
		resp.add(new FilterResponse());
		resp.add(new FilterResponse());
		resp.add(new FilterResponse());

		Mockito.when(authManager.verifyResponseHeaders(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(true);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
				.thenReturn(new ResponseEntity<List>(resp, HttpStatus.OK));

		Integer val = service.getBarteringStats(platformId, new Date());

		assertEquals(Integer.valueOf(3), val);
	}

	@Test
	public void testFetchBarteringStatsEmpty() throws Exception {
		String platformId = "p134";
		Mockito.when(authManager.verifyResponseHeaders(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(true);
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class)))
				.thenReturn(new ResponseEntity<List>(new ArrayList<>(), HttpStatus.OK))
				.thenReturn(new ResponseEntity<List>(new ArrayList<>(), HttpStatus.BAD_GATEWAY));

		Integer val = service.getBarteringStats(platformId, new Date());
		assertEquals(Integer.valueOf(0), val);

		val = service.getBarteringStats(platformId, new Date());
		assertEquals(null, val);
	}
}
