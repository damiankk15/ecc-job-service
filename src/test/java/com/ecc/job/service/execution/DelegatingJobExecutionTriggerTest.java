package com.ecc.job.service.execution;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobType;
import com.ecc.job.service.execution.handler.JobTypeExecutionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;

class DelegatingJobExecutionTriggerTest {

    @Test
    void trigger_dispatchesToTheHandlerThatSupportsTheJobsType() {
        RecordingHandler listHandler = new RecordingHandler(JobType.COMPANY_LIST_UPDATE);
        RecordingHandler detailsHandler = new RecordingHandler(JobType.COMPANY_DETAILS_UPDATE);
        JobTypeExecutionHandlerFactory handlerFactory = new JobTypeExecutionHandlerFactory(List.of(listHandler, detailsHandler));
        DelegatingJobExecutionTrigger trigger = new DelegatingJobExecutionTrigger(handlerFactory);

        Job job = new Job(1L, JobType.COMPANY_DETAILS_UPDATE, List.of("AAPL"), null, null, null, null, null);
        trigger.trigger(job);

        assertThat(detailsHandler.triggered).isTrue();
        assertThat(listHandler.triggered).isFalse();
    }

    private static final class RecordingHandler implements JobTypeExecutionHandler {

        private final JobType jobType;
        private boolean triggered;

        private RecordingHandler(JobType jobType) {
            this.jobType = jobType;
        }

        @Override
        public JobType supports() {
            return jobType;
        }

        @Override
        public void trigger(Job job) {
            triggered = true;
        }
    }
}
