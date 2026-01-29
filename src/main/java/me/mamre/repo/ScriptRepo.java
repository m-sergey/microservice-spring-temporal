package me.mamre.repo;

import me.mamre.model.ScriptDef;

public interface ScriptRepo {
    ScriptDef getRequired(String scriptId); // кидает исключение если не найден
}
