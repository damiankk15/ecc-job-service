package com.ecc.job.controller;

import com.ecc.job.assembler.JobModelAssembler;
import com.ecc.job.dto.ApiResponse;
import com.ecc.job.exception.ResourceNotFoundException;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobModel;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController
{
    private static final List<Job> JOBS = List.of(
            new Job(
                    1,
                    JobType.COMPANY_LIST_UPDATE,
                    List.of( "GPW", "NewConnect" ),
                    "2025-07-16T12:00:00.000Z",
                    "2025-07-16T12:00:00.000Z",
                    JobStatus.SUCCEEDED,
                    "Damian Kuras"
            ),
            new Job(
                    2,
                    JobType.COMPANY_DETAILS_UPDATE,
                    List.of( "CDPROJECT", "AMAZON" ),
                    "2025-07-16T00:00:00.000Z",
                    "2025-07-16T00:00:00.000Z",
                    JobStatus.FAILED,
                    "System"
            ),
            new Job(
                    3,
                    JobType.COMPANY_DETAILS_UPDATE,
                    List.of( "9" ),
                    "2025-07-16T00:00:00.000Z",
                    "2025-07-16T00:00:00.000Z",
                    JobStatus.CANCELLED,
                    "System"
            )
    );

    private final JobModelAssembler jobModelAssembler;
    private final PagedResourcesAssembler<Job> pagedResourcesAssembler;

    public JobController( JobModelAssembler jobModelAssembler,
                          PagedResourcesAssembler<Job> pagedResourcesAssembler )
    {
        this.jobModelAssembler = jobModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @GetMapping( produces = MediaTypes.HAL_JSON_VALUE )
    public ResponseEntity<ApiResponse<PagedModel<JobModel>>> getJobs( Pageable pageable )
    {
        List<Job> pageContent = JOBS.stream()
                .skip( pageable.getOffset() )
                .limit( pageable.getPageSize() )
                .toList();

        Page<Job> page = new PageImpl<>( pageContent, pageable, JOBS.size() );

        return ResponseEntity.ok( ApiResponse.success( pagedResourcesAssembler.toModel( page, jobModelAssembler ) ) );
    }

    @GetMapping( value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE )
    public ResponseEntity<ApiResponse<JobModel>> getJob( @PathVariable long id )
    {
        return JOBS.stream()
                .filter( job -> job.id() == id )
                .findFirst()
                .map( jobModelAssembler::toModel )
                .map( ApiResponse::success )
                .map( ResponseEntity::ok )
                .orElseThrow( () -> new ResourceNotFoundException( "Job with id " + id + " not found" ) );
    }
}
