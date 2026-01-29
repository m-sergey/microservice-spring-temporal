package me.mamre.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class KafkaDlqErrorPublisher implements ErrorPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.dlq-topic}")
    private String dlqTopic;

    @Override
    public void publishToDlq(ConsumerRecord<String, String> record, String workflowId, Exception exception) {
        ProducerRecord<String, String> out =
                new ProducerRecord<>(dlqTopic, record.key(), record.value());

        Headers h = out.headers();
        add(h, "x-original-topic", record.topic());
        add(h, "x-original-partition", Integer.toString(record.partition()));
        add(h, "x-original-offset", Long.toString(record.offset()));
        add(h, "x-original-timestamp", Long.toString(record.timestamp()));

        add(h, "x-workflow-id", workflowId);
        add(h, "x-exception-class", exception.getClass().getName());
        add(h, "x-exception-message", safe(exception.getMessage()));

        // Ждём подтверждение от брокера, чтобы безопасно ack offset
        kafkaTemplate.send(out).join();
    }

    private static void add(Headers headers, String key, String value) {
        headers.add(key, value.getBytes(StandardCharsets.UTF_8));
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
