package eu.h2020.symbiote.tm.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import eu.h2020.symbiote.cloud.sla.model.Violation;

/**
 * @author ruggenthalerc
 * 
 *         MongoDB repository interface for storing SLA violations.
 */
public interface SLAViolationRepository extends MongoRepository<Violation, String> {

	/**
	 * @param receivedAfter
	 *            since Date
	 * @param resId
	 *            resource ID / device ID
	 * @return returns list of Violations
	 */
	@Query("{'date': {$lte: ?0}, 'deviceId' : ?1}")
	List<Violation> findRecentViolationsByResourceId(Date receivedAfter, String resId);
}
