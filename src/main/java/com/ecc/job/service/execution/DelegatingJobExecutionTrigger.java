package com.ecc.job.service.execution;

import com.ecc.job.model.Job;
import com.ecc.job.service.execution.handler.JobTypeExecutionHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Dispatches each job to whichever {@link JobTypeExecutionHandler} the {@link JobTypeExecutionHandlerFactory} resolves for its
 * {@link com.ecc.job.model.JobType}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Service
@Profile("!test")
public class DelegatingJobExecutionTrigger implements JobExecutionTrigger {

    private final JobTypeExecutionHandlerFactory handlerFactory;

    /**
     * Creates a new trigger backed by the given handler factory.
     *
     * @param handlerFactory resolves which handler should run a given job
     */
    public DelegatingJobExecutionTrigger(JobTypeExecutionHandlerFactory handlerFactory) {
        this.handlerFactory = handlerFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trigger(Job job) {
        handlerFactory.forType(job.getJobType()).trigger(job);
    }
}
