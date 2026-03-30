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
  @Mock SpellingService spellingService;
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
    int currentSequence = 1;
    int expectedSequence = 2;
    when(idService.randomId()).thenReturn(expectedId);
    when(spellingService.checkSpelling(dto.description())).thenReturn(dto.description());
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));

    Todo expectedTodo = Todo.builder()
      .description(dto.description())
      .status(dto.status())
      .id(expectedId)
      .build();

    assertEquals(expectedTodo, todoService.addTodo(dto));
    verify(idService, times(2)).randomId();
    verify(spellingService).checkSpelling(dto.description());
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(idService, todoRepo, spellingService);

    verify(eventRepo).save(new TodoEvent(
      "some-id",
      expectedSequence,
      expectedTodo.id(),
      new TodoDto(expectedTodo.description(), expectedTodo.status()),
      null,
      TodoEventType.CREATE
    ));
    verify(eventRepo).deleteBySequenceGreaterThan(currentSequence);
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", expectedSequence));
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
    int currentSequence = 1;
    int expectedSequence = 2;
    when(todoRepo.findById(todo.id())).thenReturn(Optional.ofNullable(todo));
    when(idService.randomId()).thenReturn("event-123");
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 1)));

    assertEquals(Optional.ofNullable(expectedTodo), todoService.updateTodo(todo.id(), dto));
    verify(todoRepo).findById(todo.id());
    verify(todoRepo).save(expectedTodo);
    verifyNoMoreInteractions(todoRepo);

    verify(eventRepo).save(new TodoEvent(
      "event-123",
      expectedSequence,
      todo.id(),
      new TodoDto(expectedTodo.description(), expectedTodo.status()),
      new TodoDto(todo.description(), todo.status()),
      TodoEventType.UPDATE
    ));
    verify(eventRepo).deleteBySequenceGreaterThan(currentSequence);
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", expectedSequence));
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
    int currentSequence = 1;
    int expectedSequence = 2;
    when(todoRepo.findById(todo.id())).thenReturn(Optional.of(todo));
    when(idService.randomId()).thenReturn("event-123");
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));

    assertTrue(todoService.deleteTodo(todo.id()));

    verify(todoRepo).findById(todo.id());
    verify(todoRepo).deleteById(todo.id());
    verifyNoMoreInteractions(todoRepo);

    verify(eventRepo).save(new TodoEvent(
      "event-123",
      expectedSequence,
      todo.id(),
      null,
      new TodoDto(todo.description(), todo.status()),
      TodoEventType.DELETE
    ));
    verify(eventRepo).deleteBySequenceGreaterThan(currentSequence);
    verifyNoMoreInteractions(eventRepo);

    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", expectedSequence));
    verifyNoMoreInteractions(historyStateRepo);
  }

  @Test
  void undoEvent_returnsEmpty_whenCurrentSequenceIsZero() {
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 0)));

    assertEquals(Optional.empty(), todoService.undoEvent());
    verify(historyStateRepo).findById("global");
    verifyNoMoreInteractions(historyStateRepo);
    verifyNoInteractions(eventRepo, todoRepo);
  }

  @Test
  void undoEvent_returnsEmpty_whenEventNotFound() {
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", 1)));
    when(eventRepo.findBySequence(1)).thenReturn(Optional.empty());

    assertEquals(Optional.empty(), todoService.undoEvent());
    verify(historyStateRepo).findById("global");
    verify(eventRepo).findBySequence(1);
    verifyNoMoreInteractions(historyStateRepo, eventRepo);
    verifyNoInteractions(todoRepo);
  }

  @Test
  void undoEvent_deletesTodo_whenUndoingCreateEvent() {
    String todoId = "1";
    Todo todo = validTodo();
    int currentSequence = 1;
    TodoEvent createEvent = new TodoEvent(
      "event-1",
      currentSequence,
      todoId,
      new TodoDto(todo.description(), todo.status()),
      null,
      TodoEventType.CREATE
    );
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence)).thenReturn(Optional.of(createEvent));
    when(todoRepo.findById(todo.id())).thenReturn(Optional.of(todo));

    assertEquals(Optional.empty(), todoService.undoEvent());
    verify(todoRepo).findById(todoId);
    verify(todoRepo).deleteById(todoId);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence - 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }

  @Test
  void undoEvent_restoresTodo_whenUndoingDeleteEvent() {
    String todoId = "1";
    Todo originalTodo = validTodo();
    TodoDto todoDto = new TodoDto(originalTodo.description(), originalTodo.status());
    int currentSequence = 2;
    TodoEvent deleteEvent = new TodoEvent(
      "event-2",
      currentSequence,
      todoId,
      null,
      todoDto,
      TodoEventType.DELETE
    );
    Todo restoredTodo = new Todo(todoId, todoDto.description(), todoDto.status());

    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence)).thenReturn(Optional.of(deleteEvent));
    when(todoRepo.save(restoredTodo)).thenReturn(restoredTodo);

    assertEquals(Optional.of(restoredTodo), todoService.undoEvent());
    verify(todoRepo).save(restoredTodo);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence - 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }

  @Test
  void undoEvent_revertsToOldState_whenUndoingUpdateEvent() {
    String todoId = "1";
    Todo afterTodo = new Todo(todoId, "New description", TodoStatus.DONE);
    Todo beforeTodo = new Todo(todoId, "Old description", TodoStatus.OPEN);
    TodoDto afterDto = new TodoDto(afterTodo.description(), afterTodo.status());
    TodoDto beforeDto = new TodoDto(beforeTodo.description(), beforeTodo.status());
    int currentSequence = 3;

    TodoEvent updateEvent = new TodoEvent(
      "event-3",
      currentSequence,
      todoId,
      afterDto,
      beforeDto,
      TodoEventType.UPDATE
    );

    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence)).thenReturn(Optional.of(updateEvent));
    when(todoRepo.findById(todoId)).thenReturn(Optional.of(afterTodo));
    when(todoRepo.save(beforeTodo)).thenReturn(beforeTodo);

    assertEquals(Optional.of(beforeTodo), todoService.undoEvent());
    verify(todoRepo).findById(todoId);
    verify(todoRepo).save(beforeTodo);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence - 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }

  @Test
  void redoEvent_returnsEmpty_whenNoMoreEventsToRedo() {
    int currentSequence = 1;
    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence + 1)).thenReturn(Optional.empty());

    assertEquals(Optional.empty(), todoService.redoEvent());
    verify(historyStateRepo).findById("global");
    verify(eventRepo).findBySequence(currentSequence + 1);
    verifyNoMoreInteractions(historyStateRepo, eventRepo);
    verifyNoInteractions(todoRepo);
  }

  @Test
  void redoEvent_restoresTodo_whenRedoingCreateEvent() {
    Todo todo = validTodo();
    TodoDto todoDto = new TodoDto(todo.description(), todo.status());
    int currentSequence = 0;

    TodoEvent createEvent = new TodoEvent(
      "event-1",
      currentSequence + 1,
      todo.id(),
      todoDto,
      null,
      TodoEventType.CREATE
    );

    Todo restoredTodo = new Todo(todo.id(), todoDto.description(), todoDto.status());

    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence + 1)).thenReturn(Optional.of(createEvent));
    when(todoRepo.save(restoredTodo)).thenReturn(restoredTodo);

    assertEquals(Optional.of(restoredTodo), todoService.redoEvent());
    verify(todoRepo).save(restoredTodo);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence + 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }

  @Test
  void redoEvent_deletesTodo_whenRedoingDeleteEvent() {
    String todoId = "1";
    Todo todo = validTodo();
    TodoDto todoDto = new TodoDto(todo.description(), todo.status());
    int currentSequence = 1;

    TodoEvent deleteEvent = new TodoEvent(
      "event-2",
      currentSequence + 1,
      todoId,
      null,
      todoDto,
      TodoEventType.DELETE
    );

    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence + 1)).thenReturn(Optional.of(deleteEvent));
    when(todoRepo.findById(todo.id())).thenReturn(Optional.of(todo));

    assertEquals(Optional.empty(), todoService.redoEvent());
    verify(todoRepo).findById(todoId);
    verify(todoRepo).deleteById(todoId);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence + 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }

  @Test
  void redoEvent_appliesNewState_whenRedoingUpdateEvent() {
    String todoId = "1";
    Todo afterTodo = new Todo(todoId, "New description", TodoStatus.DONE);
    Todo beforeTodo = new Todo(todoId, "Old description", TodoStatus.OPEN);
    TodoDto afterDto = new TodoDto(afterTodo.description(), afterTodo.status());
    TodoDto beforeDto = new TodoDto(beforeTodo.description(), beforeTodo.status());
    int currentSequence = 2;

    TodoEvent updateEvent = new TodoEvent(
      "event-3",
      currentSequence + 1,
      todoId,
      afterDto,
      beforeDto,
      TodoEventType.UPDATE
    );

    when(historyStateRepo.findById("global")).thenReturn(Optional.of(new HistoryState("global", currentSequence)));
    when(eventRepo.findBySequence(currentSequence + 1)).thenReturn(Optional.of(updateEvent));
    when(todoRepo.findById(todoId)).thenReturn(Optional.of(beforeTodo));
    when(todoRepo.save(afterTodo)).thenReturn(afterTodo);

    assertEquals(Optional.of(afterTodo), todoService.redoEvent());
    verify(todoRepo).findById(todoId);
    verify(todoRepo).save(afterTodo);
    verify(historyStateRepo).findById("global");
    verify(historyStateRepo).save(new HistoryState("global", currentSequence + 1));
    verifyNoMoreInteractions(todoRepo, historyStateRepo);
  }
}
