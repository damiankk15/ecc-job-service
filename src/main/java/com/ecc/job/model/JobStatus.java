package com.ecc.job.model;

/**
 * The lifecycle state of a {@link Job}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum JobStatus {
    /** The job is waiting for a conflicting job (same type, overlapping scope) to finish. */
    QUEUED,
    /** The job is currently executing. */
    RUNNING,
    /** The job finished successfully. */
    SUCCEEDED,
    /** The job finished with an error. */
    FAILED,
    /** The job was cancelled before it finished. */
    CANCELLED,
}
