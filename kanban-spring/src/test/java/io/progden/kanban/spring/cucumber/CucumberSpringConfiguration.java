package io.progden.kanban.spring.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.progden.kanban.spring.KanbanApplication;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = KanbanApplication.class)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}
