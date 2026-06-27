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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
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
            ),
            new Job(
                    4,
                    JobType.COMPANY_LIST_UPDATE,
                    List.of( "GPW" ),
                    "2025-07-17T08:00:00.000Z",
                    null,
                    JobStatus.RUNNING,
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
    public ResponseEntity<ApiResponse<PagedModel<JobModel>>> getJobs(
            @RequestParam( required = false ) Long id,
            @RequestParam( required = false ) JobType jobType,
            @RequestParam( required = false ) String scope,
            @RequestParam( required = false ) String startedAt,
            @RequestParam( required = false ) String finishedAt,
            @RequestParam( required = false ) JobStatus status,
            @RequestParam( required = false ) String triggeredBy,
            Pageable pageable )
    {
        List<Job> filtered = JOBS.stream()
                .filter( job -> id == null || job.id() == id )
                .filter( job -> jobType == null || job.jobType() == jobType )
                .filter( job -> scope == null || job.scope().contains( scope ) )
                .filter( job -> startedAt == null || job.startedAt().equals( startedAt ) )
                .filter( job -> finishedAt == null || job.finishedAt().equals( finishedAt ) )
                .filter( job -> status == null || job.status() == status )
                .filter( job -> triggeredBy == null || job.triggeredBy().equalsIgnoreCase( triggeredBy ) )
                .sorted( toComparator( pageable.getSort() ) )
                .toList();

        List<Job> pageContent = filtered.stream()
                .skip( pageable.getOffset() )
                .limit( pageable.getPageSize() )
                .toList();

        Page<Job> page = new PageImpl<>( pageContent, pageable, filtered.size() );

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

    @PostMapping( value = "/{id}/cancel", produces = MediaTypes.HAL_JSON_VALUE )
    public ResponseEntity<ApiResponse<JobModel>> cancelJob( @PathVariable long id )
    {
        Job job = JOBS.stream()
                .filter( j -> j.id() == id )
                .findFirst()
                .orElseThrow( () -> new ResourceNotFoundException( "Job with id " + id + " not found" ) );

        if ( job.status() != JobStatus.RUNNING )
        {
            return ResponseEntity.badRequest()
                    .body( ApiResponse.failure( "INVALID_STATE",
                            "Job " + id + " cannot be cancelled — current status: " + job.status() ) );
        }

        Job cancelled = new Job( job.id(), job.jobType(), job.scope(),
                job.startedAt(), job.finishedAt(), JobStatus.CANCELLED, job.triggeredBy() );

        return ResponseEntity.ok( ApiResponse.success( jobModelAssembler.toModel( cancelled ) ) );
    }

    @DeleteMapping( "/{id}" )
    public ResponseEntity<Void> deleteJob( @PathVariable long id )
    {
        JOBS.stream()
                .filter( j -> j.id() == id )
                .findFirst()
                .orElseThrow( () -> new ResourceNotFoundException( "Job with id " + id + " not found" ) );

        return ResponseEntity.noContent().build();
    }

    private Comparator<Job> toComparator( Sort sort )
    {
        return sort.stream()
                .map( order -> {
                    Comparator<Job> c = switch ( order.getProperty() )
                    {
                        case "jobType"     -> Comparator.comparing( job -> job.jobType().name() );
                        case "status"      -> Comparator.comparing( job -> job.status().name() );
                        case "startedAt"   -> Comparator.comparing( Job::startedAt );
                        case "finishedAt"  -> Comparator.comparing( Job::finishedAt );
                        case "triggeredBy" -> Comparator.comparing( Job::triggeredBy );
                        default            -> Comparator.comparingLong( Job::id );
                    };
                    return order.isAscending() ? c : c.reversed();
                } )
                .reduce( Comparator::thenComparing )
                .orElse( Comparator.comparingLong( Job::id ) );
    }
}
