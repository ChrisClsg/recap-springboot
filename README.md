# Recap Spring Boot

A Spring Boot demo app with Todo management, undo/redo event history and spelling correction via OpenAI.

## Overview

- Backend: Spring Boot 4, Java 25
- Data layer: MongoDB
- REST API: `api/todo`
- Undo/Redo functionality using an event history
- OpenAI integration (automatic spelling correction of user input in `SpellingService`)

## Key modules

- `controller/TodoController`: CRUD + `undo`/`redo` API endpoints
- `service/TodoService`: business logic, event store, history state
- `service/SpellingService`: builds prompt + calls OpenAI for spelling correction
- `model`, `dto`, `repository`: domain objects, transfer objects, Mongo repositories
- `exceptions/GlobalExceptionHandler`: application exception mapping

## Endpoints

- GET `/api/todo` - list all todos
- GET `/api/todo/{id}` - get by id
- POST `/api/todo` - create todo (`TodoDto` with `description`, `status`)
- PUT `/api/todo/{id}` - update todo (also spell-checks description)
- DELETE `/api/todo/{id}` - delete todo
- POST `/api/todo/undo` - undo last change
- POST `/api/todo/redo` - redo last undone change

## Requirements

- Java 25
- Maven
- MongoDB
- OpenAI key

## Run locally

```bash
./mvnw clean package
./mvnw spring-boot:run
```

## Tests

```bash
./mvnw test
```
