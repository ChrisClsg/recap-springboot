package de.clsg.recap_springboot.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import de.clsg.recap_springboot.dto.TodoDto;
import de.clsg.recap_springboot.enums.TodoStatus;
import de.clsg.recap_springboot.model.Todo;
import de.clsg.recap_springboot.repository.TodoRepo;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class TodoControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TodoRepo todoRepo;

  private Todo validTodo() {
    return new Todo(
      "1", "Some description", TodoStatus.OPEN
    );
  }

  private TodoDto validDto() {
    return new TodoDto("Some Dto description", TodoStatus.DONE);
  }

  @Test
  void findAll_returnsOkAndEmptyList_whenNoTodoInDb () throws Exception {
    mockMvc.perform(get("/api/todo"))
      .andExpect(status().isOk())
      .andExpect(content().json("[]"));
  }

  @Test
  void findAll_returnsOkAndListOfTodos_whenTodosPresentInDb () throws Exception {
    List<Todo> expectedTodos = List.of(validTodo(), validTodo().withId("2"));
    expectedTodos.forEach(todo -> todoRepo.save(todo));

    mockMvc.perform(get("/api/todo"))
      .andExpect(status().isOk())
      .andExpect(content().json(objectMapper.writeValueAsString(expectedTodos)));
  }

  @Test
  void findById_returnsOkAndTodo_whenQueriedTodoExists () throws Exception {
    Todo todo = validTodo();
    todoRepo.save(todo);

    mockMvc.perform(get("/api/todo/" + todo.id()))
      .andExpect(status().isOk())
      .andExpect(content().json(objectMapper.writeValueAsString(todo)));
  }

  @Test
  void findById_returnsNotFound_whenQueriedTodoNotExists () throws Exception {
    Todo todo = validTodo();

    mockMvc.perform(get("/api/todo/" + todo.id()))
      .andExpect(status().isNotFound());
  }

  @Test
  void addTodo_returnsCreatedAndNewTodo_whenCalledWithValidDto () throws Exception {
    TodoDto dto = validDto();

    mockMvc.perform(
      post("/api/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isCreated())
      .andExpect(content().json(objectMapper.writeValueAsString(dto)))
      .andExpect(jsonPath("$.id").isNotEmpty());
  }
}
