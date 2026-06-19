package com.ecc.job.model;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

/**
 * HATEOAS representation model for {@link Job}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public class JobModel extends RepresentationModel<JobModel>
{
    private final long id;
    private final JobType jobType;
    private final List<String> scope;
    private final String startedAt;
    private final String finishedAt;
    private final JobStatus status;
    private final String triggeredBy;

    public JobModel( Job aJob )
    {
        this.id = aJob.id();
        this.jobType = aJob.jobType();
        this.scope = aJob.scope();
        this.startedAt = aJob.startedAt();
        this.finishedAt = aJob.finishedAt();
        this.status = aJob.status();
        this.triggeredBy = aJob.triggeredBy();
    }

    public long getId() { return id; }
    public JobType getJobType() { return jobType; }
    public List<String> getScope() { return scope; }
    public String getStartedAt() { return startedAt; }
    public String getFinishedAt() { return finishedAt; }
    public JobStatus getStatus() { return status; }
    public String getTriggeredBy() { return triggeredBy; }
}