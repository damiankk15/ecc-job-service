package com.ecc.job.service.event;

import com.ecc.job.service.JobDispatchService;
import com.ecc.job.service.log.JobLogStreamRegistry;

/**
 * Published whenever a job reaches an end state — completed, failed, cancelled, or deleted — so {@link JobLogStreamRegistry} knows to close any open
 * log streams for it. Deliberately separate from {@link JobCompletedEvent}: that event drives dispatch-completion handling in
 * {@link JobDispatchService}, which shouldn't re-run for a cancellation or deletion.
 *
 * @param jobId the id of the job that reached an end state
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record JobTerminatedEvent(Long jobId) {}
