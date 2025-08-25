package ll25.feedup.Payment.dto;

import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull Long planId,
        @NotNull Long promotionId
) {}