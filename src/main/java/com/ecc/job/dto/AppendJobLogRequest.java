package com.ecc.job.dto;

import com.ecc.job.model.JobLog;
import com.ecc.job.model.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for recording a new {@link JobLog} line, posted by companies-service (or, until it exists, whatever stands in for it) as it executes
 * a job.
 *
 * @param level the line's severity
 * @param message the line's text; must not be blank
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record AppendJobLogRequest(@NotNull LogLevel level, @NotBlank String message) {}
