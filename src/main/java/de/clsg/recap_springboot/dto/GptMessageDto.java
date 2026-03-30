package de.clsg.recap_springboot.dto;

public record GptMessageDto(
  String content,
  String role
) {

}
