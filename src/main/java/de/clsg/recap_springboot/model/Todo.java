package de.clsg.recap_springboot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.With;
@Document("todos")
@With
public record Todo(
  @Id String id,
  String description,
  String status
) {

}
