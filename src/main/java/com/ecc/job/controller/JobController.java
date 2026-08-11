package com.ecc.job.controller;

import com.ecc.job.assembler.JobModelAssembler;
import com.ecc.job.dto.ApiResponse;
import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobModel;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import com.ecc.job.service.JobService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing CRUD and lifecycle operations for {@link Job}s, returned as HAL+JSON wrapped in {@link ApiResponse}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobModelAssembler jobModelAssembler;
    private final PagedResourcesAssembler<Job> pagedResourcesAssembler;

    /**
     * Creates a new controller backed by the given service and HATEOAS assemblers.
     *
     * @param jobService the service used to create, query, and mutate jobs
     * @param jobModelAssembler converts a {@link Job} to a {@link JobModel} with links
     * @param pagedResourcesAssembler converts a {@link Page} of jobs to a HATEOAS {@link PagedModel}
     */
    public JobController(JobService jobService, JobModelAssembler jobModelAssembler, PagedResourcesAssembler<Job> pagedResourcesAssembler) {
        this.jobService = jobService;
        this.jobModelAssembler = jobModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    /**
     * Creates and immediately starts a new job.
     *
     * @param request the job type and scope
     * @return {@code 201 Created} with a {@code Location} header and the created job
     */
    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<JobModel>> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobModel jobModel = jobModelAssembler.toModel(jobService.create(request));

        return ResponseEntity.created(URI.create(jobModel.getRequiredLink("self").getHref())).body(ApiResponse.success(jobModel));
    }

    /**
     * Lists jobs, optionally filtered by any combination of the given parameters, paged and sorted according to {@code pageable}.
     *
     * @param id exact job id to match
     * @param jobType exact job type to match
     * @param scope a single scope value the job's scope list must contain
     * @param startedAt ISO-8601 instant the job must have started at exactly
     * @param finishedAt ISO-8601 instant the job must have finished at exactly
     * @param status exact job status to match
     * @param triggeredBy user who triggered the job (case-insensitive)
     * @param pageable page number, size, and sort order
     * @return {@code 200 OK} with the matching jobs as a HATEOAS page
     */
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<PagedModel<JobModel>>> getJobs(
        @RequestParam(required = false) Long id,
        @RequestParam(required = false) JobType jobType,
        @RequestParam(required = false) String scope,
        @RequestParam(required = false) Instant startedAt,
        @RequestParam(required = false) Instant finishedAt,
        @RequestParam(required = false) JobStatus status,
        @RequestParam(required = false) String triggeredBy,
        Pageable pageable
    ) {
        Page<Job> page = jobService.list(id, jobType, scope, startedAt, finishedAt, status, triggeredBy, pageable);

        return ResponseEntity.ok(ApiResponse.success(pagedResourcesAssembler.toModel(page, jobModelAssembler)));
    }

    /**
     * Fetches a single job by id.
     *
     * @param id the job id
     * @return {@code 200 OK} with the job, or {@code 404} if it doesn't exist
     */
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<JobModel>> getJob(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(jobModelAssembler.toModel(jobService.get(id))));
    }

    /**
     * Cancels a running job.
     *
     * @param id the job id
     * @return {@code 200 OK} with the cancelled job, or {@code 400} if the job isn't currently {@link JobStatus#RUNNING}
     */
    @PostMapping(value = "/{id}/cancel", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<ApiResponse<JobModel>> cancelJob(@PathVariable long id) {
        return ResponseEntity.ok(ApiResponse.success(jobModelAssembler.toModel(jobService.cancel(id))));
    }

    /**
     * Deletes a job.
     *
     * @param id the job id
     * @return {@code 204 No Content}, or {@code 404} if the job doesn't exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable long id) {
        jobService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
