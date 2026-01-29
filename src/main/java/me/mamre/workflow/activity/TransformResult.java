package me.mamre.workflow.activity;

import java.util.Map;

public record TransformResult(Object payload, Map<String, Object> headers) {}
