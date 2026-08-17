package com.ecc.job.service.execution.handler;

import com.ecc.job.model.JobType;
import com.ecc.job.service.execution.JobCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Starts {@link JobType#COMPANY_LIST_UPDATE} jobs by calling companies-service.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Service
@Profile("!test")
public class CompanyListUpdateExecutionHandler extends AbstractRemoteJobTypeExecutionHandler {

    /**
     * Creates a new handler that calls companies-service at the given base URL and path.
     *
     * @param baseUrl companies-service's base URL
     * @param path companies-service's endpoint for starting a company-list-update job
     * @param events used to publish a failed {@link JobCompletedEvent} if the handoff call itself fails
     */
    public CompanyListUpdateExecutionHandler(
        @Value("${company.service.base-url}") String baseUrl,
        @Value("${company.service.company-list-update-path}") String path,
        ApplicationEventPublisher events
    ) {
        super(baseUrl, path, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobType supports() {
        return JobType.COMPANY_LIST_UPDATE;
    }
}
