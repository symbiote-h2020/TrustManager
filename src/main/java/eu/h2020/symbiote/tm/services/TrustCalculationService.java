package eu.h2020.symbiote.tm.services;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.tm.interfaces.rest.RestConsumer;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

/**
 * @author RuggenthalerC
 * 
 *         Handles the connection to platform RabbitMQ broker for requesting data from other cloud components.
 *
 */
@Service
public class TrustCalculationService {
	private static final Logger logger = LoggerFactory.getLogger(TrustCalculationService.class);

	@Autowired
	private TrustAMQPService amqpService;

	@Autowired
	private RestConsumer restConsumer;

	@Autowired
	private TrustRepository trustRepository;

	/**
	 * Calculates resource trust for given internal resource ID.
	 * 
	 * @param resId
	 *            internal resource ID
	 * @return resource trust value double value between 0 - 100 or null if not specified.
	 */
	public Double calcResourceTrust(String resId) {
		Double rt = getMonitoringScore(resId);
		return formatValue(rt);
	}

	private Double getMonitoringScore(String resId) {
		Double availScore = restConsumer.fetchResourceAvailabilityMetrics(resId);
		return availScore * 100;
	}

	/**
	 * Calculates adaptive resource trust for given symbiote resource ID.
	 * 
	 * @param curArtValue
	 *            current adaptive resource trust value
	 * @param resId
	 *            symbIoTe ID
	 * @param platformId
	 *            platform ID
	 * @return adaptive resource trust value double value between 0 - 100 or null if not specified.
	 */
	public Double calcAdaptiveResourceTrust(Double curArtValue, String resId, String platformId) {
		TrustEntry rtEntry = trustRepository.getRTEntryByResourceId(resId);
		TrustEntry prEntry = trustRepository.getPREntryByPlatformId(platformId);

		if (rtEntry == null || rtEntry.getValue() == null) {
			logger.warn("Shared resource trust for resource {} does not exist", resId);
			return null;
		}

		if (prEntry == null || prEntry.getValue() == null) {
			logger.warn("Platform reputation for platform {} does not exist", platformId);
			return null;
		}

		Double artValue = rtEntry.getValue() * calcConfidenceFactor(prEntry.getValue());
		if (curArtValue != null) {
			artValue = (artValue + curArtValue) / 2;
		}

		return formatValue(artValue);
	}

	private Double calcConfidenceFactor(Double prValue) {
		if (prValue.compareTo(90.0) > 0)
			return 1.0;

		if (prValue.compareTo(70.0) > 0)
			return 0.95;

		if (prValue.compareTo(50.0) > 0)
			return 0.8;

		if (prValue.compareTo(30.0) > 0)
			return 0.6;

		if (prValue.compareTo(10.0) > 0)
			return 0.3;

		return 0.1;
	}

	/**
	 * Calculates platform reputation for given platformId.
	 * 
	 * @param platformId
	 *            platform ID
	 * @return platform reputation value double value between 0 - 100 or null if not specified.
	 */
	public Double calcPlatformReputation(String platformId) {
		Double fhScore = getFederationHistoryScore(platformId);
		Double bScore = getBarteringScore(platformId);
		Double adScore = getADStatsScore(platformId);

		// TODO: Add logic
		return formatValue(fhScore);
	}

	private Double getADStatsScore(String platformId) {
		Double adScore = restConsumer.fetchPlatformADStats(platformId);
		// TODO: Add logic
		return adScore;
	}

	private Double getBarteringScore(String platformId) {
		Double bScore = restConsumer.fetchBarteringStats(platformId);
		// TODO: Add logic
		return bScore;
	}

	private Double getFederationHistoryScore(String platformId) {
		List<FederationHistory> fh = amqpService.fetchFederationHistory(platformId);
		if (fh != null && !fh.isEmpty()) {
			Double score = 0.0;
			for (FederationHistory h : fh) {
				score += calcHistoryEntry(h);
			}
			return score / fh.size() * 100;
		}

		return null;
	}

	private Double calcHistoryEntry(FederationHistory h) {
		Long fedPeriod = h.getDateFederationRemoved() != null ? h.getDateFederationRemoved().getTime() : new Date().getTime();
		fedPeriod = fedPeriod - h.getDateFederationCreated().getTime();

		Long platPeriod = h.getDatePlatformLeft() != null ? h.getDatePlatformLeft().getTime() : new Date().getTime();
		platPeriod = platPeriod - h.getDatePlatformJoined().getTime();

		return platPeriod.doubleValue() / fedPeriod.doubleValue();
	}

	private Double formatValue(Double val) {
		DecimalFormat rounded = new DecimalFormat("#.##");
		return val != null ? Double.valueOf(rounded.format(val)) : null;
	}
}
