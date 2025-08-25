package ll25.feedup.Payment.config;

import ll25.feedup.Payment.infra.TossProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossProperties.class)
public class PaymentConfig { }
