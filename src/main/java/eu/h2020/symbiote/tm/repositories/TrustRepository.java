package eu.h2020.symbiote.tm.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import eu.h2020.symbiote.cloud.trust.model.TrustEntry;

/**
 * @author ruggenthalerc
 * 
 *         MongoDB repository interface for trust objects providing CRUD operations.
 */
public interface TrustRepository extends MongoRepository<TrustEntry, String> {
	@Query("{'last_update': {$lte: ?0}}, 'type' : ?1")
	List<TrustEntry> findEntriesUpdatedAfter(Date updatedAfter, TrustEntry.Type type);
}
