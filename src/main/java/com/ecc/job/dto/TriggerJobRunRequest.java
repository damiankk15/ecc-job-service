package com.ecc.job.dto;

import java.util.List;

/**
 * Request body posted to a downstream service's job-type-specific endpoint to start a job.
 *
 * @param jobId the id of the job to run
 * @param scope the companies/tickers (or other identifiers) to act on
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record TriggerJobRunRequest(Long jobId, List<String> scope) {}
