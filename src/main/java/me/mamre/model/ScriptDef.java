package me.mamre.model;

public record ScriptDef(
        String id,
        long version,        // инкремент при каждом изменении
        String code
) {}

