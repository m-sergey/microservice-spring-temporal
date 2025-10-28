package me.mamre.core;

import org.camunda.bpm.dmn.engine.DmnDecision;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DecisionRegistry {

    public static final class VersionedDecision {
        public final String key;
        public final int version;
        public final String sha256;
        public final DmnDecision compiled;
        public final Instant uploadedAt;
        public final boolean enabled;

        public VersionedDecision(String key, int version, String sha256, DmnDecision compiled, Instant uploadedAt, boolean enabled) {
            this.key = key; this.version = version; this.sha256 = sha256;
            this.compiled = compiled; this.uploadedAt = uploadedAt; this.enabled = enabled;
        }
    }

    private final Map<String, NavigableMap<Integer, VersionedDecision>> storage = new ConcurrentHashMap<>();

    public synchronized void put(VersionedDecision vd) {
        var map = storage.computeIfAbsent(vd.key, k -> new TreeMap<>());
        map.put(vd.version, vd);
    }

    public Optional<VersionedDecision> latestEnabled(String key) {
        var map = storage.get(key);
        if (map == null || map.isEmpty()) return Optional.empty();
        return map.descendingMap().values().stream().filter(v -> v.enabled).findFirst();
    }

    public Optional<VersionedDecision> get(String key, Integer version) {
        var map = storage.get(key);
        if (map == null || map.isEmpty()) return Optional.empty();
        return version == null ? latestEnabled(key) : Optional.ofNullable(map.get(version));
    }

    public Map<String, List<Integer>> index() {
        Map<String, List<Integer>> res = new TreeMap<>();
        storage.forEach((k, v) -> res.put(k, new ArrayList<>(v.keySet())));
        return res;
    }
}
