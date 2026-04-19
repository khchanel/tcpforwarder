package tcpforwarder.tcpforwarder.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tcpforwarder.tcpforwarder.audit.AuditLogger;
import tcpforwarder.tcpforwarder.model.ForwardingRule;
import tcpforwarder.tcpforwarder.stats.RuleStats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConnectionSessionTest {

    @Mock
    private ForwardingRule rule;
    @Mock
    private RuleStats ruleStats;
    @Mock
    private AuditLogger auditLogger;
    @Mock
    private Socket clientSocket;
    @Mock
    private Socket targetSocket;
    @Mock
    private RuleStats.StatEntry statEntry;

    private ExecutorService executor;
    private Consumer<String> onEnd;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        executor = Executors.newSingleThreadExecutor();
        onEnd = mock(Consumer.class);

        when(rule.getId()).thenReturn("rule-1");
        when(rule.getName()).thenReturn("test-rule");
        when(clientSocket.getInetAddress()).thenReturn(InetAddress.getLoopbackAddress());

        // Initialize a real StatEntry to avoid NPE on field access
        RuleStats.StatEntry realEntry = new RuleStats.StatEntry();
        when(ruleStats.getOrCreate(anyString())).thenReturn(realEntry);
    }

    @Test
    void testSessionEndTriggersCallback() throws IOException {
        when(clientSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(targetSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(targetSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(clientSocket.isClosed()).thenReturn(true);

        ConnectionSession session = new ConnectionSession(
                clientSocket, targetSocket, rule,
                ruleStats, auditLogger, onEnd
        );

        session.pipeClientToTarget();

        verify(onEnd, atLeastOnce()).accept(anyString());
    }

    @Test
    void testSessionHandlesClientDisconnect() throws IOException {
        when(clientSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(targetSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(targetSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        
        // Simulate client disconnecting by throwing IOException on read
        when(clientSocket.getInputStream()).thenThrow(new IOException("Client disconnected"));

        ConnectionSession session = new ConnectionSession(
                clientSocket, targetSocket, rule,
                ruleStats, auditLogger, onEnd
        );

        assertDoesNotThrow(() -> session.pipeClientToTarget());
        verify(onEnd, atLeastOnce()).accept(anyString());
    }

    @Test
    void testSessionHandlesTargetDisconnect() throws IOException {
        when(clientSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(targetSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(targetSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        
        // Simulate target disconnecting
        doThrow(new IOException("Target disconnected")).when(targetSocket).getOutputStream();

        ConnectionSession session = new ConnectionSession(
                clientSocket, targetSocket, rule,
                ruleStats, auditLogger, onEnd
        );

        assertDoesNotThrow(() -> session.pipeClientToTarget());
        verify(onEnd, atLeastOnce()).accept(anyString());
    }
}
