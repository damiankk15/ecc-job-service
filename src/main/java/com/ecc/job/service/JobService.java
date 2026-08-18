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
     * Lists jobs, optionally filtered by any combination of the given parameters, paged and sorted according to {@code pageable}.
     *
     * @param id exact job id to match
     * @param jobType exact job type to match
     * @param scope a single scope value the job's scope list must contain
     * @param createdAt exact creation instant to match
     * @param startedAt exact start instant to match
     * @param finishedAt exact finish instant to match
     * @param jobStatus exact job status to match
     * @param triggeredBy exact triggering user to match
     * @param pageable page number, size, and sort order
     * @return the matching jobs
     */
    Page<Job> list(
        Long id,
        JobType jobType,
        String scope,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        JobStatus jobStatus,
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
     * Creates a new job and dispatches it to {@link JobStatus#RUNNING} immediately, unless another job of the same type with an overlapping scope is
     * already active — in that case it's created {@link JobStatus#QUEUED} and dispatched automatically once that conflict clears.
     *
     * @param request the job type and scope
     * @return the created job, either {@link JobStatus#RUNNING} or {@link JobStatus#QUEUED}
     */
    Job create(CreateJobRequest request);

    /**
     * Cancels a running or queued job.
     *
     * @param id the job id
     * @return the cancelled job
     * @throws ResourceNotFoundException if no job with that id exists
     * @throws InvalidJobStateException if the job isn't currently {@link JobStatus#RUNNING} or {@link JobStatus#QUEUED}
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
