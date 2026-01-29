package me.mamre.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import me.mamre.model.Payment;

import java.util.Map;

@ActivityInterface
public interface TransformActivity {
    
    @ActivityMethod
    TransformResult transform(Payment payment);
}

