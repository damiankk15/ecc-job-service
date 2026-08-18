package com.ecc.job.controller;

import com.ecc.job.assembler.JobLogModelAssembler;
import com.ecc.job.dto.ApiResponse;
import com.ecc.job.model.JobLog;
import com.ecc.job.model.JobLogModel;
import com.ecc.job.service.log.JobLogService;
import com.ecc.job.service.log.JobLogStreamRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller exposing a job's execution log, returned as HAL+JSON wrapped in {@link ApiResponse} and as a live event stream.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/api/jobs/{jobId}/logs")
public class JobLogController {

    private final JobLogService jobLogService;
    private final JobLogModelAssembler jobLogModelAssembler;
    private final PagedResourcesAssembler<JobLog> pagedResourcesAssembler;
    private final JobLogStreamRegistry jobLogStreamRegistry;

    /**
     * Creates a new controller backed by the given service, HATEOAS assemblers, and stream registry.
     *
     * @param jobLogService the service used to query a job's log lines
     * @param jobLogModelAssembler converts a {@link JobLog} to a {@link JobLogModel} with links
     * @param pagedResourcesAssembler converts a {@link Page} of log lines to a HATEOAS {@link PagedModel}
     * @param jobLogStreamRegistry opens live log streams for a job
     */
    public JobLogController(
        JobLogService jobLogService,
        JobLogModelAssembler jobLogModelAssembler,
        PagedResourcesAssembler<JobLog> pagedResourcesAssembler,
        JobLogStreamRegistry jobLogStreamRegistry
    ) {
        this.jobLogService = jobLogService;
        this.jobLogModelAssembler = jobLogModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.jobLogStreamRegistry = jobLogStreamRegistry;
    }

    /**
     * Lists a job's log lines, oldest first, paged and sorted according to {@code pageable}.
     *
     * @param jobId the job id
     * @param pageable page number, size, and sort order
     * @return {@code 200 OK} with the job's log lines as a HATEOAS page, or {@code 404 Not Found} if the job doesn't exist
     */
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagedModel<JobLogModel>>> getJobLogs(@PathVariable long jobId, Pageable pageable) {
        Page<JobLog> page = jobLogService.list(jobId, pageable);

        return ResponseEntity.ok(ApiResponse.success(pagedResourcesAssembler.toModel(page, jobLogModelAssembler)));
    }

    /**
     * Opens a live stream of a job's log lines: replays every line recorded so far, then delivers new ones as they're recorded until the job
     * finishes.
     *
     * @param jobId the job id
     * @return an {@code SseEmitter} streaming {@code log} events (each carrying a {@link JobLogModel}) followed by a final {@code complete} event, or
     *     {@code 404 Not Found} if the job doesn't exist
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamJobLogs(@PathVariable long jobId) {
        return jobLogStreamRegistry.subscribe(jobId);
    }
}
