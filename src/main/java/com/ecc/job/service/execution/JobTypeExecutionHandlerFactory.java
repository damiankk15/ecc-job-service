package com.ecc.job.service.execution;

import com.ecc.job.model.JobType;
import com.ecc.job.service.execution.handler.JobTypeExecutionHandler;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Resolves the {@link JobTypeExecutionHandler} that handles a given {@link JobType}, built once at startup from every registered handler bean. Adding
 * support for a new job type — even one handled by an entirely different downstream service or protocol — is just adding a new
 * {@link JobTypeExecutionHandler} bean; this class never needs to change.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
@Profile("!test")
public class JobTypeExecutionHandlerFactory {

    private final Map<JobType, JobTypeExecutionHandler> handlersByType;

    /**
     * Creates a new factory backed by every {@link JobTypeExecutionHandler} bean Spring finds.
     *
     * @param handlers every registered job-type handler
     * @throws IllegalStateException if any {@link JobType} has no handler, or more than one handler claims the same type — checked here, rather than
     *     left to fail at the first job of that type, so a misconfiguration breaks application startup instead of a job silently never running
     */
    public JobTypeExecutionHandlerFactory(List<JobTypeExecutionHandler> handlers) {
        Map<JobType, JobTypeExecutionHandler> byType = new EnumMap<>(JobType.class);

        for (JobTypeExecutionHandler handler : handlers) {
            JobTypeExecutionHandler existing = byType.put(handler.supports(), handler);

            if (existing != null) {
                throw new IllegalStateException(
                    "Multiple JobTypeExecutionHandler beans support " +
                        handler.supports() +
                        ": " +
                        existing.getClass().getSimpleName() +
                        " and " +
                        handler.getClass().getSimpleName()
                );
            }
        }

        for (JobType jobType : JobType.values()) {
            if (!byType.containsKey(jobType)) {
                throw new IllegalStateException("No JobTypeExecutionHandler bean supports job type " + jobType);
            }
        }

        this.handlersByType = byType;
    }

    /**
     * Returns the handler for the given job type.
     *
     * @param jobType the job type to resolve a handler for
     * @return the handler that {@link JobTypeExecutionHandler#supports() supports} {@code jobType}
     */
    public JobTypeExecutionHandler forType(JobType jobType) {
        return handlersByType.get(jobType);
    }
}
