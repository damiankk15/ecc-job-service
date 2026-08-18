package com.ecc.job.dto;

import com.ecc.job.model.Job;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for reporting that a {@link Job} finished, posted by whichever downstream service was executing it.
 *
 * @param success whether the job succeeded
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record JobCompletionRequest(@NotNull Boolean success) {}
