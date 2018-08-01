package eu.h2020.symbiote.tm.services;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.sla.model.Violation;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.tm.interfaces.rest.TrustStatsLoader;
import eu.h2020.symbiote.tm.repositories.SLAViolationRepository;
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
	private TrustStatsLoader trustStatsLoader;

	@Autowired
	private TrustRepository trustRepository;

	@Autowired
	private SLAViolationRepository violationRepository;

	/**
	 * Calculates resource trust for given internal resource ID.
	 * 
	 * @param resId
	 *            internal resource ID
	 * @return resource trust value double value between 0 - 100 or null if not specified.
	 */
	public Double calcResourceTrust(String resId) {
		Double mS = getMonitoringScore(resId);
		Double rt = mS != null ? mS * calcViolationFactor(resId) : null;

		return formatValue(rt);
	}

	private Double getMonitoringScore(String resId) {
		Double availScore = trustStatsLoader.getResourceAvailabilityMetrics(resId);
		return availScore != null ? availScore * 100 : null;
	}

	private Double calcViolationFactor(String resId) {
		Calendar receivedAfter = Calendar.getInstance();
		// use 24h period
		receivedAfter.add(Calendar.HOUR, -24);
		List<Violation> vList = violationRepository.findRecentViolationsByResourceId(receivedAfter.getTime(), resId);

		int cnt = vList != null ? vList.size() : 0;

		if (cnt < 5)
			return 1.0;

		if (cnt < 10)
			return 0.95;

		if (cnt < 15)
			return 0.8;

		if (cnt < 25)
			return 0.6;

		if (cnt < 40)
			return 0.3;

		return 0.1;
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
		Double prVal = 0.0;
		int cnt = 0;
		Double fhScore = getFederationHistoryScore(platformId);
		Double btScore = getBarteringScore(platformId);
		Double adScore = getADStatsScore(platformId);

		if (fhScore != null) {
			int fhFactor = 10;
			prVal += fhScore * fhFactor;
			cnt += fhFactor;
		}
		if (btScore != null) {
			int btFactor = 5;
			prVal += btScore * btFactor;
			cnt += btFactor;
		}
		if (adScore != null) {
			int adFactor = 1;
			prVal += adScore * adFactor;
			cnt += adFactor;
		}

		return cnt > 0 ? formatValue(prVal / cnt) : null;
	}

	private Double getADStatsScore(String platformId) {
		Integer adHits = trustStatsLoader.getPlatformADStats(platformId);
		return adHits != null ? calcADMetric(adHits) : null;
	}

	private Double calcADMetric(Integer prValue) {
		if (prValue.compareTo(10) < 0)
			return 100.0;

		if (prValue.compareTo(100) < 0)
			return 95.0;

		if (prValue.compareTo(1000) < 0)
			return 80.0;

		if (prValue.compareTo(10000) < 0)
			return 60.0;

		if (prValue.compareTo(100000) < 0)
			return 30.0;

		return 10.0;
	}

	private Double getBarteringScore(String platformId) {
		Calendar sincePeriod = Calendar.getInstance();
		sincePeriod.add(Calendar.HOUR, -12);

		Integer btVal = trustStatsLoader.getBarteringStats(platformId, sincePeriod.getTime());
		return btVal != null ? calcBTMetric(btVal) : null;
	}

	private Double calcBTMetric(Integer bValue) {
		if (bValue.compareTo(100) > 0)
			return 100.0;

		if (bValue.compareTo(50) > 0)
			return 95.0;

		if (bValue.compareTo(25) > 0)
			return 80.0;

		if (bValue.compareTo(12) > 0)
			return 60.0;

		if (bValue.compareTo(6) > 0)
			return 30.0;

		return 10.0;
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
