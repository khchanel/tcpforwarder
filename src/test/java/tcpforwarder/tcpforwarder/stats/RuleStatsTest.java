package tcpforwarder.tcpforwarder.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleStatsTest {

    private RuleStats ruleStats;

    @BeforeEach
    void setUp() {
        ruleStats = new RuleStats();
    }

    @Test
    void testGetOrCreateCreatesNewEntry() {
        RuleStats.StatEntry entry = ruleStats.getOrCreate("rule-1");
        assertNotNull(entry);
        assertNotNull(ruleStats.get("rule-1"));
    }

    @Test
    void testGetOrCreateReturnsSameEntry() {
        RuleStats.StatEntry entry1 = ruleStats.getOrCreate("rule-1");
        RuleStats.StatEntry entry2 = ruleStats.getOrCreate("rule-1");
        assertSame(entry1, entry2);
    }

    @Test
    void testStatEntryRecordError() {
        RuleStats.StatEntry entry = ruleStats.getOrCreate("rule-1");
        String errorMessage = "Connection reset";
        entry.recordError(errorMessage);

        assertEquals(errorMessage, entry.lastError);
        assertNotNull(entry.lastErrorAt);
        assertEquals(1, entry.connectionErrors.get());
    }

    @Test
    void testStatEntryToMap() {
        RuleStats.StatEntry entry = ruleStats.getOrCreate("rule-1");
        entry.totalBytesIn.addAndGet(100);
        entry.totalBytesOut.addAndGet(200);
        entry.totalConnections.incrementAndGet();

        Map<String, Object> map = entry.toMap();

        assertEquals(100L, map.get("totalBytesIn"));
        assertEquals(200L, map.get("totalBytesOut"));
        assertEquals(1L, map.get("totalConnections"));
    }

    @Test
    void testRemoveStats() {
        ruleStats.getOrCreate("rule-1");
        assertNotNull(ruleStats.get("rule-1"));
        ruleStats.remove("rule-1");
        assertNull(ruleStats.get("rule-1"));
    }
}
