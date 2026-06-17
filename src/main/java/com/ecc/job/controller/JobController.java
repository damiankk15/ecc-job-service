package com.ecc.job.controller;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class JobController
{
    private static final List<Job> JOBS = List.of(
            new Job(
                    1,
                    JobType.COMPANY_LIST_UPDATE,
                    List.of("GPW", "NewConnect"),
                    "2025-07-16T12:00:00.000Z",
                    "2025-07-16T12:00:00.000Z",
                    JobStatus.SUCCEEDED,
                    "Damian Kuras"
            ),
            new Job(
                    2,
                    JobType.COMPANY_DETAILS_UPDATE,
                    List.of("CDPROJECT", "AMAZON"),
                    "2025-07-16T00:00:00.000Z",
                    "2025-07-16T00:00:00.000Z",
                    JobStatus.FAILED,
                    "System"
            ),
            new Job(
                    3,
                    JobType.COMPANY_DETAILS_UPDATE,
                    List.of("9"),
                    "2025-07-16T00:00:00.000Z",
                    "2025-07-16T00:00:00.000Z",
                    JobStatus.CANCELLED,
                    "System"
            )
    );

    @GetMapping
    public List<Job> getJobs()
    {
        return JOBS;
    }

    @GetMapping("/{id}")
    public Optional<Job> getJob( @PathVariable long id )
    {
        return JOBS.stream().filter( job -> job.id() == id ).findFirst();
    }
}

