package me.mamre.workflow.dsl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import me.mamre.model.Payment;
import me.mamre.service.DecisionService;

import java.util.concurrent.TimeUnit;

@Slf4j
public class DslActivitiesImpl implements DslActivities {

    private final DecisionService decisionService;

    public DslActivitiesImpl(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    @Override
    public String one() {
        sleep(1);
        return "Activity one done...";
    }

    @Override
    public String two() {
        sleep(1);
        return "Activity two done...";
    }

    @Override
    public String three() {
        sleep(1);
        return "Activity three done...";
    }

    @Override
    public String four(Payment payment) {
        //decisionService.evaluate("validation-sampleFlow", 1, null);
        var result = decisionService.evaluate("sampleFlow_validation", 1, payment.getParams());
        var json = new Gson().toJson(result.get("result"));
        log.info("four: " + json);

        return json;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
        } catch (InterruptedException ee) {
            // Empty
        }
    }
}