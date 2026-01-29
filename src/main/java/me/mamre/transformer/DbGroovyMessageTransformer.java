package me.mamre.transformer;
import groovy.lang.Binding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;
import org.springframework.messaging.Message;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Spring Integration Transformer that loads Groovy code from DB and transforms an incoming Message
 * into an outgoing Message.
 *
 * Java 16+ version (uses pattern matching for instanceof).
 *
 * Supported Groovy return types:
 *  1) org.springframework.messaging.Message  -> returned as-is
 *  2) Map with keys: 'payload' and/or 'headers'
 *  3) any other object                        -> treated as new payload (headers copied from input)
 */
public class DbGroovyMessageTransformer implements Transformer {

    private final ScriptRepository scriptRepository;
    private final GroovyScriptEngine groovy;

    public DbGroovyMessageTransformer(ScriptRepository scriptRepository, GroovyScriptEngine groovy) {
        this.scriptRepository = Objects.requireNonNull(scriptRepository, "scriptRepository");
        this.groovy = Objects.requireNonNull(groovy, "groovy");
    }

    @Override
    public Message<?> transform(Message<?> message) {
        Objects.requireNonNull(message, "message");

        // scriptId comes from headers (you can change to constructor param if you prefer)
        Object rawScriptId = message.getHeaders().get("scriptId");
        if (!(rawScriptId instanceof String scriptId) || scriptId.isBlank()) {
            throw new IllegalArgumentException("Missing or blank header 'scriptId'");
        }

        ScriptDef def = scriptRepository.getRequired(scriptId);

        Binding binding = new Binding(Map.of(
                "payload", message.getPayload(),
                "headers", message.getHeaders(),
                "message", message
        ));

        Object result = groovy.run(def.id(), def.version(), def.code(), binding);

        // 1) If Groovy returned a Message -> return it as-is
        if (result instanceof Message<?> outMsg) {
            return outMsg;
        }

        // 2) If Groovy returned a Map -> support {payload: ..., headers: {...}}
        if (result instanceof Map<?, ?> m) {
            Object outPayload = m.containsKey("payload") ? m.get("payload") : message.getPayload();

            Map<String, Object> extraHeaders = Map.of();
            if (m.get("headers") instanceof Map<?, ?> hm) {
                // Only keep String keys; drop anything else to avoid ClassCast issues in Spring headers
                extraHeaders = hm.entrySet().stream()
                        .filter(e -> e.getKey() instanceof String)
                        .collect(Collectors.toMap(
                                e -> (String) e.getKey(),
                                Map.Entry::getValue,
                                (a, b) -> b
                        ));
            }

            return MessageBuilder.withPayload(outPayload)
                    .copyHeaders(message.getHeaders())
                    .copyHeaders(extraHeaders)
                    .build();
        }

        // 3) Otherwise treat result as new payload, copy input headers
        return MessageBuilder.withPayload(result)
                .copyHeaders(message.getHeaders())
                .build();
    }

    // ===== Dependencies (your project should already have these types) =====

    public interface ScriptRepository {
        ScriptDef getRequired(String scriptId);
    }

    public record ScriptDef(String id, long version, String code) {}

    public interface GroovyScriptEngine {
        Object run(String scriptId, long version, String code, Binding binding);
    }
}
