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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
}
