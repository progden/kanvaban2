package io.progden.kanban.spring.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.progden.kanban.spring.KanbanApplication;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(classes = KanbanApplication.class)
@AutoConfigureMockMvc
@Import(FakeCardLookupPort.Config.class)
public class CucumberSpringConfiguration {
}
