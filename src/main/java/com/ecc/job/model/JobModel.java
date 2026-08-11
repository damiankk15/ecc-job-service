package com.ecc.job.model;

import java.time.Instant;
import java.util.List;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * HATEOAS representation model for {@link Job}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Relation(collectionRelation = "items", itemRelation = "item")
public class JobModel extends RepresentationModel<JobModel> {

    private final long id;
    private final JobType jobType;
    private final List<String> scope;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final JobStatus status;
    private final String triggeredBy;

    /**
     * Copies the fields of {@code job} into a new, link-less model.
     *
     * @param job the job to represent
     */
    public JobModel(Job job) {
        this.id = job.getId();
        this.jobType = job.getJobType();
        this.scope = job.getScope();
        this.startedAt = job.getStartedAt();
        this.finishedAt = job.getFinishedAt();
        this.status = job.getStatus();
        this.triggeredBy = job.getTriggeredBy();
    }

    /**
     * Returns the job's id.
     *
     * @return the job id
     */
    public long getId() {
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
     * Returns the job's current lifecycle state.
     *
     * @return the job status
     */
    public JobStatus getStatus() {
        return status;
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
