package de.clsg.recap_springboot.dto;

import java.util.List;

public record GptResponseDto(
  List<GptChoiceDto> choices
) {

}
