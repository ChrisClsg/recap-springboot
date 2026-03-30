package de.clsg.recap_springboot.dto;

import java.util.List;

public record GptRequestDto(
  String model,
  List<GptMessageDto> messages
) {

}
