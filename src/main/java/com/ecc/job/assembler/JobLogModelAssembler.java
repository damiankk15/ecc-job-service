package com.ecc.job.assembler;

import com.ecc.job.controller.JobLogController;
import com.ecc.job.model.JobLog;
import com.ecc.job.model.JobLogModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

/**
 * Assembler that converts {@link JobLog} to {@link JobLogModel} with HATEOAS links.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class JobLogModelAssembler extends RepresentationModelAssemblerSupport<JobLog, JobLogModel> {

    /**
     * Creates a new assembler that builds links relative to {@link JobLogController}.
     */
    public JobLogModelAssembler() {
        super(JobLogController.class, JobLogModel.class);
    }

    /**
     * Wraps {@code jobLog} in a {@link JobLogModel} without any HATEOAS links.
     *
     * @param jobLog the log line to wrap
     * @return a new, link-less {@link JobLogModel}
     */
    @Override
    protected JobLogModel instantiateModel(JobLog jobLog) {
        return new JobLogModel(jobLog);
    }

    /**
     * Converts {@code jobLog} to a link-less {@link JobLogModel}.
     *
     * @param jobLog the log line to convert
     * @return the resulting {@link JobLogModel}
     */
    @Override
    public JobLogModel toModel(JobLog jobLog) {
        return instantiateModel(jobLog);
    }
}
