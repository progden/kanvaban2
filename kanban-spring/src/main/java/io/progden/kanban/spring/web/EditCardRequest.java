package io.progden.kanban.spring.web;

import java.time.LocalDate;
import java.util.List;

record EditCardRequest(String description, LocalDate dueDate, List<String> labels) {
}
