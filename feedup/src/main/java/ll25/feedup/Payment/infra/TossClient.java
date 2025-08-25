package ll25.feedup.Payment.infra;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class TossClient {

    private final WebClient webClient;
    private final TossProperties props;

    public TossClient(TossProperties props) {
        this.props = props;
        String raw = props.getSecretKey() + ":";
        String basic = "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));

        this.webClient = WebClient.builder()
                .baseUrl(verify(props.getEndpoint()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, basic)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public JsonNode confirm(String paymentKey, String orderId, int amount, String idempotencyKey) {
        return webClient.post()
                .uri("/v1/payments/confirm")
                .header("Idempotency-Key", idempotencyKey)
                .bodyValue(Map.of(
                        "paymentKey", paymentKey,
                        "orderId", orderId,
                        "amount", amount
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    private static String verify(String ep) {
        if (ep == null) throw new IllegalStateException("toss.endpoint missing");
        String v = ep.trim();
        if (!"https://api.tosspayments.com".equals(v)) {
            throw new IllegalStateException("toss.endpoint must be https://api.tosspayments.com (got: " + v + ")");
        }
        return v;
    }
}