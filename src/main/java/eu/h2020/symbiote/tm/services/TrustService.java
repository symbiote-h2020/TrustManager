package eu.h2020.symbiote.tm.services;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.tm.model.TrustEntry;
import eu.h2020.symbiote.tm.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;

/**
 * @author RuggenthalerC
 * 
 *         Handles the connection to platform RabbitMQ broker for requesting data from other cloud components.
 *
 */
@Service
public class TrustService {
	private static final Logger logger = LoggerFactory.getLogger(TrustService.class);

	@Autowired
	private TrustAMQPService amqpService;

	@Autowired
	private TrustRepository repository;

	/**
	 * Calculates platform reputation for given platformId.
	 * 
	 * @param platformId
	 * @return
	 */
	public Double getPlatformReputation(String platformId) {
		Double fhScore = getFederationHistoryScore(platformId);

		Double score = roundTo2Digits(fhScore);
		logger.debug("Calculated Platform reputation for platform {} with score {}", platformId, score);
		repository.save(new TrustEntry(Type.PLATFORM_REPUTATION, platformId, score));
		return score;
	}

	private Double getFederationHistoryScore(String platformId) {
		List<FederationHistory> fh = amqpService.fetchFederationHistory(platformId);
		Double score = 0.0;

		if (fh != null && !fh.isEmpty()) {
			for (FederationHistory h : fh) {
				score += calcHistoryEntry(h);
			}
			score = score / fh.size();
		}

		return score * 100;
	}

	/**
	 * Calculates the score for one federation history entry taking account the differnt dates.
	 * 
	 * @param h
	 * @return
	 */
	private Double calcHistoryEntry(FederationHistory h) {
		Long fedPeriod = h.getDateFederationRemoved() != null ? h.getDateFederationRemoved().getTime() : new Date().getTime();
		fedPeriod = fedPeriod - h.getDateFederationCreated().getTime();

		Long platPeriod = h.getDatePlatformLeft() != null ? h.getDatePlatformLeft().getTime() : new Date().getTime();
		platPeriod = platPeriod - h.getDatePlatformJoined().getTime();

		return platPeriod.doubleValue() / fedPeriod.doubleValue();

	}

	private Double roundTo2Digits(Double val) {
		DecimalFormat rounded = new DecimalFormat("#.##");
		return Double.valueOf(rounded.format(val));
	}
}
