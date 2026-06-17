package com.ecc.job.model;

import java.util.List;

public record Job(
        long id,
        JobType jobType,
        List<String> scope,
        String startedAt,
        String finishedAt,
        JobStatus status,
        String triggeredBy
) {}
