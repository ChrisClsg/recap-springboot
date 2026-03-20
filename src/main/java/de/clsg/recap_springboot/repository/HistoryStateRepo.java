package de.clsg.recap_springboot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import de.clsg.recap_springboot.model.HistoryState;

@Repository
public interface HistoryStateRepo extends MongoRepository<HistoryState, String> {

}
