package de.clsg.recap_springboot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
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
import de.clsg.recap_springboot.enums.TodoStatus;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.repository.TodoRepo;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
  @Mock IdService idService;
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
    Todo expectedTodo = Todo.builder()
      .description(dto.description())
      .status(dto.status())
      .id(expectedId)
      .build();

    assertEquals(expectedTodo, todoService.addTodo(dto));
    verify(idService).randomId();
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(idService, todoRepo);
  }

  @Test
  void updatedTodo_returnsEmptyOptional_whenGivenTodoIdNotExists() {
    TodoDto dto = validDto();
    when(todoRepo.findById("does not exist")).thenReturn(Optional.empty());

    assertEquals(Optional.empty(), todoService.updateTodo("does not exist", dto));
    verify(todoRepo).findById("does not exist");
    verifyNoMoreInteractions(todoRepo);
  }

  @Test
  void updatedTodo_returnsOptionalWithUpdatedTodo_whenGivenValidTodoAndDto() {
    Todo todo = validTodo();
    TodoDto dto = validDto();
    Todo expectedTodo = todo
      .withDescription(dto.description())
      .withStatus(dto.status());
    when(todoRepo.findById(todo.id())).thenReturn(Optional.ofNullable(todo));

    assertEquals(Optional.ofNullable(expectedTodo), todoService.updateTodo(todo.id(), dto));
    verify(todoRepo).findById(todo.id());
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(todoRepo);
  }

  @Test
  void deleteTodo_returnsFalse_whenGivenNonExistentId() {
    when(todoRepo.findById("does not exist")).thenReturn(Optional.empty());

    assertFalse(todoService.deleteTodo("does not exist"));
    verify(todoRepo).findById("does not exist");
    verifyNoMoreInteractions(todoRepo);
  }

  @Test
  void deleteTodo_deletesTodoAndReturnsTrue_whenGivenExistingId() {
    Todo todo = validTodo();
    when(todoRepo.findById(todo.id())).thenReturn(Optional.ofNullable(todo));

    assertTrue(todoService.deleteTodo(todo.id()));
    verify(todoRepo).findById(todo.id());
    verify(todoRepo).deleteById(todo.id());
    verifyNoMoreInteractions(todoRepo);
  }
}
