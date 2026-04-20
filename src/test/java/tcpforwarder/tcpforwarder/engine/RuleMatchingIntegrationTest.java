package tcpforwarder.tcpforwarder.engine;

import org.junit.jupiter.api.Test;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.persistence.RuleRepository;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuleMatchingIntegrationTest {

    @Test
    void testStartAllStartsEnabledRules() throws IOException {
        RuleRepository repository = mock(RuleRepository.class);
        RuleStats ruleStats = mock(RuleStats.class);
        AuditLogger auditLogger = mock(AuditLogger.class);
        ForwardingEngine engine = new ForwardingEngine(repository, ruleStats, auditLogger);

        ForwardingRule rule1 = new ForwardingRule();
        rule1.setId("1");
        rule1.setName("Enabled Rule");
        rule1.setEnabled(true);
        rule1.setListenPort(9001);
        rule1.setTargetHost("localhost");
        rule1.setTargetPort(8080);

        ForwardingRule rule2 = new ForwardingRule();
        rule2.setId("2");
        rule2.setName("Disabled Rule");
        rule2.setEnabled(false);
        rule2.setListenPort(9002);
        rule2.setTargetHost("localhost");
        rule2.setTargetPort(8081);

        when(repository.findAll()).thenReturn(List.of(rule1, rule2));

        ForwardingEngine spyEngine = spy(engine);
        doNothing().when(spyEngine).startRule(any());

        spyEngine.startAll();

        verify(spyEngine, times(1)).startRule(rule1);
        verify(spyEngine, never()).startRule(rule2);
    }
}
