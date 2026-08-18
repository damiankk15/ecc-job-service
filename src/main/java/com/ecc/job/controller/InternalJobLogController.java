package com.ecc.job.controller;

import com.ecc.job.assembler.JobLogModelAssembler;
import com.ecc.job.dto.ApiResponse;
import com.ecc.job.dto.AppendJobLogRequest;
import com.ecc.job.model.JobLogModel;
import com.ecc.job.service.JobService;
import com.ecc.job.service.log.JobLogService;
import jakarta.validation.Valid;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal, service-to-service endpoint for recording job log lines.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/internal/jobs/{jobId}/logs")
public class InternalJobLogController {

    private final JobService jobService;
    private final JobLogService jobLogService;
    private final JobLogModelAssembler jobLogModelAssembler;

    /**
     * Creates a new controller backed by the given services and assembler.
     *
     * @param jobService used to confirm the job exists before recording a line for it
     * @param jobLogService used to persist the line and notify any open log stream
     * @param jobLogModelAssembler converts the persisted line to a {@link JobLogModel}
     */
    public InternalJobLogController(JobService jobService, JobLogService jobLogService, JobLogModelAssembler jobLogModelAssembler) {
        this.jobService = jobService;
        this.jobLogService = jobLogService;
        this.jobLogModelAssembler = jobLogModelAssembler;
    }

    /**
     * Records a new log line for a job.
     *
     * @param jobId the job id
     * @param request the line's severity and text
     * @return {@code 201 Created} with the recorded line, or {@code 404 Not Found} if the job doesn't exist
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<JobLogModel>> appendJobLog(@PathVariable long jobId, @Valid @RequestBody AppendJobLogRequest request) {
        jobService.get(jobId);

        JobLogModel jobLogModel = jobLogModelAssembler.toModel(jobLogService.append(jobId, request.level(), request.message()));

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(jobLogModel));
    }
}
