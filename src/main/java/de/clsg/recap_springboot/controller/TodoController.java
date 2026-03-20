package de.clsg.recap_springboot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.clsg.recap_springboot.dto.TodoDto;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.service.TodoService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("api/todo")
public class TodoController {
  private final TodoService todoService;

  public TodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  @GetMapping()
  public ResponseEntity<List<Todo>> findAll() {
    return ResponseEntity.ok(todoService.findAll());
  }

  @PostMapping()
  public ResponseEntity<Todo> addTodo(@RequestBody TodoDto todoData) {
    Todo newTodo = todoService.addTodo(todoData);
    return ResponseEntity.status(HttpStatus.CREATED).body(newTodo);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Todo> findById(@PathVariable String id) {
    return todoService.findById(id)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<Todo> updateTodo(@PathVariable String id, @RequestBody TodoDto newData) {
    return todoService.updateTodo(id, newData)
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
