package com.beg.shuttlespot.data.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class OutboxRetryPolicyTest {

    @Test
    fun `retryable failure remains pending below retry limit`() {
        assertEquals(
            OutboxAction.Retry("Server sementara tidak tersedia."),
            decideOutboxAction(
                result = MutationDeliveryResult.RetryableFailure(
                    "Server sementara tidak tersedia.",
                ),
                currentRetryCount = 3,
            ),
        )
    }

    @Test
    fun `retryable failure becomes permanent at retry limit`() {
        assertEquals(
            OutboxAction.FailPermanently("Batas percobaan sinkronisasi tercapai."),
            decideOutboxAction(
                result = MutationDeliveryResult.RetryableFailure(
                    "Server sementara tidak tersedia.",
                ),
                currentRetryCount = 4,
            ),
        )
    }

    @Test
    fun `invalid mutation fails permanently without retry`() {
        assertEquals(
            OutboxAction.FailPermanently("Payload outbox tidak valid."),
            decideOutboxAction(
                result = MutationDeliveryResult.PermanentFailure(
                    "Payload outbox tidak valid.",
                ),
                currentRetryCount = 0,
            ),
        )
    }
}
