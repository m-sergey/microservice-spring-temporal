package me.mamre.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ErrorPublisher {
    void publishToDlq(ConsumerRecord<String, String> record, String workflowId, Exception exception);
}