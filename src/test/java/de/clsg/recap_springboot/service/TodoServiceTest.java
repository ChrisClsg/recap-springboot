package de.clsg.recap_springboot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.clsg.recap_springboot.enums.TodoStatus;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.repository.TodoRepo;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {
  @Mock TodoRepo todoRepo;
  @InjectMocks private TodoService todoService;

  private Todo validTodo() {
    return new Todo(
      "1", "Some description", TodoStatus.OPEN
    );
  }

  @Test
  void findAll_returnsRepoFindAll_whenCalled() {
    Todo todo = validTodo();
    when(todoRepo.findAll()).thenReturn(List.of(todo));

    assertEquals(List.of(todo), todoService.findAll());
    verify(todoRepo).findAll();
    verifyNoMoreInteractions(todoRepo);
  }
}
