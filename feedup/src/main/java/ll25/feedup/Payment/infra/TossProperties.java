package ll25.feedup.Payment.infra;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "toss")
public class TossProperties {
    private String endpoint;
    private String secretKey;

}
