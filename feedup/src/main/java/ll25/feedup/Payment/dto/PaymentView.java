package ll25.feedup.Payment.dto;

public record PaymentView(
        String status,
        String orderId,
        Integer amount,
        String approvedAt,
        String receiptUrl
) {}