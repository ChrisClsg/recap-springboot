package de.clsg.recap_springboot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import de.clsg.recap_springboot.enums.TodoStatus;
import lombok.With;

@Document("todos")
@With
public record Todo(
  @Id String id,
  String description,
  TodoStatus status
) {

}
