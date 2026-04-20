package tcpforwarder.tcpforwarder.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tcpforwarder.tcpforwarder.config.AppProperties;
import tcpforwarder.tcpforwarder.model.ForwardingRule;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RuleRepository {

    private static final Logger log = LoggerFactory.getLogger(RuleRepository.class);

    private final AppProperties props;
    private final ObjectMapper yamlMapper;
    private final Map<String, ForwardingRule> rules = new ConcurrentHashMap<>();

    public RuleRepository(AppProperties props) {
        this.props = props;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.yamlMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.yamlMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    public void load() {
        File file = new File(props.getRulesFile());
        if (!file.exists()) {
            log.info("Rules file not found at {}, starting with empty rule set", file.getAbsolutePath());
            return;
        }
        try {
            List<ForwardingRule> list = yamlMapper.readValue(file, new TypeReference<List<ForwardingRule>>() {});
            if (list != null) {
                list.forEach(r -> rules.put(r.getId(), r));
            }
            log.info("Loaded {} rule(s) from {}", rules.size(), file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to load rules from {}: {}", file.getAbsolutePath(), e.getMessage());
        }
    }

    public synchronized void save() {
        Path rulesPath = Paths.get(props.getRulesFile());
        Path tmpPath = Paths.get(props.getRulesFile() + ".tmp");
        try {
            // Ensure parent directory exists
            if (rulesPath.getParent() != null) {
                Files.createDirectories(rulesPath.getParent());
            }
            yamlMapper.writeValue(tmpPath.toFile(), new ArrayList<>(rules.values()));
            try {
                Files.move(tmpPath, rulesPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (UnsupportedOperationException e) {
                // ATOMIC_MOVE not supported on this filesystem (can happen on Windows across drives)
                Files.move(tmpPath, rulesPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Failed to save rules: {}", e.getMessage());
        }
    }

    public Collection<ForwardingRule> findAll() {
        return rules.values();
    }

    public Optional<ForwardingRule> findById(String id) {
        return Optional.ofNullable(rules.get(id));
    }

    public void upsert(ForwardingRule rule) {
        rules.put(rule.getId(), rule);
        save();
    }

    public boolean delete(String id) {
        boolean removed = rules.remove(id) != null;
        if (removed) save();
        return removed;
    }

    public boolean existsByListenPort(int port, String excludeId) {
        return rules.values().stream()
                .anyMatch(r -> r.getListenPort() == port && !r.getId().equals(excludeId));
    }
}
