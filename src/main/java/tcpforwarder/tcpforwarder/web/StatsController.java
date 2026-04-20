package tcpforwarder.tcpforwarder.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcpforwarder.tcpforwarder.engine.ForwardingEngine;
import tcpforwarder.tcpforwarder.model.ConnectionSnapshot;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final RuleStats ruleStats;
    private final ForwardingEngine engine;

    public StatsController(RuleStats ruleStats, ForwardingEngine engine) {
        this.ruleStats = ruleStats;
        this.engine = engine;
    }

    @GetMapping("/stats")
    public Map<String, Map<String, Object>> allStats() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        ruleStats.getAll().forEach((ruleId, entry) -> result.put(ruleId, entry.toMap()));
        return result;
    }

    @GetMapping("/stats/{ruleId}")
    public ResponseEntity<Map<String, Object>> ruleStats(@PathVariable String ruleId) {
        RuleStats.StatEntry entry = ruleStats.get(ruleId);
        if (entry == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entry.toMap());
    }

    @GetMapping("/connections")
    public List<ConnectionSnapshot> connections() {
        return engine.getActiveSessions();
    }
}
