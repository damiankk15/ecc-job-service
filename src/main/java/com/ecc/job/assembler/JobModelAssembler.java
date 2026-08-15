package com.ecc.job.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecc.job.controller.JobController;
import com.ecc.job.controller.JobLogController;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobModel;
import com.ecc.job.model.JobStatus;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * Assembler that converts {@link Job} to {@link JobModel} with HATEOAS links.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class JobModelAssembler extends RepresentationModelAssemblerSupport<Job, JobModel> {

    /**
     * Creates a new assembler that builds links relative to {@link JobController}.
     */
    public JobModelAssembler() {
        super(JobController.class, JobModel.class);
    }

    /**
     * Wraps {@code job} in a {@link JobModel} without any HATEOAS links.
     *
     * @param job the job to wrap
     * @return a new, link-less {@link JobModel}
     */
    @Override
    protected JobModel instantiateModel(Job job) {
        return new JobModel(job);
    }

    /**
     * Converts {@code job} to a {@link JobModel}, adding a self link, a link to its logs, and, if the job is still {@link JobStatus#RUNNING} or
     * {@link JobStatus#QUEUED}, a link to cancel it.
     *
     * @param job the job to convert
     * @return the resulting {@link JobModel} with its HATEOAS links attached
     */
    @Override
    public JobModel toModel(Job job) {
        JobModel model = instantiateModel(job);

        model.add(linkTo(methodOn(JobController.class).getJob(job.getId())).withSelfRel());
        model.add(linkTo(methodOn(JobLogController.class).getJobLogs(job.getId(), null)).withRel("logs"));

        if (job.getJobStatus() == JobStatus.RUNNING || job.getJobStatus() == JobStatus.QUEUED) {
            model.add(linkTo(methodOn(JobController.class).cancelJob(job.getId())).withRel("cancel"));
        }

        return model;
    }
}
