package com.ecc.job.service;

import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.exception.InvalidJobStateException;
import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import com.ecc.job.repository.JobRepository;
import com.ecc.job.repository.JobSpecifications;
import com.ecc.job.service.event.JobTerminatedEvent;
import com.ecc.job.util.Instants;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
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

    private static final String MOCK_TRIGGERED_BY = "mock-user";

    private final JobRepository jobRepository;
    private final JobDispatchService jobDispatchService;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new service backed by the given repository and dispatcher.
     *
     * @param jobRepository the repository used to persist and query jobs
     * @param jobDispatchService decides whether a job runs immediately or queues, and starts its execution
     * @param events used to publish {@link JobTerminatedEvent} when a job is cancelled or deleted
     */
    public JobServiceImpl(JobRepository jobRepository, JobDispatchService jobDispatchService, ApplicationEventPublisher events) {
        this.jobRepository = jobRepository;
        this.jobDispatchService = jobDispatchService;
        this.events = events;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Job> list(
        Long id,
        JobType jobType,
        String scope,
        Instant createdAt,
        Instant startedAt,
        Instant finishedAt,
        JobStatus jobStatus,
        String triggeredBy,
        Pageable pageable
    ) {
        return jobRepository.findAll(
            JobSpecifications.filter(id, jobType, scope, createdAt, startedAt, finishedAt, jobStatus, triggeredBy),
            pageable
        );
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
        Job job = new Job(null, request.jobType(), request.scope(), Instants.now(), null, null, JobStatus.QUEUED, MOCK_TRIGGERED_BY);
        Job saved = jobRepository.save(job);
        jobDispatchService.tryDispatch(saved);

        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Job cancel(long id) {
        Job job = get(id);
        JobStatus previousStatus = job.getJobStatus();

        if (previousStatus != JobStatus.RUNNING && previousStatus != JobStatus.QUEUED) {
            throw new InvalidJobStateException("Job " + id + " cannot be cancelled — current status: " + previousStatus);
        }

        job.setJobStatus(JobStatus.CANCELLED);
        job.setFinishedAt(Instants.now());
        Job cancelled = jobRepository.save(job);
        events.publishEvent(new JobTerminatedEvent(cancelled.getId()));

        if (previousStatus == JobStatus.QUEUED) {
            jobDispatchService.dispatchQueuedJobs(cancelled.getJobType());
        }

        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(long id) {
        Job job = get(id);
        jobRepository.delete(job);
        events.publishEvent(new JobTerminatedEvent(job.getId()));
    }
}
