package me.mamre.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowNotFoundException;
import lombok.RequiredArgsConstructor;
import me.mamre.model.PaymentStatusEvent;
import me.mamre.workflow.dsl.DslWorkflow;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaToTemporalSignalListener {

    private final WorkflowClient workflowClient;
    private final ObjectMapper objectMapper;
    private final ErrorPublisher errorPublisher;

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        PaymentStatusEvent event = objectMapper.readValue(record.value(), PaymentStatusEvent.class);
        String workflowId = "payment/" + event.getPaymentId();

        try {
            // ВАЖНО: это НЕ стартует workflow, а только сигналит по workflowId
            DslWorkflow wf = workflowClient.newWorkflowStub(DslWorkflow.class, workflowId);
            wf.sendSignal();
///!!!            wf.onStatus(event);

            ack.acknowledge();
        } catch (WorkflowNotFoundException e) {
            // workflow отсутствует/закрыт -> DLQ и дальше
            errorPublisher.publishToDlq(record, workflowId, e);
            ack.acknowledge();
        }
        // Другие исключения (например, Temporal недоступен) — лучше НЕ ack,
        // чтобы Kafka retry/обработчик ошибок отработали.
    }
}
