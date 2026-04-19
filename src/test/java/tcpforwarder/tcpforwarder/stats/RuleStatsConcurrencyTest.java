package tcpforwarder.tcpforwarder.stats;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RuleStatsConcurrencyTest {

    @Test
    void testConcurrentStatUpdates() throws InterruptedException {
        RuleStats ruleStats = new RuleStats();
        String ruleId = "concurrent-rule";
        int threadCount = 10;
        int updatesPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                for (int j = 0; j < updatesPerThread; j++) {
                    RuleStats.StatEntry entry = ruleStats.getOrCreate(ruleId);
                    entry.totalBytesIn.addAndGet(1);
                    entry.totalBytesOut.addAndGet(1);
                    entry.totalConnections.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        RuleStats.StatEntry entry = ruleStats.get(ruleId);
        assertNotNull(entry);
        assertEquals(threadCount * updatesPerThread, entry.totalBytesIn.get());
        assertEquals(threadCount * updatesPerThread, entry.totalBytesOut.get());
        assertEquals(threadCount * updatesPerThread, entry.totalConnections.get());
    }
    
    @Test
    void testConcurrentGetOrCreate() throws InterruptedException {
        RuleStats ruleStats = new RuleStats();
        String ruleId = "concurrent-get-or-create";
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                RuleStats.StatEntry entry = ruleStats.getOrCreate(ruleId);
                assertNotNull(entry);
            });
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        
        assertNotNull(ruleStats.get(ruleId));
    }
}
