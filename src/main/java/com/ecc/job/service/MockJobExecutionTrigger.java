package com.ecc.job.service;

import com.ecc.job.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Temporary stand-in for the future outbound call to companies-service, which doesn't exist yet. Simulates the round trip (job-service triggers work,
 * the other side eventually reports back) with a fixed delay instead of a real HTTP call. Swapping this out for a real {@link JobExecutionTrigger}
 * implementation later shouldn't require any changes to {@link JobDispatchService} or anything else that depends on this interface.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class MockJobExecutionTrigger implements JobExecutionTrigger {

    private final ApplicationEventPublisher events;
    private final long delayMillis;

    /**
     * Creates a new trigger backed by the given event publisher.
     *
     * @param events used to report completion via {@link JobCompletedEvent}
     * @param delayMillis how long to simulate work for before reporting completion
     */
    public MockJobExecutionTrigger(ApplicationEventPublisher events, @Value("${job.execution.mock-delay-ms:1000}") long delayMillis) {
        this.events = events;
        this.delayMillis = delayMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void trigger(Job job) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        events.publishEvent(new JobCompletedEvent(job.getId(), true));
    }
}
