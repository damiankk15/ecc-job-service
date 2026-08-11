package com.ecc.job.repository;

import com.ecc.job.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data repository for {@link Job}s. {@link JpaSpecificationExecutor} enables the dynamic, multi-field filtering built by
 * {@link JobSpecifications} and used in {@code JobService.list(...)}; everything else (save, findById, delete, ...) comes from {@link JpaRepository}.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {}
