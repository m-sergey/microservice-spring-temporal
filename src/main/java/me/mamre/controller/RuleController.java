package me.mamre.controller;

import com.google.gson.Gson;
import me.mamre.dto.RuleMetadataDto;
import me.mamre.service.DecisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rule")
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
}
