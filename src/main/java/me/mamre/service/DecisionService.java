package me.mamre.service;

import me.mamre.core.DecisionRegistry;
import me.mamre.repo.RuleVersion;
import me.mamre.repo.RuleVersionRepo;
import org.camunda.bpm.dmn.engine.*;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.camunda.bpm.engine.variable.Variables.createVariables;

@Service
public class DecisionService {

    private final DmnEngine engine;
    private final DecisionRegistry registry = new DecisionRegistry();
    private final RuleVersionRepo repo;

    private volatile Instant lastScan = Instant.EPOCH;

    public DecisionService(DmnEngine engine, RuleVersionRepo repo) {
        this.engine = engine;
        this.repo = repo;
        refreshFromDb(Instant.EPOCH);
    }

    public List<Map<String, Object>> upload(InputStream dmnXml, Optional<String> decisionKeyOpt) throws Exception {
        byte[] bytes = dmnXml.readAllBytes();
        String sha = sha256(bytes);
        DmnModelInstance model = Dmn.readModelFromStream(new ByteArrayInputStream(bytes));
        try {
            Dmn.validateModel(model);
        } catch (ModelValidationException ex) {
            throw new IllegalArgumentException("DMN validation failed: " + ex.getMessage(), ex);
        }

        List<DmnDecision> decisions = decisionKeyOpt.isPresent()
                ? List.of(engine.parseDecision(decisionKeyOpt.get(), model))
                : engine.parseDecisions(model);

        List<Map<String, Object>> added = new ArrayList<>();
        for (DmnDecision d : decisions) {
            String key = d.getKey();
            int next = Math.max(1, repo.maxVersion(key) + 1);

            var ent = new RuleVersion();
            ent.setDecisionKey(key);
            ent.setVersion(next);
            ent.setSha256(sha);
            ent.setXml(bytes);
            ent.setEnabled(true);
            ent.setUploadedAt(Instant.now());
            repo.save(ent);

            registry.put(new DecisionRegistry.VersionedDecision(
                    key, next, sha, d, ent.getUploadedAt(), true
            ));
            added.add(Map.of("key", key, "version", next, "uploadedAt", ent.getUploadedAt().toString()));
        }
        lastScan = Instant.now();
        return added;
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledRefresh() {
        refreshFromDb(lastScan);
        lastScan = Instant.now();
    }

    private void refreshFromDb(Instant since) {
        var changed = repo.findByUploadedAtAfterOrderByUploadedAtAsc(since);
        for (RuleVersion rv : changed) {
            try {
                DmnModelInstance model = Dmn.readModelFromStream(new ByteArrayInputStream(rv.getXml()));
                Dmn.validateModel(model);
                DmnDecision compiled = engine.parseDecision(rv.getDecisionKey(), model);
                registry.put(new DecisionRegistry.VersionedDecision(
                        rv.getDecisionKey(), rv.getVersion(), rv.getSha256(), compiled, rv.getUploadedAt(), rv.getEnabled()
                ));
            } catch (Exception ex) {
                System.err.println("Failed to refresh decision " + rv.getDecisionKey() + " v" + rv.getVersion() + ": " + ex.getMessage());
            }
        }
    }

    public Map<String, Object> evaluate(String key, Integer version, Map<String, Object> payload) {
        var vd = registry.get(key, version).orElseThrow(() ->
                new NoSuchElementException("Decision not found or disabled: key=" + key + (version == null ? "" : ", version=" + version)));

        var vm = createVariables();
        payload.forEach(vm::putValue);

        DmnDecisionResult result = engine.evaluateDecision(vd.compiled, vm);

        List<Map<String, Object>> rows = result.getResultList().stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            r.entrySet().forEach(e -> m.put(e.getKey(), e.getValue())); // ← исправлено
            return m;
        }).collect(Collectors.toList());

        Object normalized = (rows.size() == 1 && rows.get(0).size() == 1)
                ? rows.get(0).values().iterator().next()
                : rows;

        return Map.of("key", key, "version", vd.version, "result", normalized);
    }
    public Map<String, List<Integer>> list() { return registry.index(); }

    public List<Map<String, Object>> versions(String key) {
        return repo.findByDecisionKeyOrderByVersionAsc(key).stream()
                .map(rv -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("version", rv.getVersion());
                    map.put("enabled", rv.getEnabled());
                    map.put("uploadedAt", rv.getUploadedAt().toString());
                    return map;
                })
                .collect(Collectors.<Map<String, Object>>toList());
    }

    public void setEnabled(String key, Integer version, boolean enabled) {
        var rv = repo.findFirstByDecisionKeyAndVersion(key, version)
                .orElseThrow(() -> new NoSuchElementException("Version not found: "+key+"#"+version));
        rv.setEnabled(enabled);
        repo.save(rv);
        refreshFromDb(rv.getUploadedAt().minusSeconds(1));
    }

    private static String sha256(byte[] bytes) throws Exception {
        var md = MessageDigest.getInstance("SHA-256");
        var d = md.digest(bytes);
        var sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
