package ll25.feedup.Payment.controller;

import jakarta.validation.Valid;
import ll25.feedup.Payment.dto.ConfirmRequest;
import ll25.feedup.Payment.dto.CreateOrderRequest;
import ll25.feedup.Payment.dto.CreateOrderResponse;
import ll25.feedup.Payment.dto.PaymentView;
import ll25.feedup.Payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    /** 주문 생성 */
    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(Authentication authentication,
                                                           @Valid @RequestBody CreateOrderRequest req) {
        String hostLoginId = authentication.getName(); // 너가 이미 이렇게 쓰고 있었지
        CreateOrderResponse res = service.createOrder(hostLoginId, req);
        return ResponseEntity.status(201).body(res);
    }

    /** 결제 승인(confirm) */
    @PostMapping("/confirm")
    public ResponseEntity<PaymentView> confirm(Authentication authentication,
                                               @Valid @RequestBody ConfirmRequest req) {
        String hostLoginId = authentication.getName();
        PaymentView res = service.confirm(hostLoginId, req);
        return ResponseEntity.ok(res);
    }

    /** 단건 조회 */
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentView> get(Authentication authentication,
                                           @PathVariable String orderId) {
        String hostLoginId = authentication.getName();
        PaymentView res = service.get(hostLoginId, orderId);
        return ResponseEntity.ok(res);
    }
}
