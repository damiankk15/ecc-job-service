package com.ecc.job.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * A single log line recorded during a {@link Job}'s execution.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "job_log")
public class JobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private LogLevel level;

    @Column(length = 2000, nullable = false)
    private String message;

    /**
     * No-arg constructor required by Hibernate; not for application use.
     */
    protected JobLog() {}

    /**
     * Creates a new log line.
     *
     * @param id the log entry id, or {@code null} for one not yet persisted
     * @param jobId the id of the {@link Job} this line was recorded for
     * @param createdAt when this line was recorded
     * @param level the line's severity
     * @param message the line's text
     */
    public JobLog(Long id, Long jobId, Instant createdAt, LogLevel level, String message) {
        this.id = id;
        this.jobId = jobId;
        this.createdAt = createdAt;
        this.level = level;
        this.message = message;
    }

    /**
     * Returns the log entry's id.
     *
     * @return the log entry id, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the id of the job this line was recorded for.
     *
     * @return the job id
     */
    public Long getJobId() {
        return jobId;
    }

    /**
     * Returns when this line was recorded.
     *
     * @return the creation instant
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns this line's severity.
     *
     * @return the log level
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Returns this line's text.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
