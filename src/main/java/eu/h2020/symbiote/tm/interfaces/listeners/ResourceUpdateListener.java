package eu.h2020.symbiote.tm.interfaces.listeners;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.ResourcesAddedOrUpdatedMessage;
import eu.h2020.symbiote.cloud.model.internal.ResourcesDeletedMessage;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.model.mim.Federation;
import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.util.RabbitConstants;

/**
 * @author RuggenthalerC
 *
 *         AMQP listener endpoints for platform and resource updates from SM, PR and FM.
 */
@Service
public class ResourceUpdateListener {
	private static final Logger logger = LoggerFactory.getLogger(ResourceUpdateListener.class);

	@Autowired
	private TrustRepository trustRepository;

	@Value("${platform.id}")
	private String ownPlatformId;

	/**
	 * Receives own resource registration updates from RH.
	 * 
	 * @param registeredResList list of registered resources
	 */
	@RabbitListener(bindings = @QueueBinding(
			value = @Queue,
            exchange = @Exchange(
                    value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    internal = "${" + RabbitConstants.EXCHANGE_RH_INTERNAL_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}",
                    type = "${" + RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_UPDATED_PROPERTY + "}"))
	public void receiveOwnSharedResources(List<CloudResource> registeredResList) {
        logger.debug("receiveOwnSharedResources = " + registeredResList);
        if (registeredResList != null) {
			registeredResList.forEach(cr -> {
				if (cr != null) {
					TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, ownPlatformId, cr.getInternalId());
					// add new entry if not exists
					if (!trustRepository.exists(te.getId())) {
						// Store empty own resource trust object -> cron will update resource trust
						trustRepository.save(te);
						logger.debug("Added own resource: internalId {} from platform {}", te.getResourceId(), te.getPlatformId());
					}
				}
			});
		}
	}

	/**
	 * Receives own resource deletion updates from RH.
	 * 
	 * @param deletedResList list of deleted resources
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue,
            exchange = @Exchange(
                    value = "${" + RabbitConstants.EXCHANGE_RH_NAME_PROPERTY + "}",
                    durable = "${" + RabbitConstants.EXCHANGE_RH_DURABLE_PROPERTY + "}",
                    internal = "${" + RabbitConstants.EXCHANGE_RH_INTERNAL_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_RH_AUTODELETE_PROPERTY + "}",
                    type = "${" + RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY + "}"),
            key = "${" + RabbitConstants.ROUTING_KEY_RH_DELETED_PROPERTY + "}"))
	public void receiveOwnUnsharedResources(List<String> deletedResList) {
        logger.debug("receiveOwnUnsharedResources = " + deletedResList);
        if (deletedResList != null) {
			deletedResList.forEach(resId -> {
				TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, ownPlatformId, resId);
				trustRepository.delete(te.getId());
				logger.debug("Deleted own resource: internalId {} from platform {}", te.getResourceId(), te.getPlatformId());
			});
		}
	}

	/**
	 * Receives foreign resource adding/updating messages from SM and stores the resource trust values.
	 * 
	 * @param sharedResources shared resource message
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue,
            exchange = @Exchange(
                    value = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_NAME_PROPERTY + "}",
                    durable = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_DURABLE_PROPERTY + "}",
                    internal = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_INTERNAL_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_AUTODELETE_PROPERTY + "}",
                    type = "${" + RabbitConstants.EXCHANGE_RH_TYPE_PROPERTY + "}"),
            key = "${rabbit.routingKey.platformRegistry.addOrUpdateFederatedResources}"))
	public void receiveForeignSharedResources(ResourcesAddedOrUpdatedMessage sharedResources) {
	    logger.debug("receiveForeignSharedResources = " + sharedResources);
		if (sharedResources != null && sharedResources.getNewFederatedResources() != null) {
			sharedResources.getNewFederatedResources().forEach(res -> {
				if (res != null && res.getCloudResource() != null && res.getCloudResource().getFederationInfo() != null
						&& res.getCloudResource().getFederationInfo().getSharingInformation() != null) {
					res.getCloudResource().getFederationInfo().getSharingInformation().values().forEach(info -> {
						TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, res.getPlatformId(), info.getSymbioteId());
						te.updateEntry(sanitizeValue(res.getCloudResource().getFederationInfo().getResourceTrust()));
						// Store shared foreign resource trust object
						trustRepository.save(te);
						logger.debug("Updated foreign resource trust value: resource {} with score {} from platform {}", te.getResourceId(), te.getValue(),
								te.getPlatformId());
					});
				}
			});
		}
	}

	private Double sanitizeValue(Double val) {
		if (val != null && !val.isNaN()) {
			return Math.min(Math.max(val, 0), 100);
		}
		return null;
	}

	/**
	 * Receives foreign resource deletion messages from SM.
	 * 
	 * @param unsharedResources unshared resource message
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue,
            exchange = @Exchange(
                    value = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_NAME_PROPERTY + "}",
                    durable = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_DURABLE_PROPERTY + "}",
                    internal = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_INTERNAL_PROPERTY + "}",
                    autoDelete = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_AUTODELETE_PROPERTY + "}",
                    type = "${" + RabbitConstants.EXCHANGE_PLATFORM_REGISTRY_TYPE_PROPERTY + "}"),
            key = "${rabbit.routingKey.platformRegistry.removeFederatedResources}"))
	public void receiveForeignUnsharedResources(ResourcesDeletedMessage unsharedResources) {
	    logger.debug("receiveForeignUnsharedResources = " + unsharedResources);
		if (unsharedResources != null && unsharedResources.getDeletedFederatedResources() != null) {
			unsharedResources.getDeletedFederatedResources().forEach(resId -> {
				TrustEntry te = new TrustEntry(Type.RESOURCE_TRUST, null, resId);
				trustRepository.delete(te.getId());
				logger.debug("Removed foreign resource trust value: resource {}", te.getResourceId());
			});
		}
	}

	/**
	 * Receives created federation requests.
	 * 
	 * @param fed federation object
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue,
            exchange = @Exchange(
                    value = "${rabbit.exchange.federation}",
                    durable = "${rabbit.exchange.federation.durable}",
                    internal = "${rabbit.exchange.federation.internal}",
                    autoDelete = "${rabbit.exchange.federation.autodelete}",
                    type = "${rabbit.exchange.federation.type}"),
            key = "${rabbit.routingKey.federation.created}"))
	public void receiveFederationCreated(Federation fed) {
        logger.debug("receiveFederationCreated = " + fed);
        updatePlatformEntries(fed);
	}

	/**
	 * Receives updated federation requests.
	 * 
	 * @param fed federation object
	 */
	@RabbitListener(bindings = @QueueBinding(
	        value = @Queue,
            exchange = @Exchange(
                    value = "${rabbit.exchange.federation}",
                    durable = "${rabbit.exchange.federation.durable}",
                    internal = "${rabbit.exchange.federation.internal}",
                    autoDelete = "${rabbit.exchange.federation.autodelete}",
                    type = "${rabbit.exchange.federation.type}"),
            key = "${rabbit.routingKey.federation.changed}"))
	public void receiveFederationUpdated(Federation fed) {
        logger.debug("receiveFederationUpdated = " + fed);
        updatePlatformEntries(fed);
	}

	private void updatePlatformEntries(Federation fed) {
		if (fed != null && fed.getMembers() != null) {
			fed.getMembers().forEach(fedMem -> {
				TrustEntry te = new TrustEntry(Type.PLATFORM_REPUTATION, fedMem.getPlatformId(), null);
				// add new entry if not exists
				if (!trustRepository.exists(te.getId())) {
					// Store empty reputation trust object -> cron will update resource trust
					trustRepository.save(te);
					logger.debug("Added federated platform {} from federation {}", te.getPlatformId(), fed.getId());
				}
			});
		}
	}
}