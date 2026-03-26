package de.clsg.recap_springboot.dto;

import de.clsg.recap_springboot.enums.TodoStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TodoDto(
  @NotEmpty String description,
  @NotNull TodoStatus status
) {
}
