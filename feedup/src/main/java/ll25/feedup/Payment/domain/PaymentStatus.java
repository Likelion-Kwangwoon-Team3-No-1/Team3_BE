package ll25.feedup.Payment.domain;

public enum PaymentStatus {
    READY, APPROVING, DONE, CANCELED;

    public boolean isFinal() {
        return this == DONE || this == CANCELED;
    }
}