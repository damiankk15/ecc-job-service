package com.ecc.job.service;

import com.ecc.job.model.Job;

/**
 * Starts the actual work behind a {@link Job} once it's been dispatched to {@link com.ecc.job.model.JobStatus#RUNNING}. Implementations report
 * completion asynchronously by publishing a {@link JobCompletedEvent} — they don't return a result directly, since the real work (eventually, a call
 * to a separate companies-service) happens out of band.
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
