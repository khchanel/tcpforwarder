package tcpforwarder.tcpforwarder.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import tcpforwarder.tcpforwarder.config.AppProperties;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);
    private static final int MAX_LIMIT = 1000;

    private final AppProperties props;
    private final ObjectMapper jsonMapper;

    public AuditController(AppProperties props, ObjectMapper jsonMapper) {
        this.props = props;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping
    public List<Map<String, Object>> audit(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String ruleId) {

        limit = Math.min(limit, MAX_LIMIT);
        File file = new File(props.getAuditLogFile());
        if (!file.exists()) {
            return Collections.emptyList();
        }

        List<String> lines = tailFile(file, limit * 2); // over-fetch to account for filtering
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = lines.size() - 1; i >= 0 && result.size() < limit; i--) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> entry = jsonMapper.readValue(line, Map.class);
                if (ruleId == null || ruleId.equals(entry.get("ruleId"))) {
                    result.add(entry);
                }
            } catch (IOException e) {
                // skip malformed lines
            }
        }
        return result;
    }

    private List<String> tailFile(File file, int maxLines) {
        List<String> lines = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            if (fileLength == 0) return lines;

            long pos = fileLength - 1;
            StringBuilder sb = new StringBuilder();
            int lineCount = 0;

            while (pos >= 0 && lineCount < maxLines) {
                raf.seek(pos);
                char c = (char) raf.read();
                if (c == '\n') {
                    if (sb.length() > 0) {
                        lines.add(sb.reverse().toString());
                        sb.setLength(0);
                        lineCount++;
                    }
                } else {
                    sb.append(c);
                }
                pos--;
            }
            if (sb.length() > 0) {
                lines.add(sb.reverse().toString());
            }
            Collections.reverse(lines);
        } catch (IOException e) {
            log.warn("Failed to read audit log: {}", e.getMessage());
        }
        return lines;
    }
}
