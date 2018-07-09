package eu.h2020.symbiote.tm.interfaces.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import eu.h2020.symbiote.security.communication.payloads.OriginPlatformGroupedPlatformMisdeedsReport;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;

/**
 * @author RuggenthalerC
 *
 *         Loads / fetching data via REST from other components for trust/reputation calculation.
 */
@Service
public class TrustStatsLoader {
	private static final Logger logger = LoggerFactory.getLogger(TrustStatsLoader.class);

	@Value("${symbIoTe.monitoring.url}")
	private String monitoringUrl;

	@Value("${symbIoTe.core.bartering.url}")
	private String coreBarteringUrl;

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
			} else {
				logger.warn("Invalid response received: ", resp);
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
	 * @return returns the number of hits.
	 */
	public Integer getPlatformADStats(String platformId) {
		try {
			IComponentSecurityHandler csh = authManager.getSecurityHandler();
			Map<String, OriginPlatformGroupedPlatformMisdeedsReport> resp = csh.getOriginPlatformGroupedPlatformMisdeedsReports(Optional.of(platformId), null);

			if (resp != null && resp.get(platformId) != null) {
				return resp.get(platformId).getTotalMisdeeds();
			} else {
				logger.warn("Invalid response received: ", resp);
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