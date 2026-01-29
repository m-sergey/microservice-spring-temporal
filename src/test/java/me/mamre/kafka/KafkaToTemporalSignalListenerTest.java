package me.mamre.kafka;

import static org.junit.jupiter.api.Assertions.*;

import groovy.lang.Binding;
import me.mamre.transformer.DbGroovyMessageTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbGroovyMessageTransformerTest {

    @Mock
    private DbGroovyMessageTransformer.ScriptRepository scriptRepository;

    @Mock
    private DbGroovyMessageTransformer.GroovyScriptEngine groovy;

    private DbGroovyMessageTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new DbGroovyMessageTransformer(scriptRepository, groovy);
    }

    @Test
    void shouldThrowWhenScriptIdMissing() {
        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("h1", "v1")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transformer.transform(in));
        assertTrue(ex.getMessage().contains("scriptId"));

        verifyNoInteractions(scriptRepository, groovy);
    }

    @Test
    void shouldThrowWhenScriptIdBlank() {
        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("scriptId", "   ")
                .build();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transformer.transform(in));
        assertTrue(ex.getMessage().contains("scriptId"));

        verifyNoInteractions(scriptRepository, groovy);
    }

    @Test
    void shouldReturnMessageAsIsWhenGroovyReturnsMessage() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 1L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("scriptId", "s1")
                .setHeader("h1", "v1")
                .build();

        Message<String> outMsg = MessageBuilder.withPayload("out")
                .setHeader("x", "y")
                .build();

        when(groovy.run(eq("s1"), eq(1L), eq("code"), any(Binding.class))).thenReturn(outMsg);

        Message<?> out = transformer.transform(in);

        assertSame(outMsg, out); // должен вернуть тот же объект

        verify(scriptRepository).getRequired("s1");
        verify(groovy).run(eq("s1"), eq(1L), eq("code"), any(Binding.class));
    }

    @Test
    void shouldTreatResultAsNewPayloadWhenGroovyReturnsPlainObject() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 2L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("scriptId", "s1")
                .setHeader("h1", "v1")
                .build();

        when(groovy.run(eq("s1"), eq(2L), eq("code"), any(Binding.class))).thenReturn("NEW_PAYLOAD");

        Message<?> out = transformer.transform(in);

        assertEquals("NEW_PAYLOAD", out.getPayload());
        assertEquals("v1", out.getHeaders().get("h1"));
        assertEquals("s1", out.getHeaders().get("scriptId"));
    }

    @Test
    void shouldSupportMapPayloadAndHeadersWhenGroovyReturnsMap() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 3L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("scriptId", "s1")
                .setHeader("h1", "v1")
                .setHeader("override", "IN")
                .build();

        Map<String, Object> result = Map.of(
                "payload", "OUT",
                "headers", Map.of(
                        "added", "YES",
                        "override", "OUT" // должно перезаписать IN
                )
        );

        when(groovy.run(eq("s1"), eq(3L), eq("code"), any(Binding.class))).thenReturn(result);

        Message<?> out = transformer.transform(in);

        assertEquals("OUT", out.getPayload());
        assertEquals("v1", out.getHeaders().get("h1"));
        assertEquals("YES", out.getHeaders().get("added"));
        assertEquals("OUT", out.getHeaders().get("override")); // перезаписано
    }

    @Test
    void shouldFilterNonStringHeaderKeys() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 4L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("in")
                .setHeader("scriptId", "s1")
                .build();

        // headers-map со смешанными ключами
        Map<Object, Object> headers = new LinkedHashMap<>();
        headers.put("ok", "V1");
        headers.put(123, "SHOULD_BE_DROPPED");

        Map<String, Object> result = Map.of(
                "payload", "OUT",
                "headers", headers
        );

        when(groovy.run(eq("s1"), eq(4L), eq("code"), any(Binding.class))).thenReturn(result);

        Message<?> out = transformer.transform(in);

        assertEquals("OUT", out.getPayload());
        assertEquals("V1", out.getHeaders().get("ok"));
        assertNull(out.getHeaders().get(123)); // не должно быть
    }

    @Test
    void shouldKeepOriginalPayloadWhenMapMissingPayload() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 5L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("IN_PAYLOAD")
                .setHeader("scriptId", "s1")
                .build();

        Map<String, Object> result = Map.of(
                "headers", Map.of("added", "YES")
        );

        when(groovy.run(eq("s1"), eq(5L), eq("code"), any(Binding.class))).thenReturn(result);

        Message<?> out = transformer.transform(in);

        assertEquals("IN_PAYLOAD", out.getPayload()); // payload не меняли
        assertEquals("YES", out.getHeaders().get("added"));
    }

    @Test
    void shouldPassBindingWithPayloadHeadersAndMessage() {
        var def = new DbGroovyMessageTransformer.ScriptDef("s1", 6L, "code");
        when(scriptRepository.getRequired("s1")).thenReturn(def);

        Message<String> in = MessageBuilder.withPayload("IN")
                .setHeader("scriptId", "s1")
                .setHeader("h1", "v1")
                .build();

        when(groovy.run(eq("s1"), eq(6L), eq("code"), any(Binding.class))).thenReturn("OUT");

        transformer.transform(in);

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(groovy).run(eq("s1"), eq(6L), eq("code"), captor.capture());

        Binding binding = captor.getValue();
        assertEquals("IN", binding.getVariable("payload"));
        assertNotNull(binding.getVariable("headers"));
        assertSame(in, binding.getVariable("message"));
    }
}
