package com.ecc.job.service.execution;

import com.ecc.job.model.Job;
import com.ecc.job.model.LogLevel;
import com.ecc.job.service.log.JobLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Test-only stand-in for {@link CompaniesServiceExecutionTrigger}, the real implementation that calls companies-service. Simulates the round trip
 * (job-service triggers work, the other side eventually reports back with progress and a result) entirely in-process, with a fixed delay and a
 * couple of canned log lines, so the test suite doesn't depend on a real companies-service instance being reachable.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@Profile("test")
public class MockJobExecutionTrigger implements JobExecutionTrigger {

    private final ApplicationEventPublisher events;
    private final JobLogService jobLogService;
    private final long delayMillis;

    /**
     * Creates a new trigger backed by the given event publisher and log service.
     *
     * @param events used to report completion via {@link JobCompletedEvent}
     * @param jobLogService used to record simulated progress lines while the job "runs"
     * @param delayMillis how long to simulate work for before reporting completion
     */
    public MockJobExecutionTrigger(
        ApplicationEventPublisher events,
        JobLogService jobLogService,
        @Value("${job.execution.mock-delay-ms:1000}") long delayMillis
    ) {
        this.events = events;
        this.jobLogService = jobLogService;
        this.delayMillis = delayMillis;
    }

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void trigger(Job job) {
        jobLogService.append(job.getId(), LogLevel.INFO, "Job started");

        sleep(delayMillis / 2);
        jobLogService.append(job.getId(), LogLevel.INFO, "Fetching data...");

        sleep(delayMillis / 2);
        jobLogService.append(job.getId(), LogLevel.INFO, "Job completed");

        events.publishEvent(new JobCompletedEvent(job.getId(), true));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
