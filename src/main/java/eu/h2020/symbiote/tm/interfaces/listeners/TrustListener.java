package eu.h2020.symbiote.tm.interfaces.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.tm.services.TrustService;

/**
 * @author RuggenthalerC
 *
 *         AMQP listener endpoints for trust/reputation requests.
 */
@Service
public class TrustListener {
	private static final Logger logger = LoggerFactory.getLogger(TrustListener.class);

	@Autowired
	private TrustService trustService;

	/**
	 * RMQ listener to consume platform reputation requests for given platformIds.
	 * 
	 * @param platformId
	 * @return Double reputation value
	 */
	@RabbitListener(queues = "${rabbit.queue.trust.get_platform_reputation}")
	public Double getFederationHistoryByPlatformId(String platformId) {
		logger.debug("Received platform ID: {}", platformId);

		return trustService.getPlatformReputation(platformId);
	}
}