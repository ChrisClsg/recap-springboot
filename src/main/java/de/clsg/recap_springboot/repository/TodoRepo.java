package de.clsg.recap_springboot.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import de.clsg.recap_springboot.model.Todo;

@Repository
public interface TodoRepo extends MongoRepository<Todo, String> {

}
