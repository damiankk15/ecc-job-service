package com.ecc.job.service.log;

import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.JobLog;
import com.ecc.job.model.LogLevel;
import com.ecc.job.repository.JobLogRepository;
import com.ecc.job.repository.JobRepository;
import com.ecc.job.service.JobService;
import com.ecc.job.service.MockJobExecutionTrigger;
import com.ecc.job.service.event.JobLogCreatedEvent;
import com.ecc.job.util.Instants;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default {@link JobLogService} implementation, backed by {@link JobLogRepository}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class JobLogServiceImpl implements JobLogService {

    private final JobRepository jobRepository;
    private final JobLogRepository jobLogRepository;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new service backed by the given repositories and event publisher. Depends on {@link JobRepository} directly, rather than
     * {@link JobService}, since {@link MockJobExecutionTrigger} (reached from {@code JobServiceImpl} via {@code JobDispatchService}) depends on
     * this service to record progress — depending back on {@code JobService} here would create a circular bean dependency.
     *
     * @param jobRepository used to confirm a job exists before listing its logs
     * @param jobLogRepository the repository used to persist and query log lines
     * @param events used to publish {@link JobLogCreatedEvent} whenever a line is recorded
     */
    public JobLogServiceImpl(JobRepository jobRepository, JobLogRepository jobLogRepository, ApplicationEventPublisher events) {
        this.jobRepository = jobRepository;
        this.jobLogRepository = jobLogRepository;
        this.events = events;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobLog append(long jobId, LogLevel level, String message) {
        JobLog saved = jobLogRepository.save(new JobLog(null, jobId, Instants.now(), level, message));
        events.publishEvent(new JobLogCreatedEvent(saved));

        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<JobLog> list(long jobId, Pageable pageable) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResourceNotFoundException("Job with id " + jobId + " not found");
        }

        return jobLogRepository.findByJobId(jobId, pageable);
    }
}
