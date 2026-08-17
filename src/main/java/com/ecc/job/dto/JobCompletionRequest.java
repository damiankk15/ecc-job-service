package com.ecc.job.dto;

import com.ecc.job.model.Job;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for reporting that a {@link Job} finished, posted by companies-service once its execution of the job is done.
 *
 * @param success whether the job succeeded
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record JobCompletionRequest(@NotNull Boolean success) {}
