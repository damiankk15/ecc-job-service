package com.ecc.job.repository;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data repository for {@link Job}s. {@link JpaSpecificationExecutor} enables the dynamic, multi-field filtering built by
 * {@link JobSpecifications}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
    /**
     * Finds jobs of the given type in any of the given statuses, oldest first. Used by {@code JobDispatchService} to check for scope-overlap
     * conflicts and to re-scan the queue. Ordered by {@code id} rather than {@code createdAt} since the id is a reliable, collision-free
     * insertion-order signal — {@code createdAt} is truncated to whole seconds and can be identical for jobs created moments apart.
     *
     * @param jobType the job type to match
     * @param jobStatuses the job statuses to match
     * @return the matching jobs ordered by {@code id} ascending
     */
    List<Job> findByJobTypeAndJobStatusInOrderByIdAsc(JobType jobType, List<JobStatus> jobStatuses);
}
