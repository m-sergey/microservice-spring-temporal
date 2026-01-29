package me.mamre.workflow.dsl;

import java.util.Map;

public record TransformResult(Object payload, Map<String, Object> headers) {}
