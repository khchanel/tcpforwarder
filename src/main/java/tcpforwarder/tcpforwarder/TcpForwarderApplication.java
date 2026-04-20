package tcpforwarder.tcpforwarder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import tcpforwarder.tcpforwarder.config.AppProperties;
import tcpforwarder.tcpforwarder.engine.ForwardingEngine;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TcpForwarderApplication implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TcpForwarderApplication.class);

    private final ForwardingEngine engine;
    private final Environment environment;

    public TcpForwarderApplication(ForwardingEngine engine, Environment environment) {
        this.engine = engine;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(TcpForwarderApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        // Log startup info
        String port = environment.getProperty("server.port");
        logger.info("TCP Forwarder started on http://localhost:{}", port);
        logger.info("API: http://localhost:{}/api", port);

        engine.startAll();
    }
}
