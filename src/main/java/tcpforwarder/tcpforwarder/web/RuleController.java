package tcpforwarder.tcpforwarder.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcpforwarder.tcpforwarder.engine.ForwardingEngine;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.persistence.RuleRepository;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleRepository repository;
    private final ForwardingEngine engine;

    public RuleController(RuleRepository repository, ForwardingEngine engine) {
        this.repository = repository;
        this.engine = engine;
    }

    @GetMapping
    public Collection<ForwardingRule> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ForwardingRule> get(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RuleRequest req) {
        if (repository.existsByListenPort(req.listenPort, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "A rule with listenPort " + req.listenPort + " already exists"));
        }
        ForwardingRule rule = new ForwardingRule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(req.name);
        rule.setListenPort(req.listenPort);
        rule.setTargetHost(req.targetHost);
        rule.setTargetPort(req.targetPort);
        rule.setEnabled(req.enabled);
        rule.setCreatedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());
        repository.upsert(rule);
        if (rule.isEnabled()) {
            try {
                engine.startRule(rule);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Rule saved but failed to start: " + e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody RuleRequest req) {
        return repository.findById(id).map(rule -> {
            if (repository.existsByListenPort(req.listenPort, id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body((Object) Map.of("error", "A rule with listenPort " + req.listenPort + " already exists"));
            }
            rule.setName(req.name);
            rule.setListenPort(req.listenPort);
            rule.setTargetHost(req.targetHost);
            rule.setTargetPort(req.targetPort);
            rule.setEnabled(req.enabled);
            rule.setUpdatedAt(Instant.now());
            repository.upsert(rule);
            try {
                engine.restartRule(rule);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body((Object) Map.of("error", "Rule saved but failed to restart: " + e.getMessage()));
            }
            return ResponseEntity.ok((Object) rule);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        engine.stopRule(id);
        repository.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable String id) {
        return repository.findById(id).map(rule -> {
            rule.setEnabled(true);
            rule.setUpdatedAt(Instant.now());
            repository.upsert(rule);
            try {
                engine.startRule(rule);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body((Object) Map.of("error", "Failed to start: " + e.getMessage()));
            }
            return ResponseEntity.ok((Object) rule);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable String id) {
        return repository.findById(id).map(rule -> {
            rule.setEnabled(false);
            rule.setUpdatedAt(Instant.now());
            repository.upsert(rule);
            engine.stopRule(id);
            return ResponseEntity.ok((Object) rule);
        }).orElse(ResponseEntity.notFound().build());
    }

    public static class RuleRequest {
        @NotBlank
        public String name;
        @Min(1) @Max(65535)
        public int listenPort;
        @NotBlank
        public String targetHost;
        @Min(1) @Max(65535)
        public int targetPort;
        public boolean enabled = true;
    }
}
