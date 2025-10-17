package me.mamre.service;

import me.mamre.model.Payment;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;
import me.mamre.model.Flow;
import me.mamre.workflow.dsl.DslActivitiesImpl;
import me.mamre.workflow.dsl.DslWorkflow;
import me.mamre.workflow.dsl.DslWorkflowBaseImpl;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DslWorkflowService {

    public static final WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    public static final WorkflowClient client = WorkflowClient.newInstance(service);
    public static final WorkerFactory factory = WorkerFactory.newInstance(client);

    private final Map<String, Flow> paymentWorkflows;

    public DslWorkflowService() {
        this.paymentWorkflows = getFlowFromResource();
        log.info("Created DslWorkflowService");
    }

    public String runFlow(Payment payment) {
        Flow flow = paymentWorkflows.get(payment.getSystem());

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("dsl-task-queue");
        worker.registerWorkflowImplementationTypes(DslWorkflowBaseImpl.class);
        worker.registerActivitiesImplementations(new DslActivitiesImpl());
        factory.start();

        DslWorkflow workflow =
                client.newWorkflowStub(
                        DslWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setWorkflowId(flow.getId())
                                .setTaskQueue("dsl-task-queue")
                                .build());

        String result = workflow.run(flow, "sample input");

        return result;
    }

    private Map<String, Flow> getFlowFromResource() {
        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Flow> workflows = new HashMap<>();
        try {
            URL folderUrl = DslWorkflowService.class.getClassLoader().getResource("workflows/");

            if (folderUrl != null) {
                File folder = new File(folderUrl.getFile());
                if (folder.exists() && folder.isDirectory()) {
                    File[] files = folder.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.isFile()) {
                                var flow =  objectMapper.readValue(file, Flow.class);
                                workflows.put(flow.getId(), flow);
                            }
                        }
                    }
                }
            }

//            var flow =  objectMapper.readValue(
//                    DslWorkflowService.class.getClassLoader().getResource("workflows/sampleflow.json"), Flow.class);


            return workflows;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
