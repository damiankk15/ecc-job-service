package com.ecc.job.service.log;

import com.ecc.job.model.JobLog;

/**
 * Published whenever a new {@link JobLog} line is recorded, so {@link JobLogStreamRegistry} can push it to any clients currently streaming that job's
 * logs.
 *
 * @param jobLog the log line that was recorded
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record JobLogCreatedEvent(JobLog jobLog) {}
