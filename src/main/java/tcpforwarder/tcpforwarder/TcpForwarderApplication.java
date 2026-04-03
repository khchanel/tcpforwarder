package tcpforwarder.tcpforwarder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tcpforwarder.tcpforwarder.config.AppProperties;
import tcpforwarder.tcpforwarder.engine.ForwardingEngine;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class TcpForwarderApplication implements ApplicationRunner {

    private final ForwardingEngine engine;

    public TcpForwarderApplication(ForwardingEngine engine) {
        this.engine = engine;
    }

    public static void main(String[] args) {
        SpringApplication.run(TcpForwarderApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) {
        engine.startAll();
    }
}
