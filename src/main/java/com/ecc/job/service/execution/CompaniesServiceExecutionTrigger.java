package com.ecc.job.service.execution;

import com.ecc.job.dto.TriggerJobRunRequest;
import com.ecc.job.model.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Starts a job by calling companies-service's {@code POST /internal/jobs/{jobId}/run} endpoint. That call only hands the work off — companies-service
 * responds immediately and reports progress and completion later via its own callbacks into job-service ({@code POST /internal/jobs/{jobId}/logs}
 * and {@code POST /internal/jobs/{jobId}/complete}), so this class doesn't publish a completion event on success. It does publish a failed
 * {@link JobCompletedEvent} itself if the handoff call fails outright, since companies-service will never learn about a job it was never told to
 * run, and the job would otherwise stay {@link com.ecc.job.model.JobStatus#RUNNING} forever.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@Profile("!test")
public class CompaniesServiceExecutionTrigger implements JobExecutionTrigger {

    private static final Logger log = LoggerFactory.getLogger(CompaniesServiceExecutionTrigger.class);

    private final RestClient restClient;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new trigger that calls companies-service at the given base URL. Builds its own {@link RestClient} via the static factory rather
     * than taking an injected {@code RestClient.Builder}, since that auto-configured bean isn't reliably available in this environment.
     *
     * @param baseUrl companies-service's base URL, e.g. {@code http://company-service.<namespace>.svc.cluster.local:8082}
     * @param events used to publish a failed {@link JobCompletedEvent} if the handoff call itself fails
     */
    public CompaniesServiceExecutionTrigger(@Value("${company.service.base-url}") String baseUrl, ApplicationEventPublisher events) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(10_000);

        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.events = events;
    }

    /**
     * {@inheritDoc}
     */
    @Async
    @Override
    public void trigger(Job job) {
        try {
            restClient
                .post()
                .uri("/internal/jobs/{jobId}/run", job.getId())
                .body(new TriggerJobRunRequest(job.getJobType(), job.getScope()))
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to hand job {} off to companies-service", job.getId(), e);
            events.publishEvent(new JobCompletedEvent(job.getId(), false));
        }
    }
}
