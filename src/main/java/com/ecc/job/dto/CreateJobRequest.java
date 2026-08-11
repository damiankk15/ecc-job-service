package com.ecc.job.dto;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request body for creating a new {@link Job}.
 *
 * @param jobType the type of job to run
 * @param scope the companies/tickers (or other identifiers) the job should act on; must not be empty
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record CreateJobRequest(@NotNull JobType jobType, @NotEmpty List<String> scope) {}
