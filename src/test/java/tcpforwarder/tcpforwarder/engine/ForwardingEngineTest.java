package tcpforwarder.tcpforwarder.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.persistence.RuleRepository;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForwardingEngineTest {

    @Mock
    private RuleRepository repository;
    @Mock
    private RuleStats ruleStats;
    @Mock
    private AuditLogger auditLogger;

    private ForwardingEngine engine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        engine = new ForwardingEngine(repository, ruleStats, auditLogger);
    }

    @Test
    void testStartAllStartsEnabledRules() throws IOException {
        ForwardingRule rule1 = new ForwardingRule();
        rule1.setId("1");
        rule1.setName("Rule 1");
        rule1.setEnabled(true);
        rule1.setListenPort(9001);
        rule1.setTargetHost("localhost");
        rule1.setTargetPort(8080);

        ForwardingRule rule2 = new ForwardingRule();
        rule2.setId("2");
        rule2.setName("Rule 2");
        rule2.setEnabled(false);
        rule2.setListenPort(9002);

        when(repository.findAll()).thenReturn(List.of(rule1, rule2));

        ForwardingEngine spyEngine = spy(engine);
        doNothing().when(spyEngine).startRule(any());

        spyEngine.startAll();

        verify(spyEngine, times(1)).startRule(rule1);
        verify(spyEngine, never()).startRule(rule2);
    }

    @Test
    void testStopRuleStopsListener() throws IOException {
        ForwardingRule rule = new ForwardingRule();
        rule.setId("1");
        rule.setListenPort(9001);

        engine.startRule(rule);

        assertTrue(engine.isRunning("1"));
        engine.stopRule("1");
        assertFalse(engine.isRunning("1"));
    }
}
