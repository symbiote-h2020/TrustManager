package eu.h2020.symbiote.tm.interfaces.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;

/**
 * @author RuggenthalerC
 *
 *         Handles fetching data via REST endpoints from other components.
 */
@Service
public class RestConsumer {

	@Value("${symbIoTe.monitoring.url}")
	private String monitoringUrl;

	@Value("${symbIoTe.core.ad.url}")
	private String coreAdUrl;

	@Value("${symbIoTe.core.bartering.url}")
	private String coreBarteringUrl;

	@Value("${platform.id")
	private String ownPlatformId;

	@Autowired
	private RestTemplate restTemplate;

	public Double fetchResourceAvailabilityMetrics(String resId) {
		List<AggregatedMetrics> resp = restTemplate.getForObject(monitoringUrl + "?device=" + resId, List.class);
		AggregatedMetrics am = resp.get(0);

		return null;
	}

	public Double fetchPlatformADStats(String platformId) {
		Map resp = restTemplate.getForObject(coreAdUrl + "?platformId=" + platformId + "&searchOriginPlatformId=" + ownPlatformId, Map.class);

		return null;
	}

	public Double fetchBarteringStats(String platformId) {
		// Map resp = restTemplate.getForObject(coreBarteringUrl + "?platformId=" + platformId + "&searchOriginPlatformId=" + ownPlatformId, Map.class);

		return null;
	}
}
