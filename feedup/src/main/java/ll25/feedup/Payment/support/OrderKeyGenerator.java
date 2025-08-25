package ll25.feedup.Payment.support;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class OrderKeyGenerator {
    private static final SecureRandom RND = new SecureRandom();
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderKeyGenerator() {}

    public static String newOrderId() {
        String ts = LocalDateTime.now().format(F);
        int rand = 100000 + RND.nextInt(900000);
        return "ORD-" + ts + "-" + Integer.toHexString(rand).toUpperCase();
    }

    public static String newIdempotencyKey() {
        long a = RND.nextLong();
        long b = RND.nextLong();
        return Long.toHexString(a).toUpperCase() + "-" + Long.toHexString(b).toUpperCase();
    }
}
