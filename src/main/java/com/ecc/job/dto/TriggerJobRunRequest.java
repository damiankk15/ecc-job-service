package com.ecc.job.dto;

import com.ecc.job.model.JobType;
import java.util.List;

/**
 * Request body posted to companies-service's {@code POST /internal/jobs/{jobId}/run} endpoint to start a job.
 *
 * @param jobType the kind of work to run
 * @param scope the companies/tickers (or other identifiers) to act on
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record TriggerJobRunRequest(JobType jobType, List<String> scope) {}
