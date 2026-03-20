package de.clsg.recap_springboot.service;

import de.clsg.recap_springboot.dto.TodoDto;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.repository.TodoRepo;

@Service
public class TodoService {
  private final IdService idService;
  private final TodoRepo todoRepo;

  public TodoService(IdService idService, TodoRepo todoRepo) {
    this.idService = idService;
    this.todoRepo = todoRepo;
  }

  public List<Todo> findAll() {
    return todoRepo.findAll();
  }

  public Optional<Todo> findById(String id) {
    return todoRepo.findById(id);
  }

  public Todo addTodo(TodoDto todoData) {
    String id = idService.randomId();
    Todo newTodo = new Todo(id, todoData.description(), todoData.status());

    todoRepo.save(newTodo);
    return newTodo;
  }
}
