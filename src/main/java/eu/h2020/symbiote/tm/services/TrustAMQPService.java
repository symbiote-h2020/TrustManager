package eu.h2020.symbiote.tm.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.federation.model.FederationHistoryResponse;

/**
 * @author RuggenthalerC
 * 
 *         Handles the connection to platform RabbitMQ broker for requesting data from other cloud components.
 *
 */
@Service
public class TrustAMQPService {
	private static final Logger logger = LoggerFactory.getLogger(TrustAMQPService.class);

	@Autowired
	private RabbitTemplate template;

	@Autowired
	private Queue federationHistoryQueue;

	/**
	 * Fetch federation history entries for given platform id.
	 * 
	 * Returns FederationHistory list or empty list if failed.
	 * 
	 * @param platformId
	 * @return List<FederationHistory>
	 */
	public List<FederationHistory> fetchFederationHistory(String platformId) {
		logger.debug("Queried fed history for platform {}", platformId);
		FederationHistoryResponse r = (FederationHistoryResponse) template.convertSendAndReceive(federationHistoryQueue.getName(), platformId);

		if (r != null) {
			logger.debug("Received {} history entries for platform {}", r.getEvents().size(), platformId);

			return r.getEvents();
		} else {
			logger.warn("No history response returned for platform {} - response: {}", platformId, r);
			return new ArrayList<>();
		}

	}
}
