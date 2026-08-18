package com.ecc.job.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Timestamp helper shared by anything in this codebase that needs to persist the current instant.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
public final class Instants {

    private Instants() {}

    /**
     * Returns the current instant, truncated to whole seconds. Sub-second precision isn't meaningful for job timestamps, and truncating up front
     * avoids relying on any particular database's fractional-second precision — every timestamp column supports at least second-level precision, so
     * the in-memory value can never silently diverge from what's persisted (which would otherwise break exact-instant equality filters — a client
     * filtering by a timestamp it was just given back).
     *
     * @return the current instant, truncated to whole seconds
     */
    public static Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
