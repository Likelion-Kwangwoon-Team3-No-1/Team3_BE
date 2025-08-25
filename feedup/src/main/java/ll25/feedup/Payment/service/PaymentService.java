package ll25.feedup.Payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import ll25.feedup.Host.domain.Host;
import ll25.feedup.Host.repository.HostRepository;
import ll25.feedup.Payment.repository.PaymentRepository;
import ll25.feedup.Plan.domain.Plan;
import ll25.feedup.Plan.repository.PlanRepository;
import ll25.feedup.Promotion.domain.Promotion;
import ll25.feedup.Promotion.repository.PromotionRepository;
import ll25.feedup.Payment.domain.Payment;
import ll25.feedup.Payment.dto.ConfirmRequest;
import ll25.feedup.Payment.dto.CreateOrderRequest;
import ll25.feedup.Payment.dto.CreateOrderResponse;
import ll25.feedup.Payment.dto.PaymentView;
import ll25.feedup.Payment.infra.TossClient;
import ll25.feedup.Payment.support.OrderKeyGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository payments;
    private final HostRepository hosts;
    private final PlanRepository plans;
    private final PromotionRepository promotions;
    private final TossClient tossClient;

    public PaymentService(PaymentRepository payments,
                          HostRepository hosts,
                          PlanRepository plans,
                          PromotionRepository promotions,
                          TossClient tossClient) {
        this.payments = payments;
        this.hosts = hosts;
        this.plans = plans;
        this.promotions = promotions;
        this.tossClient = tossClient;
    }

    public CreateOrderResponse createOrder(String hostLoginId, CreateOrderRequest req) {
        Host host = hosts.findByLoginId(hostLoginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Host not found"));

        Plan plan = plans.findById(req.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid planId"));

        Promotion promo = promotions.findById(req.promotionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid promotionId"));

        if (!promo.getHost().getId().equals(host.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Promotion not owned by host");
        }

        int amount = plan.getPrice(); // 단순 과금 (데모)
        String orderId = OrderKeyGenerator.newOrderId();

        Payment p = Payment.ready(orderId, host.getId(), plan.getId(), promo.getId(), amount);
        payments.save(p);

        String orderName = "FeedUp 플랜 결제 (" + plan.getName() + ")";
        return new CreateOrderResponse(orderId, amount, orderName);
    }

    public PaymentView confirm(String hostLoginId, ConfirmRequest req) {
        Host host = hosts.findByLoginId(hostLoginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Host not found"));

        Payment p = payments.findByOrderIdForUpdate(req.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!p.getHostId().equals(host.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your order");
        }
        if (p.getStatus().isFinal()) {
            return toView(p); // 멱등 처리: 이미 완료/취소면 현재 상태 반환
        }
        if (!p.getAmount().equals(req.amount())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Amount mismatch");
        }

        p.markApproving();

        String idem = OrderKeyGenerator.newIdempotencyKey();
        JsonNode res = tossClient.confirm(req.paymentKey(), req.orderId(), req.amount(), idem);


        String approvedAtText = text(res, "approvedAt");
        String receiptUrl = firstNonNull(
                text(res, "receiptUrl"),
                text(res.path("receipt"), "url")
        );

        String returnedOrderId = text(res, "orderId");
        String returnedPaymentKey = text(res, "paymentKey");

        if (!req.orderId().equals(returnedOrderId)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Order mismatch from PG");
        }
        if (returnedPaymentKey == null || returnedPaymentKey.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Invalid PG response");
        }

        p.applyApproved(returnedPaymentKey, parseTime(approvedAtText), receiptUrl);
        return toView(p);
    }

    @Transactional(readOnly = true)
    public PaymentView get(String hostLoginId, String orderId) {
        Host host = hosts.findByLoginId(hostLoginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Host not found"));

        Payment p = payments.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!p.getHostId().equals(host.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your order");
        }
        return toView(p);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    private static String firstNonNull(String a, String b) {
        return a != null && !a.isBlank() ? a : (b != null && !b.isBlank() ? b : null);
    }

    private static java.time.LocalDateTime parseTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            return OffsetDateTime.parse(iso).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static PaymentView toView(Payment p) {
        String approvedAt = p.getApprovedAt() == null ? null : p.getApprovedAt().toString();
        return new PaymentView(
                p.getStatus().name(),
                p.getOrderId(),
                p.getAmount(),
                approvedAt,
                p.getReceiptUrl()
        );
    }
}
