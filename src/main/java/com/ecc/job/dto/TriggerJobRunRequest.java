package com.ecc.job.dto;

import java.util.List;

/**
 * Request body posted to a downstream service's job-type-specific endpoint to start a job. {@code jobId} travels in the body rather than the URL,
 * since each job type's endpoint path is configured independently and isn't guaranteed to include a path variable for it. Doesn't carry the job
 * type itself — the endpoint being called already identifies it.
 *
 * @param jobId the id of the job to run
 * @param scope the companies/tickers (or other identifiers) to act on
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public record TriggerJobRunRequest(Long jobId, List<String> scope) {}
