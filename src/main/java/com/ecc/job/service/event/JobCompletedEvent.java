package com.ecc.job.service.event;

import com.ecc.job.service.JobDispatchService;
import com.ecc.job.service.JobExecutionTrigger;

/**
 * Published when a {@link JobExecutionTrigger} finishes running a job, so {@link JobDispatchService} can record the result without either class
 * depending on the other.
 *
 * @param jobId the id of the job that finished
 * @param success whether the job succeeded
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record JobCompletedEvent(Long jobId, boolean success) {}
