package com.ecc.job.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

/**
 * A persisted background job.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_scope", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "scope_value")
    private List<String> scope;

    private Instant startedAt;

    private Instant finishedAt;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private String triggeredBy;

    /**
     * No-arg constructor required by Hibernate; not for application use.
     */
    protected Job() {}

    /**
     * Creates a new job.
     *
     * @param id the job id, or {@code null} for a job not yet persisted
     * @param jobType the kind of work the job performs
     * @param scope the companies/tickers (or other identifiers) the job acts on
     * @param startedAt when the job started running
     * @param finishedAt when the job reached a terminal state, or {@code null} if still running
     * @param status the job's current lifecycle state
     * @param triggeredBy who or what triggered the job
     */
    public Job(Long id, JobType jobType, List<String> scope, Instant startedAt, Instant finishedAt, JobStatus status, String triggeredBy) {
        this.id = id;
        this.jobType = jobType;
        this.scope = scope;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.triggeredBy = triggeredBy;
    }

    /**
     * Returns the job's id.
     *
     * @return the job id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the kind of work the job performs.
     *
     * @return the job type
     */
    public JobType getJobType() {
        return jobType;
    }

    /**
     * Returns what the job acts on.
     *
     * @return the markets/companies the job targets
     */
    public List<String> getScope() {
        return scope;
    }

    /**
     * Returns when the job started running.
     *
     * @return the start instant
     */
    public Instant getStartedAt() {
        return startedAt;
    }

    /**
     * Returns when the job reached a terminal state.
     *
     * @return the finish instant, or {@code null} if the job is still {@link JobStatus#RUNNING}
     */
    public Instant getFinishedAt() {
        return finishedAt;
    }

    /**
     * Sets when the job reached a terminal state.
     *
     * @param finishedAt the finish instant
     */
    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    /**
     * Returns the job's current lifecycle state.
     *
     * @return the job status
     */
    public JobStatus getStatus() {
        return status;
    }

    /**
     * Sets the job's current lifecycle state.
     *
     * @param status the new status
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }

    /**
     * Returns who or what triggered the job.
     *
     * @return the triggering user
     */
    public String getTriggeredBy() {
        return triggeredBy;
    }
}
