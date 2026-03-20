package de.clsg.recap_springboot.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import de.clsg.recap_springboot.dto.TodoDto;
import de.clsg.recap_springboot.enums.TodoEventType;

@Document("todo_events")
public record TodoEvent(
  @Id String id,
  int sequence,
  String todoId,
  TodoDto after,
  TodoDto before,
  TodoEventType type
) {

}
