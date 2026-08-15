package com.ecc.job.model;

import java.time.Instant;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * HATEOAS representation model for {@link JobLog}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Relation(collectionRelation = "items", itemRelation = "item")
public class JobLogModel extends RepresentationModel<JobLogModel> {

    private final long id;
    private final Instant createdAt;
    private final LogLevel level;
    private final String message;

    /**
     * Copies the fields of {@code jobLog} into a new, link-less model.
     *
     * @param jobLog the log line to represent
     */
    public JobLogModel(JobLog jobLog) {
        this.id = jobLog.getId();
        this.createdAt = jobLog.getCreatedAt();
        this.level = jobLog.getLevel();
        this.message = jobLog.getMessage();
    }

    /**
     * Returns the log entry's id.
     *
     * @return the log entry id
     */
    public long getId() {
        return id;
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
