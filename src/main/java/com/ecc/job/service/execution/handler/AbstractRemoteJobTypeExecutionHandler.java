package com.ecc.job.service.execution.handler;

import com.ecc.job.dto.TriggerJobRunRequest;
import com.ecc.job.model.Job;
import com.ecc.job.service.execution.JobCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;

/**
 * Base class for {@link JobTypeExecutionHandler}s that start work by POSTing to a downstream microservice's job-type-specific endpoint. That call
 * only hands the work off — the target service responds immediately and reports progress and completion later via its own callbacks into
 * job-service ({@code POST /internal/jobs/{jobId}/logs} and {@code POST /internal/jobs/{jobId}/complete}), so this class doesn't publish a
 * completion event on success. It does publish a failed {@link JobCompletedEvent} itself if the handoff call fails outright, since the target
 * service will never learn about a job it was never told to run, and the job would otherwise stay {@link com.ecc.job.model.JobStatus#RUNNING}
 * forever.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public abstract class AbstractRemoteJobTypeExecutionHandler implements JobTypeExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(AbstractRemoteJobTypeExecutionHandler.class);

    private final String path;
    private final RestClient restClient;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new handler that POSTs to {@code path} on the given base URL. Builds its own {@link RestClient} via the static factory rather than
     * an injected {@code RestClient.Builder}, since that auto-configured bean isn't reliably available in this environment.
     *
     * @param baseUrl the target service's base URL
     * @param path this job type's endpoint on that service — specific to the job type this handler {@link #supports()}, not shared with other
     *     handlers even when they target the same service
     * @param events used to publish a failed {@link JobCompletedEvent} if the handoff call itself fails
     */
    protected AbstractRemoteJobTypeExecutionHandler(String baseUrl, String path, ApplicationEventPublisher events) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(10_000);

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.path = path;
        this.events = events;
    }

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void trigger(Job job) {
        try {
            restClient.post().uri(path).body(new TriggerJobRunRequest(job.getId(), job.getScope())).retrieve().toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to hand job {} off (job type {})", job.getId(), job.getJobType(), e);
            events.publishEvent(new JobCompletedEvent(job.getId(), false));
        }
    }
}
