package me.mamre.controller;

import me.mamre.model.Payment;
import me.mamre.service.DslWorkflowService;
import me.mamre.util.Snowflake;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final DslWorkflowService service;
    private final Snowflake generator = new Snowflake();

    public PaymentController(DslWorkflowService service) {
        this.service = service;
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> runPayment(@RequestBody Payment payment) throws ClassNotFoundException {
        if(payment.getId() == 0) {
            payment.setId(generator.nextId());
        }
        var result = service.runFlow(payment);
        return new ResponseEntity<>("\"" + result + "\"", HttpStatus.OK);
    }
}
