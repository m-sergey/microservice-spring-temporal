package me.mamre.workflow.hello;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import me.mamre.model.Person;

@WorkflowInterface
public interface HelloWorkflow {
    @WorkflowMethod
    String sayHello(Person person);
}