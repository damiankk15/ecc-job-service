package com.ecc.job.model;

/**
 * The severity of a {@link JobLog} entry.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum LogLevel {
    /** A routine progress update. */
    INFO,
    /** Something unexpected happened but the job can continue. */
    WARN,
    /** Something failed. */
    ERROR,
}
