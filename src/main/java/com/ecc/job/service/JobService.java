package com.ecc.job.service;

import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.exception.InvalidJobStateException;
import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Creates, queries, and mutates {@link Job}s.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobService {

    /**
     * Lists jobs, optionally filtered by any combination of the given parameters, paged and
     * sorted according to {@code pageable}.
     *
     * @param id exact job id to match
     * @param jobType exact job type to match
     * @param scope a single scope value the job's scope list must contain
     * @param startedAt exact start instant to match
     * @param finishedAt exact finish instant to match
     * @param status exact job status to match
     * @param triggeredBy user who triggered the job, matched case-insensitively
     * @param pageable page number, size, and sort order
     * @return the matching jobs
     */
    Page<Job> list(
        Long id,
        JobType jobType,
        String scope,
        Instant startedAt,
        Instant finishedAt,
        JobStatus status,
        String triggeredBy,
        Pageable pageable
    );

    /**
     * Fetches a single job by id.
     *
     * @param id the job id
     * @return the job
     * @throws ResourceNotFoundException if no job with that id exists
     */
    Job get(long id);

    /**
     * Creates and immediately starts a new job.
     *
     * @param request the job type and scope
     * @return the created, {@link JobStatus#RUNNING} job
     */
    Job create(CreateJobRequest request);

    /**
     * Cancels a running job.
     *
     * @param id the job id
     * @return the cancelled job
     * @throws ResourceNotFoundException if no job with that id exists
     * @throws InvalidJobStateException if the job isn't currently {@link JobStatus#RUNNING}
     */
    Job cancel(long id);

    /**
     * Deletes a job.
     *
     * @param id the job id
     * @throws ResourceNotFoundException if no job with that id exists
     */
    void delete(long id);
}
