package eu.h2020.symbiote.tm.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.federation.model.FederationHistory;
import eu.h2020.symbiote.cloud.federation.model.FederationHistoryResponse;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.util.RabbitConstants;

/**
 * @author RuggenthalerC
 * 
 *         Handles the connection to platform RabbitMQ broker for requesting data from other cloud components.
 *
 */
@Service
public class TrustAMQPService {
	private static final Logger logger = LoggerFactory.getLogger(TrustAMQPService.class);

	@Value("${" + RabbitConstants.ROUTING_KEY_TRUST_RESOURCE_UPDATED + "}")
	private String routingKeyResTrustUpdated;

	@Value("${" + RabbitConstants.ROUTING_KEY_TRUST_PLATFORM_UPDATED + "}")
	private String routingKeyPlatfRepUpdated;

	@Value("${" + RabbitConstants.ROUTING_KEY_TRUST_ADAPTIVE_RESOURCE_UPDATED + "}")
	private String routingKeyAdaptiveResTrustUpdated;

	@Autowired
	private RabbitTemplate template;

	@Autowired
	private Queue federationHistoryQueue;

	@Autowired
	private TopicExchange trustTopic;

	/**
	 * Publish updated Resource Trust entry to topic.
	 * 
	 * @param trustObj
	 */
	public void publishResourceTrustUpdate(TrustEntry trustObj) {
		send(routingKeyResTrustUpdated, trustObj);
	}

	/**
	 * Publish updated Resource Trust entry to topic.
	 * 
	 * @param trustObj
	 */
	public void publishPlatformReputationUpdate(TrustEntry trustObj) {
		send(routingKeyPlatfRepUpdated, trustObj);
	}

	/**
	 * Publish updated Resource Trust entry to topic.
	 * 
	 * @param trustObj
	 */
	public void publishAdaptiveResourceTrustUpdate(TrustEntry trustObj) {
		send(routingKeyAdaptiveResTrustUpdated, trustObj);
	}

	/**
	 * Sends the given message to topic with given routing key.
	 * 
	 * @param routingKey
	 * @param trustObj
	 */
	private void send(String routingKey, TrustEntry trustObj) {
		logger.debug("Message published with routingkey: {} and msg: {}", routingKey, trustObj);
		template.convertAndSend(trustTopic.getName(), routingKey, trustObj);
	}

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
