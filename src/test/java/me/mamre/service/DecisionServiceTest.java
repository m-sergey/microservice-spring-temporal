package me.mamre.service;

import me.mamre.core.DecisionRegistry;
import me.mamre.repo.RuleVersion;
import me.mamre.repo.RuleVersionRepo;

import org.camunda.bpm.dmn.engine.*;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Ignore
class DecisionServiceTest {

    private DmnEngine engine;
    private RuleVersionRepo repo;
    private DecisionService service;

    @BeforeEach
    void setup() {
        engine = mock(DmnEngine.class);
        repo = mock(RuleVersionRepo.class);
        service = new DecisionService(engine, repo);
    }

    @Test
    void upload_shouldValidateAndStoreDecision() throws Exception {
        // given
        InputStream dmnStream = getClass().getResourceAsStream("/dmns/select-dish.dmn");
        assertNotNull(dmnStream, "DMN test file not found in resources");

        DmnDecision decision = mock(DmnDecision.class);
        when(decision.getKey()).thenReturn("d1");
        when(engine.parseDecisions(any(DmnModelInstance.class))).thenReturn(List.of(decision));
        when(repo.maxVersion("d1")).thenReturn(0);

        // when
        var result = service.upload(dmnStream, Optional.empty());

        // then
        assertEquals(1, result.size());
        assertEquals("d1", result.get(0).get("key"));

        ArgumentCaptor<RuleVersion> captor = ArgumentCaptor.forClass(RuleVersion.class);
        verify(repo).save(captor.capture());
        RuleVersion saved = captor.getValue();
        assertEquals("d1", saved.getDecisionKey());
        assertEquals(1, saved.getVersion());
        assertTrue(saved.getEnabled());
        assertNotNull(saved.getUploadedAt());
    }

    @Test
    void evaluate_shouldReturnResultFromEngine() {
        DecisionRegistry.VersionedDecision vd =
                new DecisionRegistry.VersionedDecision("d1", 1, "sha", mock(DmnDecision.class), Instant.now(), true);

        DmnDecisionResult result = mock(DmnDecisionResult.class);
        DmnDecisionResultEntries entry = mock(DmnDecisionResultEntries.class);

        // Явное приведение типов для корректной компиляции
        when(entry.entrySet())
                .thenReturn(((Map<String, Object>) (Map) Map.of("dish", "Salad")).entrySet());
        when(result.getResultList()).thenReturn(List.of(entry));

        // Уточняем сигнатуру метода для Mockito
        when(engine.evaluateDecision(eq(vd.compiled), any(Map.class))).thenReturn(result);

        DecisionRegistry registry = new DecisionRegistry();
        registry.put(vd);
        ReflectionTestUtils.setField(service, "registry", registry);

        Map<String, Object> res = service.evaluate("d1", 1, Map.of("season", "Summer"));

        assertEquals("Salad", res.get("result"));
        assertEquals("d1", res.get("key"));
        assertEquals(1, res.get("version"));
    }

    @Test
    void evaluate_shouldThrowIfDecisionNotFound() {
        assertThrows(NoSuchElementException.class, () ->
                service.evaluate("unknown", null, Map.of("x", 1))
        );
    }

    @Test
    void setEnabled_shouldUpdateVersionState() {
        RuleVersion rv = new RuleVersion();
        rv.setDecisionKey("d1");
        rv.setVersion(1);
        rv.setEnabled(true);
        rv.setUploadedAt(Instant.now());

        when(repo.findFirstByDecisionKeyAndVersion("d1", 1)).thenReturn(Optional.of(rv));

        service.setEnabled("d1", 1, false);

        assertFalse(rv.getEnabled());
        verify(repo).save(rv);
    }

    @Test
    void versions_shouldReturnVersionList() {
        RuleVersion v1 = new RuleVersion();
        v1.setVersion(1);
        v1.setEnabled(true);
        v1.setUploadedAt(Instant.now());

        RuleVersion v2 = new RuleVersion();
        v2.setVersion(2);
        v2.setEnabled(false);
        v2.setUploadedAt(Instant.now());

        when(repo.findByDecisionKeyOrderByVersionAsc("d1")).thenReturn(List.of(v1, v2));

        var list = service.versions("d1");
        assertEquals(2, list.size());
        assertEquals(1, list.get(0).get("version"));
        assertEquals(false, list.get(1).get("enabled"));
    }
}
