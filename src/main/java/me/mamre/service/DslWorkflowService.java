package me.mamre.service;

import io.grpc.StatusRuntimeException;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.WorkflowIdConflictPolicy;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflowservice.v1.DescribeWorkflowExecutionRequest;
import me.mamre.model.Payment;
import me.mamre.workflow.dsl.DslWorkflowImpl;
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
    private final DecisionService decisionService;

    public DslWorkflowService(DecisionService decisionService) {
        this.paymentWorkflows = getFlowFromResource();
        this.decisionService = decisionService;
        log.info("Created DslWorkflowService");
    }

    public String runFlow(Payment payment) throws ClassNotFoundException {
        Flow flow = paymentWorkflows.get(payment.getSystem());

        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker("dsl-task-queue");
        worker.registerWorkflowImplementationTypes(DslWorkflowImpl.class);
        worker.registerActivitiesImplementations(new DslActivitiesImpl(decisionService));
        factory.start();

        String workflowId = Long.toString(payment.getId());
//        checkExistingWorkflowId(workflowId);

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setWorkflowId(workflowId)
                .setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)
                .setWorkflowIdConflictPolicy(WorkflowIdConflictPolicy.WORKFLOW_ID_CONFLICT_POLICY_FAIL)
                .setTaskQueue("dsl-task-queue")
                .build();

        DslWorkflow workflow =
                client.newWorkflowStub(DslWorkflow.class, options);

        String result = workflow.run(flow, payment);

        return result;
    }

    private void checkExistingWorkflowId (String workflowId) {
        try {
            WorkflowExecution execution = WorkflowExecution.newBuilder()
                    .setWorkflowId(workflowId)
                    .build();
            client.getWorkflowServiceStubs()
                    .blockingStub()
                    .describeWorkflowExecution(DescribeWorkflowExecutionRequest.newBuilder()
                            .setNamespace("default")
                            .setExecution(execution)
                            .build());
            throw new RuntimeException("Workflow с ID " + workflowId + " уже существует!");
        } catch (StatusRuntimeException e) {
            return;
        }
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
