package com.onlinestore.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseRetryJobTest {

    @Mock
    PendingReleaseRepository repository;

    @Mock
    ProductClient productClient;

    @InjectMocks
    ReleaseRetryJob job;

    @Test
    void successfulRetryDeletesTask() {
        PendingRelease task = new PendingRelease(1L, 3);
        when(repository.findByAttemptsLessThan(ReleaseRetryJob.MAX_ATTEMPTS)).thenReturn(List.of(task));

        job.retryFailedReleases();

        verify(productClient).release(1L, 3);
        verify(repository).delete(task);
    }

    @Test
    void failedRetryKeepsTaskAndCountsAttempt() {
        PendingRelease task = new PendingRelease(1L, 3);
        when(repository.findByAttemptsLessThan(ReleaseRetryJob.MAX_ATTEMPTS)).thenReturn(List.of(task));
        doThrow(new RuntimeException("still down")).when(productClient).release(1L, 3);

        job.retryFailedReleases();

        assertThat(task.getAttempts()).isEqualTo(1);
        verify(repository).save(task);
        verify(repository, never()).delete(task);
    }
}
