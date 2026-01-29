package me.mamre.workflow.dsl;

import io.temporal.activity.ActivityInterface;
import me.mamre.model.Payment;

@ActivityInterface
public interface DslActivities {
    String request(Payment payment);

    String validate(Payment payment);

    String transform(Payment payment);

    String signal(Payment payment);
}