package de.clsg.recap_springboot.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.With;

@Document("history_state")
@With
public record HistoryState(
  @Id String id,
  int currentSequence
) {

}
