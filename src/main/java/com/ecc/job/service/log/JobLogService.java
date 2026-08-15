package com.ecc.job.service.log;

import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.JobLog;
import com.ecc.job.model.LogLevel;
import com.ecc.job.service.event.JobLogCreatedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Records and retrieves {@link JobLog} lines for a job's execution.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobLogService {
    /**
     * Records a new log line for a job and publishes a {@link JobLogCreatedEvent} so any open log stream for it is notified. Doesn't verify the job
     * still exists — called only by execution code that already holds a reference to it, and an orphaned line for a job deleted mid-execution is
     * harmless.
     *
     * @param jobId the id of the job this line belongs to
     * @param level the line's severity
     * @param message the line's text
     * @return the persisted log line
     */
    JobLog append(long jobId, LogLevel level, String message);

    /**
     * Lists a job's log lines, oldest first, paged according to {@code pageable}.
     *
     * @param jobId the job id
     * @param pageable page number, size, and sort order
     * @return the matching page of log lines
     * @throws ResourceNotFoundException if no job with that id exists
     */
    Page<JobLog> list(long jobId, Pageable pageable);
}
