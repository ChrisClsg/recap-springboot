package de.clsg.recap_springboot.dto;

import de.clsg.recap_springboot.enums.TodoStatus;

public record TodoDto(
  String description,
  TodoStatus status
) {

}
