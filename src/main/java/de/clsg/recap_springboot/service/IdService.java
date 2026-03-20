package de.clsg.recap_springboot.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class IdService {
  public String randomId() {
    return UUID.randomUUID().toString();
  }
}
