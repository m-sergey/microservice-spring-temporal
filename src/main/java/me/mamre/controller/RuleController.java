package me.mamre.controller;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import me.mamre.dto.RuleMetadataDto;
import me.mamre.service.DecisionService;
import me.mamre.service.DslWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rule")
@Slf4j
public class RuleController {

    private final DecisionService service;
    public RuleController(DecisionService service) {
        this.service = service;
    }

    @RequestMapping(
            method = RequestMethod.GET,
            produces = { "application/json" }
    )
    public ResponseEntity<Map<String, List<Integer>>> rulesGet() {
        return ResponseEntity.ok(service.list());
    }

    @RequestMapping(
            method = RequestMethod.POST,
            produces = { "application/json" },
            consumes = { "multipart/form-data" }
    )
    public ResponseEntity<List<RuleMetadataDto>> rulesPost(MultipartFile file, String decisionKey) {
        log.info("rulesPost");
        try {
            Gson gson = new Gson();
            var added = service.upload(file.getInputStream(), Optional.ofNullable(decisionKey));
            List<RuleMetadataDto> dtos = new ArrayList<>();
            for (var m : added) {
                var dto = new RuleMetadataDto();
                dto.setKey((String)m.get("key"));
                dto.setVersion(((Number)m.get("version")).intValue());
                dto.setUploadedAt(OffsetDateTime.parse((String)m.get("uploadedAt")));
                dtos.add(dto);
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(
            value = "/init",
            method = RequestMethod.POST,
            produces = { "application/json" }
    )
    public ResponseEntity<List<RuleMetadataDto>> rulesInit() {
        log.info("rulesInit");
        try {
            Gson gson = new Gson();
            URL file = DslWorkflowService.class.getClassLoader().getResource("dmns/select-dish.dmn");

//            InputStream input = RuleController.class.getClass().getResourceAsStream("/dmns/select-dish.dmn");
//            log.info(String.valueOf(input.available()));
            var added = service.upload(Files.newInputStream(Path.of(file.getPath())), Optional.empty());
            List<RuleMetadataDto> dtos = new ArrayList<>();
            for (var m : added) {
                var dto = new RuleMetadataDto();
                dto.setKey((String)m.get("key"));
                dto.setVersion(((Number)m.get("version")).intValue());
                dto.setUploadedAt(OffsetDateTime.parse((String)m.get("uploadedAt")));
                dtos.add(dto);
            }
            return ResponseEntity.ok(dtos);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
