package me.mamre.workflow.hello;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import me.mamre.model.Person;

import java.time.Duration;

@WorkflowImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloWorkflowImpl implements HelloWorkflow {

    private HelloActivity activity =
            Workflow.newActivityStub(
                    HelloActivity.class,
                    ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String sayHello(Person person) {
        return activity.hello(person);
    }
}