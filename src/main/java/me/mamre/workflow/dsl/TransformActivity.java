package me.mamre.workflow.dsl;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.Map;

@ActivityInterface
public interface TransformActivity {
    @ActivityMethod
    TransformResult transform(String scriptId, Object payload, Map<String, Object> headers);
}

