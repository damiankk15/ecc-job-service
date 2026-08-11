package com.ecc.job.service;

import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.exception.InvalidJobStateException;
import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import com.ecc.job.repository.JobRepository;
import com.ecc.job.repository.JobSpecifications;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default {@link JobService} implementation, backed by {@link JobRepository}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class JobServiceImpl implements JobService {

    // Placeholder until triggeredBy is derived from the authenticated user (Keycloak JWT `sub` claim).
    private static final String MOCK_TRIGGERED_BY = "mock-user";

    private final JobRepository jobRepository;

    /**
     * Creates a new service backed by the given repository.
     *
     * @param jobRepository the repository used to persist and query jobs
     */
    public JobServiceImpl(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Job> list(
        Long id,
        JobType jobType,
        String scope,
        Instant startedAt,
        Instant finishedAt,
        JobStatus status,
        String triggeredBy,
        Pageable pageable
    ) {
        return jobRepository.findAll(JobSpecifications.filter(id, jobType, scope, startedAt, finishedAt, status, triggeredBy), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job get(long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job with id " + id + " not found"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job create(CreateJobRequest request) {
        Job job = new Job(null, request.jobType(), request.scope(), now(), null, JobStatus.RUNNING, MOCK_TRIGGERED_BY);
        return jobRepository.save(job);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job cancel(long id) {
        Job job = get(id);

        if (job.getStatus() != JobStatus.RUNNING) {
            throw new InvalidJobStateException("Job " + id + " cannot be cancelled — current status: " + job.getStatus());
        }

        job.setStatus(JobStatus.CANCELLED);
        job.setFinishedAt(now());
        return jobRepository.save(job);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(long id) {
        jobRepository.delete(get(id));
    }

    // Truncated to microseconds since that's the precision the DB actually stores; keeping the
    // in-memory value at full nanosecond precision would silently diverge from what's persisted,
    // breaking exact-instant equality filters (a client filtering by a startedAt it was just given).
    private Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }
}
