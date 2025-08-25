package ll25.feedup.Payment.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_pay_order", columnNames = "order_id"),
                @UniqueConstraint(name = "uq_pay_key",   columnNames = "payment_key")
        })
public class Payment {

    @Getter
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "order_id", nullable = false, length = 64)
    private String orderId;

    @Getter
    @Column(name = "payment_key", length = 128)
    private String paymentKey;

    @Getter
    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Getter
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Getter
    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Getter
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Getter
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Getter
    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    protected Payment() {}

    private Payment(String orderId, Long hostId, Long planId, Long promotionId, Integer amount) {
        this.orderId = orderId;
        this.hostId = hostId;
        this.planId = planId;
        this.promotionId = promotionId;
        this.amount = amount;
        this.status = PaymentStatus.READY;
    }

    public static Payment ready(String orderId, Long hostId, Long planId, Long promotionId, Integer amount) {
        return new Payment(orderId, hostId, planId, promotionId, amount);
    }

    public void markApproving() {
        this.status = PaymentStatus.APPROVING;
    }

    public void applyApproved(String paymentKey, LocalDateTime approvedAt, String receiptUrl) {
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
        this.status = PaymentStatus.DONE;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELED;
    }
}