package org.manzanita.commons;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Retry {

    public static <T> RetryBuilder<T> retry(Supplier<T> supplierFunction) {
        return new RetryBuilder<>(supplierFunction);
    }

    @RequiredArgsConstructor
    public static class RetryBuilder<T> {

        private final Supplier<T> supplierFunction;
        private int maxRetries = 5;
        private long delay = 1000;


        public RetryBuilder<T> maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public RetryBuilder<T> withDelay(long delay) {
            this.delay = delay;
            return this;
        }

        @SneakyThrows
        public T retry() {
            int attempts = 0;

            while (attempts < maxRetries) {
                try {
                    return supplierFunction.get();
                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxRetries) {
                        throw e;
                    }
                    log.warn("Attempt {} failed, retrying in {} ms...", attempts, delay);
                    try {
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }

            // This should never be reached
            throw new IllegalStateException("Max retries exceeded");
        }
    }

}
