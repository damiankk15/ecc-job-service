package com.ecc.job.repository;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobStatus;
import com.ecc.job.model.JobType;
import jakarta.persistence.criteria.Join;
import java.time.Instant;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds the {@link Specification} used by {@link JobRepository} to filter jobs by any combination of the parameters accepted by
 * {@code JobService.list(...)}. A {@code null} parameter means "don't filter on this field" — the corresponding condition is omitted rather than
 * matching {@code null} values.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public final class JobSpecifications {

    private JobSpecifications() {}

    /**
     * Combines a condition per non-null parameter with {@code AND} into a single {@link Specification}.
     *
     * @param id exact job id to match
     * @param jobType exact job type to match
     * @param scope a single scope value the job's scope list must contain
     * @param startedAt exact start instant to match
     * @param finishedAt exact finish instant to match
     * @param status exact job status to match
     * @param triggeredBy user who triggered the job, matched case-insensitively
     * @return a specification restricted to the non-null parameters
     */
    public static Specification<Job> filter(
        Long id,
        JobType jobType,
        String scope,
        Instant startedAt,
        Instant finishedAt,
        JobStatus status,
        String triggeredBy
    ) {
        return Specification.where(hasId(id))
            .and(hasJobType(jobType))
            .and(hasScope(scope))
            .and(hasStartedAt(startedAt))
            .and(hasFinishedAt(finishedAt))
            .and(hasStatus(status))
            .and(hasTriggeredBy(triggeredBy));
    }

    /**
     * Matches an exact {@code id}, or no restriction if {@code id} is {@code null}.
     *
     * @param id the id to match
     * @return the resulting specification
     */
    private static Specification<Job> hasId(Long id) {
        return equal("id", id);
    }

    /**
     * Matches an exact {@code jobType}, or no restriction if {@code jobType} is {@code null}.
     *
     * @param jobType the job type to match
     * @return the resulting specification
     */
    private static Specification<Job> hasJobType(JobType jobType) {
        return equal("jobType", jobType);
    }

    /**
     * Matches an exact {@code startedAt}, or no restriction if {@code startedAt} is {@code null}.
     *
     * @param startedAt the start instant to match
     * @return the resulting specification
     */
    private static Specification<Job> hasStartedAt(Instant startedAt) {
        return equal("startedAt", startedAt);
    }

    /**
     * Matches an exact {@code finishedAt}, or no restriction if {@code finishedAt} is {@code null}.
     *
     * @param finishedAt the finish instant to match
     * @return the resulting specification
     */
    private static Specification<Job> hasFinishedAt(Instant finishedAt) {
        return equal("finishedAt", finishedAt);
    }

    /**
     * Matches an exact {@code status}, or no restriction if {@code status} is {@code null}.
     *
     * @param status the job status to match
     * @return the resulting specification
     */
    private static Specification<Job> hasStatus(JobStatus status) {
        return equal("status", status);
    }

    /**
     * Builds a simple {@code attribute = value} condition, or no restriction if {@code value} is {@code null}. Shared by the single-field equality
     * checks above.
     *
     * @param attribute the {@link Job} entity attribute name
     * @param value the value it must equal, or {@code null} for no restriction
     * @return the resulting specification
     */
    private static <V> Specification<Job> equal(String attribute, V value) {
        return (root, query, cb) -> value == null ? null : cb.equal(root.get(attribute), value);
    }

    /**
     * Matches jobs whose {@code scope} collection contains {@code scope}, or no restriction if {@code scope} is {@code null}. Joins the
     * {@code job_scope} collection table and marks the query distinct so a job can't appear more than once if its scope list has duplicate values.
     *
     * @param scope the scope value to look for
     * @return the resulting specification
     */
    private static Specification<Job> hasScope(String scope) {
        return (root, query, cb) -> {
            if (scope == null) {
                return null;
            }

            query.distinct(true);
            Join<Job, String> scopeJoin = root.join("scope");

            return cb.equal(scopeJoin, scope);
        };
    }

    /**
     * Matches an exact {@code triggeredBy}, case-insensitively, or no restriction if {@code triggeredBy} is {@code null}.
     *
     * @param triggeredBy the triggering user to match
     * @return the resulting specification
     */
    private static Specification<Job> hasTriggeredBy(String triggeredBy) {
        return (root, query, cb) -> triggeredBy == null ? null : cb.equal(cb.lower(root.get("triggeredBy")), triggeredBy.toLowerCase());
    }
}
