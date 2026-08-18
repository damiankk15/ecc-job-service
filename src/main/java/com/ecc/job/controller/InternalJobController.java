package com.ecc.job.controller;

import com.ecc.job.dto.JobCompletionRequest;
import com.ecc.job.service.execution.JobCompletedEvent;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal, service-to-service endpoint for reporting that a job finished.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/internal/jobs")
public class InternalJobController {

    private final ApplicationEventPublisher events;

    /**
     * Creates a new controller backed by the given event publisher.
     *
     * @param events used to publish a {@link JobCompletedEvent}, which {@code JobDispatchService} listens for
     */
    public InternalJobController(ApplicationEventPublisher events) {
        this.events = events;
    }

    /**
     * Records that a job finished. Doesn't verify the job still exists first — the listener that reacts to this already tolerates a job having been
     * deleted while it was running, so there's nothing gained by checking twice.
     *
     * @param jobId the job id
     * @param request whether the job succeeded
     * @return {@code 202 Accepted}
     */
    @PostMapping("/{jobId}/complete")
    public ResponseEntity<Void> completeJob(@PathVariable long jobId, @Valid @RequestBody JobCompletionRequest request) {
        events.publishEvent(new JobCompletedEvent(jobId, request.success()));

        return ResponseEntity.accepted().build();
    }
}
