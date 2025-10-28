package me.mamre.controller;

import me.mamre.model.Payment;
import me.mamre.service.DslWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final DslWorkflowService service;

    public PaymentController(DslWorkflowService service) {
        this.service = service;
    }

    @GetMapping
    public String hello() {
        return "TBD";
    }

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<String> runPayment(@RequestBody Payment payment) throws ClassNotFoundException {
        var result = service.runFlow(payment);
        return new ResponseEntity<>("\"" + result + "\"", HttpStatus.OK);
    }
}
