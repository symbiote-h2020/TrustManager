package eu.h2020.symbiote.tm.interfaces.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.security.commons.SecurityConstants;

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
	private AuthManager authManager;

	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Fetch resource availability stats.
	 * 
	 * @param resId
	 *            internal resource ID
	 * @return returns the availability in range 0 - 1
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Double getResourceAvailabilityMetrics(String resId) {
		try {
			String url = monitoringUrl + "?metric=availability&operation=avg&device=" + resId;
			ResponseEntity<List> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authManager.generateRequestHeaders()), List.class);
			if (resp != null && resp.getStatusCode().equals(HttpStatus.OK) && resp.getBody() != null && !resp.getBody().isEmpty()) {
				List<AggregatedMetrics> res = resp.getBody();
				return res.get(0).getStatistics().get("avg");
			}
		} catch (Exception e) {
			logger.warn("Fetching stats from Monitoring failed", e);
		}

		return null;
	}

	/**
	 * Fetch platform anomaly detection stats.
	 * 
	 * @param platformId
	 *            platform ID
	 * @return returns the avg stats
	 */
	@SuppressWarnings({ "rawtypes" })
	public Double getPlatformADStats(String platformId) {
		try {
			String url = coreAdUrl + "?platformId=" + platformId + "&searchOriginPlatformId=" + ownPlatformId;
			ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authManager.generateRequestHeaders()), Map.class);

			if (authManager.verifyResponseHeaders("ad", SecurityConstants.CORE_AAM_INSTANCE_ID, resp.getHeaders())) {
				// TODO: Add logic
			} else {
				logger.warn("Response Header verification failed.");
			}

		} catch (Exception e) {
			logger.warn("Fetching stats from Core AD failed", e);
		}

		return null;
	}

	/**
	 * Fetch platform bartering stats.
	 * 
	 * @param platformId
	 *            platform ID
	 * @return returns the avg stats
	 */
	@SuppressWarnings({ "rawtypes" })
	public Double getBarteringStats(String platformId) {
		try {
			String url = coreBarteringUrl + "?platformId=" + platformId;
			ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authManager.generateRequestHeaders()), Map.class);

			if (authManager.verifyResponseHeaders("ad", SecurityConstants.CORE_AAM_INSTANCE_ID, resp.getHeaders())) {
				// TODO: Add logic
			} else {
				logger.warn("Response Header verification failed.");
			}
		} catch (Exception e) {
			logger.warn("Fetching stats from Core Bartering failed", e);
		}

		return null;
	}
}