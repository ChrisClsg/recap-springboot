package de.clsg.recap_springboot.service;

import java.util.List;

import org.springframework.stereotype.Service;

import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.repository.TodoRepo;

@Service
public class TodoService {
  private final TodoRepo todoRepo;

  public TodoService(TodoRepo todoRepo) {
    this.todoRepo = todoRepo;
  }

  public List<Todo> findAll() {
    return todoRepo.findAll();
  }
}
