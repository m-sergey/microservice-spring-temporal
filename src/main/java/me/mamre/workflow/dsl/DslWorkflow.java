package me.mamre.workflow.dsl;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import me.mamre.model.Flow;
import me.mamre.model.Payment;

@WorkflowInterface
public interface DslWorkflow {
    @WorkflowMethod
    String run(Flow flow, Payment input);
}