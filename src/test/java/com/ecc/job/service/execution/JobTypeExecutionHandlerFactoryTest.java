package com.ecc.job.service.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobType;
import com.ecc.job.service.execution.handler.JobTypeExecutionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobTypeExecutionHandlerFactoryTest {

    @Test
    void forType_returnsTheHandlerThatSupportsThatJobType() {
        RecordingHandler listHandler = new RecordingHandler(JobType.COMPANY_LIST_UPDATE);
        RecordingHandler detailsHandler = new RecordingHandler(JobType.COMPANY_DETAILS_UPDATE);
        JobTypeExecutionHandlerFactory factory = new JobTypeExecutionHandlerFactory(List.of(listHandler, detailsHandler));

        assertThat(factory.forType(JobType.COMPANY_LIST_UPDATE)).isSameAs(listHandler);
        assertThat(factory.forType(JobType.COMPANY_DETAILS_UPDATE)).isSameAs(detailsHandler);
    }

    @Test
    void constructor_withUnsupportedJobType_throwsIllegalState() {
        List<JobTypeExecutionHandler> handlers = List.of(new RecordingHandler(JobType.COMPANY_LIST_UPDATE));

        assertThatThrownBy(() -> new JobTypeExecutionHandlerFactory(handlers))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPANY_DETAILS_UPDATE");
    }

    @Test
    void constructor_withTwoHandlersForTheSameJobType_throwsIllegalState() {
        List<JobTypeExecutionHandler> handlers = List.of(
            new RecordingHandler(JobType.COMPANY_LIST_UPDATE),
            new RecordingHandler(JobType.COMPANY_LIST_UPDATE),
            new RecordingHandler(JobType.COMPANY_DETAILS_UPDATE)
        );

        assertThatThrownBy(() -> new JobTypeExecutionHandlerFactory(handlers))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("COMPANY_LIST_UPDATE");
    }

    private static final class RecordingHandler implements JobTypeExecutionHandler {

        private final JobType jobType;

        private RecordingHandler(JobType jobType) {
            this.jobType = jobType;
        }

        @Override
        public JobType supports() {
            return jobType;
        }

        @Override
        public void trigger(Job job) {}
    }
}
