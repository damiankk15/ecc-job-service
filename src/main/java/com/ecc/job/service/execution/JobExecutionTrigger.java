package com.ecc.job.service.execution;

import com.ecc.job.model.Job;
import com.ecc.job.service.execution.handler.AbstractRemoteJobTypeExecutionHandler;

/**
 * Starts the actual work behind a {@link Job} once it's been dispatched to {@link com.ecc.job.model.JobStatus#RUNNING}. Doesn't return a result
 * directly, since the work happens out of band — completion is always reported later via a {@link JobCompletedEvent}, either published by the
 * implementation itself once its work finishes, or by an inbound HTTP callback from whatever performed the work, as with
 * {@link AbstractRemoteJobTypeExecutionHandler}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobExecutionTrigger {
    /**
     * Starts the work for {@code job}. Must not block the calling thread for the duration of that work — implementations run (or hand off to) it
     * asynchronously.
     *
     * @param job the job to run, already marked {@link com.ecc.job.model.JobStatus#RUNNING}
     */
    void trigger(Job job);
}
