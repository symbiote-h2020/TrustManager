package eu.h2020.symbiote.tm.interfaces.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger logger = LoggerFactory.getLogger(RestConsumer.class);

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

	/**
	 * @param resId
	 *            internal resource ID
	 * @return returns the availability in range 0 - 1
	 */
	public Double fetchResourceAvailabilityMetrics(String resId) {
		try {
			List<AggregatedMetrics> resp = restTemplate.getForObject(monitoringUrl + "?metric=availability&operation=avg&device=" + resId, List.class);

			if (resp != null && !resp.isEmpty()) {
				return resp.get(0).getStatistics().get("avg");
			}
		} catch (Exception e) {
			logger.warn("Fetching stats from Monitoring failed", e);
		}

		return null;
	}

	public Double fetchPlatformADStats(String platformId) {
		try {
			Map resp = restTemplate.getForObject(coreAdUrl + "?platformId=" + platformId + "&searchOriginPlatformId=" + ownPlatformId, Map.class);
		} catch (Exception e) {
			logger.warn("Fetching stats from Core AD failed", e);
		}

		return null;
	}

	public Double fetchBarteringStats(String platformId) {
		try {
			// Map resp = restTemplate.getForObject(coreBarteringUrl + "?platformId=" + platformId + "&searchOriginPlatformId=" + ownPlatformId, Map.class);
		} catch (Exception e) {
			logger.warn("Fetching stats from Core Bartering failed", e);
		}

		return null;
	}
}
