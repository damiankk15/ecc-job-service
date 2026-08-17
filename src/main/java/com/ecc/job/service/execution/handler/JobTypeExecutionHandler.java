package com.ecc.job.service.execution.handler;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobType;
import com.ecc.job.service.execution.DelegatingJobExecutionTrigger;

/**
 * Starts the actual work behind jobs of one particular {@link JobType}. {@link DelegatingJobExecutionTrigger} collects every bean implementing this
 * interface and dispatches each job to whichever one declares it {@link #supports()}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobTypeExecutionHandler {
    /**
     * Returns the job type this handler starts work for.
     *
     * @return the supported job type
     */
    JobType supports();

    /**
     * Starts the work for {@code job}. Must not block the calling thread for the duration of that work — implementations run (or hand off to) it
     * asynchronously.
     *
     * @param job the job to run, already marked {@link com.ecc.job.model.JobStatus#RUNNING}; {@code job.getJobType()} always equals
     *     {@link #supports()}
     */
    void trigger(Job job);
}
