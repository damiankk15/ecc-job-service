package com.ecc.job.service;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import com.ecc.job.repository.JobRepository;
import com.ecc.job.service.execution.JobCompletedEvent;
import com.ecc.job.service.execution.JobExecutionTrigger;
import com.ecc.job.util.Instants;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Decides whether a job can run immediately or must queue behind a conflicting one, and drives it from {@link JobStatus#QUEUED} through
 * {@link JobStatus#RUNNING} to a terminal state. Two jobs of the same {@link JobType} whose {@code scope} lists overlap never run concurrently — the
 * later one queues until the earlier one finishes or is cancelled.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class JobDispatchService {

    private static final Logger log = LoggerFactory.getLogger(JobDispatchService.class);

    // Guards the check-then-dispatch sequence in tryDispatch/dispatchQueuedJobs so two concurrently-created overlapping jobs can't both see "not
    // blocked" at once. Single-JVM only — a multi-instance deployment would need DB-level locking instead.
    private final Object dispatchLock = new Object();

    private final JobRepository jobRepository;
    private final JobExecutionTrigger jobExecutionTrigger;
    private final ApplicationEventPublisher events;

    /**
     * Creates a new dispatch service.
     *
     * @param jobRepository the repository used to query and persist jobs
     * @param jobExecutionTrigger starts the actual work behind a dispatched job
     * @param events used to publish {@link JobTerminatedEvent} when a job reaches a terminal status
     */
    public JobDispatchService(JobRepository jobRepository, JobExecutionTrigger jobExecutionTrigger, ApplicationEventPublisher events) {
        this.jobRepository = jobRepository;
        this.jobExecutionTrigger = jobExecutionTrigger;
        this.events = events;
    }

    /**
     * Dispatches {@code job} to {@link JobStatus#RUNNING} if nothing of the same {@link JobType} with an overlapping scope is already active;
     * otherwise leaves it {@link JobStatus#QUEUED}.
     *
     * @param job a newly-created, already-persisted, {@link JobStatus#QUEUED} job
     */
    public void tryDispatch(Job job) {
        synchronized (dispatchLock) {
            if (!isBlocked(job)) {
                dispatch(job);
            }
        }
    }

    /**
     * Re-checks every {@link JobStatus#QUEUED} job of the given type, oldest first, and dispatches any that are no longer blocked. Called after a job
     * of that type finishes or a queued one is cancelled, since either can free up a conflicting job behind it.
     *
     * @param jobType the job type to re-scan
     */
    public void dispatchQueuedJobs(JobType jobType) {
        synchronized (dispatchLock) {
            List<Job> queued = jobRepository.findByJobTypeAndJobStatusInOrderByIdAsc(jobType, List.of(JobStatus.QUEUED));

            for (Job job : queued) {
                if (!isBlocked(job)) {
                    dispatch(job);
                }
            }
        }
    }

    /**
     * Records the outcome of a job's execution and re-scans the queue for its job type. If the job was deleted while its execution was still in
     * flight, this logs a warning and returns without failing — there's nothing left to update, and a deleted job was never counted as a blocker for
     * anything else in the first place.
     *
     * @param event the completion event published by a {@link JobExecutionTrigger}
     */
    @EventListener
    public void onJobCompleted(JobCompletedEvent event) {
        Optional<Job> maybeJob = jobRepository.findById(event.jobId());

        if (maybeJob.isEmpty()) {
            log.warn("Job {} completed but no longer exists (likely deleted mid-execution)", event.jobId());

            return;
        }

        Job job = maybeJob.get();
        job.setJobStatus(event.success() ? JobStatus.SUCCEEDED : JobStatus.FAILED);
        job.setFinishedAt(Instants.now());
        jobRepository.save(job);
        events.publishEvent(new JobTerminatedEvent(job.getId()));

        dispatchQueuedJobs(job.getJobType());
    }

    /**
     * Checks whether an earlier job of the same type and overlapping scope is still active. Only jobs with a smaller id than {@code candidate}
     * count — {@code id} is a reliable, collision-free insertion-order signal (unlike {@code createdAt}, which is truncated to whole seconds and can
     * be identical for jobs created moments apart).
     *
     * @param candidate the job to check
     * @return {@code true} if an earlier, active, scope-overlapping job of the same type exists
     */
    private boolean isBlocked(Job candidate) {
        List<Job> active = jobRepository.findByJobTypeAndJobStatusInOrderByIdAsc(
            candidate.getJobType(),
            List.of(JobStatus.RUNNING, JobStatus.QUEUED)
        );

        return active
            .stream()
            .filter(other -> other.getId() < candidate.getId())
            .anyMatch(other -> !Collections.disjoint(other.getScope(), candidate.getScope()));
    }

    /**
     * Marks {@code job} as {@link JobStatus#RUNNING}, persists it, and starts its execution. Callers are responsible for confirming it's not
     * {@link #isBlocked(Job) blocked} first.
     *
     * @param job the job to dispatch
     */
    private void dispatch(Job job) {
        job.setJobStatus(JobStatus.RUNNING);
        job.setStartedAt(Instants.now());
        jobRepository.save(job);

        jobExecutionTrigger.trigger(job);
    }
}
