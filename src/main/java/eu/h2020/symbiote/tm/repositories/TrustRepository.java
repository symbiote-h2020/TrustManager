package eu.h2020.symbiote.tm.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import eu.h2020.symbiote.tm.model.TrustEntry;

/**
 * @author ruggenthalerc
 * 
 *         MongoDB repository interface for trust objects providing CRUD operations.
 */
interface TrustRepository extends MongoRepository<TrustEntry, String> {
}
