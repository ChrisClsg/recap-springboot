package de.clsg.recap_springboot.service;

import de.clsg.recap_springboot.dto.TodoDto;
import de.clsg.recap_springboot.enums.TodoEventType;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import de.clsg.recap_springboot.model.HistoryState;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.model.TodoEvent;
import de.clsg.recap_springboot.repository.HistoryStateRepo;
import de.clsg.recap_springboot.repository.TodoEventRepo;
import de.clsg.recap_springboot.repository.TodoRepo;

@Service
public class TodoService {
  private final HistoryStateRepo historyStateRepo;
  private final IdService idService;
  private final TodoEventRepo eventRepo;
  private final TodoRepo todoRepo;

  public TodoService(HistoryStateRepo historyStateRepo, IdService idService, TodoEventRepo eventRepo,
      TodoRepo todoRepo) {
    this.historyStateRepo = historyStateRepo;
    this.idService = idService;
    this.eventRepo = eventRepo;
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
    createEvent(
      TodoEventType.CREATE,
      id,
      null,
      new TodoDto(newTodo.description(), newTodo.status())
    );

    return newTodo;
  }

  public Optional<Todo> updateTodo(String id, TodoDto newData) {
    Optional<Todo> todoOpt = todoRepo.findById(id);
    if (todoOpt.isEmpty()) return todoOpt;

    Todo updatedTodo = todoOpt
      .get()
      .withDescription(newData.description())
      .withStatus(newData.status());

    todoRepo.save(updatedTodo);

    Todo before = todoOpt.get();
    createEvent(
      TodoEventType.UPDATE,
      id,
      new TodoDto(before.description(), before.status()),
      new TodoDto(updatedTodo.description(), updatedTodo.status())
    );

    return Optional.ofNullable(updatedTodo);
  }

  public boolean deleteTodo(String id) {
    Optional<Todo> todoOpt = todoRepo.findById(id);
    if (todoOpt.isEmpty()) return false;

    todoRepo.deleteById(id);

    Todo before = todoOpt.get();
    createEvent(
      TodoEventType.DELETE,
      id,
      new TodoDto(before.description(), before.status()),
      null
    );

    return true;
  }

  private void createEvent(TodoEventType type, String todoId, TodoDto before, TodoDto after) {
    HistoryState state = getOrCreateHistoryState();

    TodoEvent event = TodoEvent.builder()
      .id(idService.randomId())
      .sequence(state.currentSequence() + 1)
      .todoId(todoId)
      .after(after)
      .before(before)
      .type(type)
      .build();

    eventRepo.save(event);
    historyStateRepo.save(state.withCurrentSequence(state.currentSequence() + 1));
  }

  private HistoryState getOrCreateHistoryState() {
    return historyStateRepo.findById("global")
        .orElseGet(() -> historyStateRepo.save(new HistoryState("global", 0)));
  }
}
