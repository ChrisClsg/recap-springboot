package de.clsg.recap_springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.clsg.recap_springboot.dto.TodoDto;
import de.clsg.recap_springboot.enums.TodoEventType;
import de.clsg.recap_springboot.enums.TodoStatus;
import de.clsg.recap_springboot.model.HistoryState;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.model.TodoEvent;
import de.clsg.recap_springboot.repository.HistoryStateRepo;
import de.clsg.recap_springboot.repository.TodoEventRepo;
import de.clsg.recap_springboot.repository.TodoRepo;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
  @Mock HistoryStateRepo historyStateRepo;
  @Mock IdService idService;
  @Mock TodoEventRepo eventRepo;
  @Mock TodoRepo todoRepo;
  @InjectMocks private TodoService todoService;

  private Todo validTodo() {
    return new Todo(
      "1", "Some description", TodoStatus.OPEN
    );
  }

  private TodoDto validDto() {
    return new TodoDto("Some Dto description", TodoStatus.DONE);
  }

  @Test
  void findAll_returnsRepoFindAll_whenCalled() {
    Todo todo = validTodo();
    when(todoRepo.findAll()).thenReturn(List.of(todo));

    assertEquals(List.of(todo), todoService.findAll());
    verify(todoRepo).findAll();
    verifyNoMoreInteractions(todoRepo);
  }

  @Test
  void findById_returnsRepoFindById_whenCalled() {
    Todo todo = validTodo();
    when(todoRepo.findById(todo.id())).thenReturn(Optional.ofNullable(todo));

    assertEquals(Optional.ofNullable(todo), todoService.findById(todo.id()));
    verify(todoRepo).findById(todo.id());
    verifyNoMoreInteractions(todoRepo);
  }

  @Test
  void addTodo_returnsSavedTodo_whenCalledWithValidDto() {
    TodoDto dto = validDto();
    String expectedId = "some-id";
    when(idService.randomId()).thenReturn(expectedId);
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 1)));

    Todo expectedTodo = Todo.builder()
      .description(dto.description())
      .status(dto.status())
      .id(expectedId)
      .build();

    assertEquals(expectedTodo, todoService.addTodo(dto));
    verify(idService, times(2)).randomId();
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(idService, todoRepo);

    verify(eventRepo).save(new TodoEvent(
      "some-id",
      2,
      expectedTodo.id(),
      new TodoDto(expectedTodo.description(), expectedTodo.status()),
      null,
      TodoEventType.CREATE
    ));
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", 2));
    verifyNoMoreInteractions(historyStateRepo);
  }

  @Test
  void updatedTodo_returnsEmptyOptional_whenGivenTodoIdNotExists() {
    TodoDto dto = validDto();
    when(todoRepo.findById("does not exist")).thenReturn(Optional.empty());

    assertEquals(Optional.empty(), todoService.updateTodo("does not exist", dto));
    verify(todoRepo).findById("does not exist");
    verifyNoMoreInteractions(todoRepo);

    verifyNoInteractions(eventRepo);
    verifyNoInteractions(historyStateRepo);
  }

  @Test
  void updatedTodo_returnsOptionalWithUpdatedTodo_whenGivenValidTodoAndDto() {
    Todo todo = validTodo();
    TodoDto dto = validDto();
    Todo expectedTodo = todo
      .withDescription(dto.description())
      .withStatus(dto.status());
    when(todoRepo.findById(todo.id())).thenReturn(Optional.ofNullable(todo));
    when(idService.randomId()).thenReturn("event-123");
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 1)));

    assertEquals(Optional.ofNullable(expectedTodo), todoService.updateTodo(todo.id(), dto));
    verify(todoRepo).findById(todo.id());
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(todoRepo);

    verify(eventRepo).save(new TodoEvent(
      "event-123",
      2,
      todo.id(),
      new TodoDto(expectedTodo.description(), expectedTodo.status()),
      new TodoDto(todo.description(), todo.status()),
      TodoEventType.UPDATE
    ));
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", 2));
    verifyNoMoreInteractions(historyStateRepo);
  }

  @Test
  void deleteTodo_returnsFalse_whenGivenNonExistentId() {
    when(todoRepo.findById("does not exist")).thenReturn(Optional.empty());

    assertFalse(todoService.deleteTodo("does not exist"));
    verify(todoRepo).findById("does not exist");
    verifyNoMoreInteractions(todoRepo);

    verifyNoInteractions(eventRepo);
    verifyNoInteractions(historyStateRepo);
  }

  @Test
  void deleteTodo_deletesTodoAndReturnsTrue_whenGivenExistingId() {
    Todo todo = validTodo();
    when(todoRepo.findById(todo.id())).thenReturn(Optional.of(todo));
    when(idService.randomId()).thenReturn("event-123");
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 1)));

    assertTrue(todoService.deleteTodo(todo.id()));

    verify(todoRepo).findById(todo.id());
    verify(todoRepo).deleteById(todo.id());
    verifyNoMoreInteractions(todoRepo);

    verify(eventRepo).save(new TodoEvent(
      "event-123",
      2,
      todo.id(),
      null,
      new TodoDto(todo.description(), todo.status()),
      TodoEventType.DELETE
    ));
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", 2));
    verifyNoMoreInteractions(historyStateRepo);
  }
}
