package me.mamre.controller;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import me.mamre.model.Person;
import me.mamre.workflow.hello.HelloWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Slf4j
public class HelloController {

    @Autowired WorkflowClient client;

    public HelloController() {
        log.info("Created HelloController");
    }

    @GetMapping("/hello")
    public String hello(Model model) {
        return "hello";
    }

    @PostMapping(
            value = "/hello",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.TEXT_HTML_VALUE})
    ResponseEntity<String> helloSample(@RequestBody Person person) {
        HelloWorkflow workflow =
                client.newWorkflowStub(
                        HelloWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue("HelloSampleTaskQueue")
                                .setWorkflowId("HelloSample")
                                .build());

        // bypass thymeleaf, don't return template name just result
        var result = workflow.sayHello(person);
        return new ResponseEntity<>("\"" + result + "\"", HttpStatus.OK);
    }
}
