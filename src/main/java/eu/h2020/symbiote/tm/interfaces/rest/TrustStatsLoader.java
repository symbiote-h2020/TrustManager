package eu.h2020.symbiote.tm.interfaces.rest;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import eu.h2020.symbiote.barteringAndTrading.FilterRequest;
import eu.h2020.symbiote.barteringAndTrading.FilterResponse;
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
	public Double getResourceAvailabilityMetrics(String resId) {
		String url=null;
		try {
			url = monitoringUrl + "?metric=availability&operation=avg&device=" + resId;

			ResponseEntity<List<AggregatedMetrics>> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(authManager.generateRequestHeaders()),
					new ParameterizedTypeReference<List<AggregatedMetrics>>() {
					});

			if (resp.getStatusCode().equals(HttpStatus.OK) && resp.getBody() != null && !resp.getBody().isEmpty()) {
				List<AggregatedMetrics> res = resp.getBody();
				return res.get(0).getStatistics().get("avg");
			} else {
				logger.warn("Invalid response received: ", resp);
			}
		} catch (Exception e) {
			logger.warn("Fetching stats from Monitoring failed");
			logger.warn("The URL used was {}", url);
			logger.warn("The exception thrown was:", e);
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
			logger.warn("Fetching stats from Core AD failed");
			logger.warn("The platformId was {}", platformId);
			logger.warn("The exception thrown was:", e);
		}

		return null;
	}

	/**
	 * Fetch platform bartering stats.
	 * 
	 * @param platformId
	 *            platform ID
	 * @param since
	 *            since Date
	 * @return returns total number of coupons used.
	 */
	public Integer getBarteringStats(String platformId, Date since) {
		try {
			FilterRequest req = new FilterRequest();
			req.setPlatform(platformId);
			req.setBeginTimestamp(since.getTime());
			req.setEndTimestamp(new Date().getTime());

			
			HttpHeaders header=authManager.generateRequestHeaders();
			if (header==null) {	// This is the case during unit testing. It's unclear whether this can also happen at other conditions thus we just issue a warning here
				logger.warn("request header from auth manager is null!!");
				header=new HttpHeaders();
			}
			
			header.add("Content-Type", "application/json");
			
			
			ResponseEntity<List<FilterResponse>> resp = restTemplate.exchange(coreBarteringUrl, HttpMethod.POST,
					new HttpEntity<>(req, header), new ParameterizedTypeReference<List<FilterResponse>>() {
					});

			if (authManager.verifyResponseHeaders("btm", SecurityConstants.CORE_AAM_INSTANCE_ID, resp.getHeaders())) {
				if (resp.getStatusCode().equals(HttpStatus.OK) && resp.getBody() != null) {
					return resp.getBody().size();
				} else {
					logger.warn("Invalid response received: ", resp);
				}
			} else {
				logger.warn("Response Header verification failed.");
			}
		} catch (Exception e) {
			logger.warn("Fetching stats from Core Bartering failed.");
			logger.warn("The URL used for the request was {}, the platformId was {}, the start date used was {}", coreBarteringUrl, platformId, since);
			logger.warn("The exception thrown was:", e);
		}

		return null;
	}
}