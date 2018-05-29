package eu.h2020.symbiote.tm.cron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.tm.services.TrustAMQPService;

/**
 * @author RuggenthalerC
 * 
 *         Handles the scheduled updates for all trust/reputation values.
 *
 */
@Component
public class TrustUpdater {
	private static final Logger logger = LoggerFactory.getLogger(TrustUpdater.class);

	@Autowired
	private TrustAMQPService amqpService;

	@Autowired
	private TrustRepository repository;

	@Scheduled(cron = "${symbIoTe.trust.resource_trust.period}")
	public void scheduleResourceTrustUpdate() {
		logger.debug("Resource Trust update triggered");
		// TODO: add logic
	}

	@Scheduled(cron = "${symbIoTe.trust.platform_reputation.period}")
	public void schedulePlatformReputationUpdate() {
		logger.debug("Platform Reputation Trust update triggered");
		// TODO: add logic
	}

	@Scheduled(cron = "${symbIoTe.trust.adaptive_resource_trust.period}")
	public void scheduleAdaptiveResourceTrustUpdate() {
		logger.debug("Adaptive Resource Trust update triggered");
		// TODO: add logic
	}

}