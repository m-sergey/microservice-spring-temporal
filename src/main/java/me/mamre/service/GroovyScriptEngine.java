package me.mamre.service;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

import java.util.concurrent.ConcurrentHashMap;

public class GroovyScriptEngine {

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private final ConcurrentHashMap<String, Class<? extends Script>> cache = new ConcurrentHashMap<>();

    public Object run(String scriptId, long version, String code, Binding binding) {
        String key = scriptId + ":" + version;

        Class<? extends Script> scriptClass = cache.computeIfAbsent(key, k -> {
            Class<?> parsed = groovyClassLoader.parseClass(code, "db_" + scriptId + "_" + version + ".groovy");
            return parsed.asSubclass(Script.class);
        });

        try {
            Script script = scriptClass.getDeclaredConstructor().newInstance();
            script.setBinding(binding);
            return script.run();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute groovy script " + scriptId + " v" + version, e);
        }
    }
}
