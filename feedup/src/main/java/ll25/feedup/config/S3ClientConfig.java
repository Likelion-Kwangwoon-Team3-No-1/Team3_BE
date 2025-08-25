package ll25.feedup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {
    @Bean
    public S3Client s3Client() {
        return S3Client.create();
    }
}
