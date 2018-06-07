package eu.h2020.symbiote.tm.cron;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.tm.services.TrustAMQPService;
import eu.h2020.symbiote.tm.services.TrustCalculationService;

/**
 * @author RuggenthalerC
 * 
 *         Handles the scheduled updates for all trust/reputation values.
 *
 */
@Component
public class TrustReputationUpdateTasks {
	private static final Logger logger = LoggerFactory.getLogger(TrustReputationUpdateTasks.class);

	@Value("${symbIoTe.trust.update.interval}")
	private Integer interval;

	@Autowired
	private TrustAMQPService amqpService;

	@Autowired
	private TrustCalculationService trustService;

	@Autowired
	private TrustRepository trustRepository;

	@Scheduled(cron = "${symbIoTe.trust.resource_trust.period}")
	public void scheduleResourceTrustUpdate() {
		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfter(getUpdateInterval(), Type.RESOURCE_TRUST);
		logger.debug("Resource Trust update triggered for {} entries", entries.size());

		entries.forEach(entry -> {
			Double curVal = entry.getValue();

			entry.updateEntry(trustService.calcResourceTrust(entry.getResourceId()));
			trustRepository.save(entry);

			if (shouldPublishUpdate(curVal, entry.getValue())) {
				amqpService.publishResourceTrustUpdate(entry);
				logger.debug("Resource Trust for resource {} updated: {} to {}", entry.getResourceId(), curVal, entry.getValue());
			}
		});
	}

	@Scheduled(cron = "${symbIoTe.trust.platform_reputation.period}")
	public void schedulePlatformReputationUpdate() {
		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfter(getUpdateInterval(), Type.PLATFORM_REPUTATION);
		logger.debug("Platform Reputation update triggered for {} entries", entries.size());

		entries.forEach(entry -> {
			Double curVal = entry.getValue();

			entry.updateEntry(trustService.calcPlatformReputation(entry.getPlatformId()));
			trustRepository.save(entry);

			if (shouldPublishUpdate(curVal, entry.getValue())) {
				amqpService.publishPlatformReputationUpdate(entry);
				logger.debug("Platform Reputation for platform {} updated: {} to {}", entry.getPlatformId(), curVal, entry.getValue());
			}
		});
	}

	@Scheduled(cron = "${symbIoTe.trust.adaptive_resource_trust.period}")
	public void scheduleAdaptiveResourceTrustUpdate() {
		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfter(getUpdateInterval(), Type.ADAPTIVE_RESOURCE_TRUST);
		logger.debug("Adaptive Resource Trust update triggered for {} entries", entries.size());

		entries.forEach(entry -> {
			Double curVal = entry.getValue();

			entry.updateEntry(trustService.calcAdaptiveResourceTrust(curVal, entry.getResourceId(), entry.getPlatformId()));
			trustRepository.save(entry);

			if (shouldPublishUpdate(curVal, entry.getValue())) {
				amqpService.publishAdaptiveResourceTrustUpdate(entry);
				logger.debug("Adaptive Resource Trust for resource {} updated: {} to {}", entry.getResourceId(), curVal, entry.getValue());
			}
		});
	}

	private Date getUpdateInterval() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -interval);
		return cal.getTime();
	}

	private boolean shouldPublishUpdate(Double curVal, Double newVal) {
		if (curVal != null) {
			return !curVal.equals(newVal);
		}

		return newVal != null;
	}
}