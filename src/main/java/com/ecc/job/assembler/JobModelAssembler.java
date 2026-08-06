package com.ecc.job.assembler;

import com.ecc.job.controller.JobController;
import com.ecc.job.model.Job;
import com.ecc.job.model.JobModel;
import com.ecc.job.model.JobStatus;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler that converts {@link Job} to {@link JobModel} with HATEOAS links.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class JobModelAssembler extends RepresentationModelAssemblerSupport<Job, JobModel>
{
    public JobModelAssembler()
    {
        super( JobController.class, JobModel.class );
    }

    @Override
    protected JobModel instantiateModel( Job aJob )
    {
        return new JobModel( aJob );
    }

    @Override
    public JobModel toModel( Job aJob )
    {
        JobModel model = instantiateModel( aJob );

        model.add( linkTo( methodOn( JobController.class ).getJob( aJob.id() ) ).withSelfRel() );

        if ( aJob.status() == JobStatus.RUNNING )
        {
            model.add( linkTo( methodOn( JobController.class ).cancelJob( aJob.id() ) ).withRel( "cancel" ) );
        }

        return model;
    }
}
