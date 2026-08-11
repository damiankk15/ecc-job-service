package com.ecc.job.model;

/**
 * The kind of work a {@link Job} performs.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public enum JobType {
    /** Refreshes the list of known companies. */
    COMPANY_LIST_UPDATE,
    /** Refreshes the details of one or more existing companies. */
    COMPANY_DETAILS_UPDATE,
}
