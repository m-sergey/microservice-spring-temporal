package me.mamre.workflow.activity;

import me.mamre.model.Payment;
import me.mamre.transformer.DbGroovyMessageTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;

public class TransformActivityImpl implements TransformActivity {

    private final DbGroovyMessageTransformer transformer;

    public TransformActivityImpl(DbGroovyMessageTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public TransformResult transform(Payment payment) {

        String scriptId = payment.getParams().get("scriptId").toString();
        Map<String, Object> headers = (Map<String, Object>) payment.getParams().get("headers");

        Map<String, Object> allHeaders = new HashMap<>(headers == null ? Map.of() : headers);
        allHeaders.put("scriptId", scriptId);

        Message<Object> in = new GenericMessage<>(payment, allHeaders);
        Message<?> out = transformer.transform(in);

        // В Temporal лучше возвращать простые структуры (Map, POJO), а не Spring Message
        return new TransformResult(out.getPayload(), out.getHeaders());
    }
}
