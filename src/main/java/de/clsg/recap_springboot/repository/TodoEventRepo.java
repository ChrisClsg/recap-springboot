package de.clsg.recap_springboot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import de.clsg.recap_springboot.model.TodoEvent;

@Repository
public interface TodoEventRepo extends MongoRepository<TodoEvent, String> {

}
