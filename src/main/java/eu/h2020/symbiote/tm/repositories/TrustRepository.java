package eu.h2020.symbiote.tm.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import eu.h2020.symbiote.tm.model.TrustEntry;

/**
 * @author ruggenthalerc
 * 
 *         MongoDB repository interface for trust objects providing CRUD operations.
 */
public interface TrustRepository extends MongoRepository<TrustEntry, String> {

	@Query("{'platform_id' : ?0, 'type' : ?1}")
	public TrustEntry findEntryByPlatformIdAndType(String platformId, TrustEntry.Type type);
}
