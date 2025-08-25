package ll25.feedup.Payment.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmRequest(
        @NotNull String paymentKey,
        @NotNull String orderId,
        @NotNull Integer amount
) {}