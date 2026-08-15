package com.ecc.job.repository;

import com.ecc.job.model.JobLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for {@link JobLog}s.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    /**
     * Fetches a page of log lines for the given job.
     *
     * @param jobId the job id to match
     * @param pageable page number, size, and sort order
     * @return the matching page of log lines
     */
    Page<JobLog> findByJobId(Long jobId, Pageable pageable);

    /**
     * Fetches every log line for the given job, oldest first. Used to replay the full backlog when a client subscribes to the live log stream.
     *
     * @param jobId the job id to match
     * @return the matching log lines ordered by {@code id} ascending
     */
    List<JobLog> findByJobIdOrderByIdAsc(Long jobId);
}
