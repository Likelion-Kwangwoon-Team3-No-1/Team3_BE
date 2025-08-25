package ll25.feedup.Payment.dto;

public record CreateOrderResponse(
        String orderId,
        int amount,
        String orderName
) {}