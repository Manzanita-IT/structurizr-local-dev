package org.manzanita.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class RetryTest {

    @Test
    void retryEventuallySucceeds() {
        AtomicInteger attempts = new AtomicInteger();
        String result = Retry.retry(() -> {
                    int a = attempts.incrementAndGet();
                    if (a < 3) {
                        throw new RuntimeException("fail " + a);
                    }
                    return "ok";
                })
                .maxRetries(5)
                .withDelay(1)
                .retry();

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void retryFailsAfterMaxRetries() {
        AtomicInteger attempts = new AtomicInteger();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> Retry.retry(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("always fail");
                })
                .maxRetries(3)
                .withDelay(1)
                .retry());

        assertEquals("always fail", ex.getMessage());
        assertEquals(3, attempts.get());
    }

    @Test
    void retryInterruptedDuringDelayThrowsAndPreservesInterruptFlag() {
        AtomicInteger attempts = new AtomicInteger();
        try {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> Retry.retry(() -> {
                        // Interrupt the current thread and fail so Retry will attempt to sleep and be interrupted
                        Thread.currentThread().interrupt();
                        attempts.incrementAndGet();
                        throw new RuntimeException("fail to trigger retry");
                    })
                    .maxRetries(5)
                    .withDelay(100)
                    .retry());

            assertEquals("Retry interrupted", ex.getMessage());
            assertInstanceOf(InterruptedException.class, ex.getCause());
            assertTrue(Thread.currentThread().isInterrupted(),
                    "Interrupt flag should be preserved");
            assertEquals(1, attempts.get(),
                    "Supplier should be invoked only once due to interruption");
        } finally {
            // Clear interrupt status to avoid affecting other tests
            Thread.interrupted();
        }
    }
}
