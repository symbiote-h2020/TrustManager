package eu.h2020.symbiote.tm.interfaces.listeners;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.ResourcesAddedOrUpdatedMessage;
import eu.h2020.symbiote.cloud.model.internal.ResourcesDeletedMessage;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.util.RabbitConstants;

/**
 * @author RuggenthalerC
 *
 *         AMQP listener endpoints for resource updates from SM and PR.
 */
@Service
public class ResourceUpdateListener {
	private static final Logger logger = LoggerFactory.getLogger(ResourceUpdateListener.class);

	@Autowired
	private TrustRepository trustRepository;

	/**
	 * Receives own resource sharing updates from RH.
	 * 
	 * @param sharedResourceMap
	 */
	@RabbitListener(bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY
			+ "}", type = RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY), key = "${" + RabbitConstants.ROUTING_KEY_RH_SHARED_PROPERTY + "}"))
	public void receiveOwnSharedResources(Map<String, List<CloudResource>> sharedResourceMap) {
		// TODO: add storage
	}

	/**
	 * Receives own resource unsharing updates from RH.
	 * 
	 * @param unsharedResourceMap
	 */
	@RabbitListener(bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY
			+ "}", type = RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY), key = "${" + RabbitConstants.ROUTING_KEY_RH_UNSHARED_PROPERTY + "}"))
	public void receiveOwnUnsharedResources(Map<String, List<String>> sharedResourceMap) {
		// TODO: add removal
	}

	/**
	 * Receives foreign resource adding/updating messages from SM and stores the resource trust values.
	 * 
	 * @param sharedResources
	 */
	@RabbitListener(bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY
			+ "}", type = RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY), key = "${rabbit.routingKey.platformRegistry.addOrUpdateFederatedResources}"))
	public void receiveForeignSharedResources(ResourcesAddedOrUpdatedMessage sharedResources) {
		sharedResources.getNewFederatedResources().forEach(res -> {
			TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, res.getPlatformId(), res.getSymbioteId(),
					res.getCloudResource().getFederationInfo().getResourceTrust());
			trustRepository.save(te);
			logger.debug("Updated foreign resource trust value: resource {} with score {} from platform {}", te.getResourceId(), te.getValue(),
					te.getPlatformId());
		});
	}

	/**
	 * Receives foreign resource deletion messages from SM.
	 * 
	 * @param unsharedResources
	 */
	@RabbitListener(bindings = @QueueBinding(value = @Queue, exchange = @Exchange(value = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_TYPE_PROPERTY
			+ "}", type = RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_TYPE_PROPERTY), key = "${rabbit.routingKey.platformRegistry.removeFederatedResources}"))
	public void receiveForeignUnsharedResources(ResourcesDeletedMessage unsharedResources) {
		unsharedResources.getDeletedFederatedResourcesMap().keySet().forEach(resId -> {
			TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, null, resId, null);
			trustRepository.delete(te.getId());
			logger.debug("Removed foreign resource trust value: resource {}", te.getResourceId());
		});
	}
}