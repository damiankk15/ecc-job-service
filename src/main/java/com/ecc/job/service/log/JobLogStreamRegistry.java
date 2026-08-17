package com.ecc.job.service.log;

import com.ecc.job.model.Job;
import com.ecc.job.model.JobLog;
import com.ecc.job.model.JobLogModel;
import com.ecc.job.model.JobStatus;
import com.ecc.job.repository.JobLogRepository;
import com.ecc.job.service.JobService;
import com.ecc.job.service.JobTerminatedEvent;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

/**
 * Tracks clients streaming a job's logs live and pushes new lines to them as they're recorded. JVM-local only — like {@code JobDispatchService}'s
 * dispatch lock, this doesn't survive across instances, which is fine for the current single-instance deployment.
 *
 * @author Damian Kuras
 * @version 1.0
 * @since 0.0.1-SNAPSHOT
 */
@Component
public class JobLogStreamRegistry {

    private static final Logger log = LoggerFactory.getLogger(JobLogStreamRegistry.class);
    private static final String LOG_EVENT = "log";
    private static final String COMPLETE_EVENT = "complete";

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emittersByJobId = new ConcurrentHashMap<>();

    private final JobService jobService;
    private final JobLogRepository jobLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new registry backed by the given repository and job service.
     *
     * @param jobService used to confirm a job exists, and to check whether it's already finished, when a client subscribes
     * @param jobLogRepository used to replay a job's log backlog when a client subscribes
     * @param objectMapper used to serialize each line to JSON before sending; {@link JobLogModel} extends {@code RepresentationModel}, which
     *     Spring's default message converters only know how to write as {@code hal+json}, not as a plain SSE data payload
     */
    public JobLogStreamRegistry(JobService jobService, JobLogRepository jobLogRepository, ObjectMapper objectMapper) {
        this.jobService = jobService;
        this.jobLogRepository = jobLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Opens a live log stream for {@code jobId}: replays every existing log line, then either closes immediately (if the job has already reached a
     * terminal status) or stays open and receives new lines as they're recorded. The emitter is registered only after the backlog is sent, so a line
     * recorded concurrently is never missed — at the cost of, rarely, being delivered twice.
     *
     * @param jobId the job to stream logs for
     * @return an emitter the caller returns directly from a controller method
     * @throws com.ecc.job.exception.ResourceNotFoundException if no job with that id exists
     */
    public SseEmitter subscribe(long jobId) {
        Job job = jobService.get(jobId);
        SseEmitter emitter = new SseEmitter();

        for (JobLog jobLog : jobLogRepository.findByJobIdOrderByIdAsc(jobId)) {
            send(emitter, jobLog);
        }

        if (isTerminal(job.getJobStatus())) {
            complete(emitter);
        } else {
            register(jobId, emitter);
        }

        return emitter;
    }

    /**
     * Pushes a newly-recorded log line to every client currently streaming its job's logs.
     *
     * @param event the event published when a line is recorded
     */
    @EventListener
    public void onJobLogCreated(JobLogCreatedEvent event) {
        JobLog jobLog = event.jobLog();

        for (SseEmitter emitter : emittersByJobId.getOrDefault(jobLog.getJobId(), new CopyOnWriteArrayList<>())) {
            send(emitter, jobLog);
        }
    }

    /**
     * Closes every open log stream for a job that just reached an end state.
     *
     * @param event the event published when a job terminates
     */
    @EventListener
    public void onJobTerminated(JobTerminatedEvent event) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByJobId.remove(event.jobId());

        if (emitters == null) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            complete(emitter);
        }
    }

    /**
     * Registers {@code emitter} to receive live log lines for {@code jobId}, and arranges for it to be dropped from the registry once it completes,
     * times out, or errors out — whichever happens first.
     *
     * @param jobId the job the emitter is streaming logs for
     * @param emitter the emitter to register
     */
    private void register(long jobId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByJobId.computeIfAbsent(jobId, key -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(ex -> emitters.remove(emitter));
    }

    /**
     * Returns whether a job status is a terminal one, i.e. one no further lines will be recorded for.
     *
     * @param status the status to check
     * @return {@code true} unless the job is still {@link JobStatus#RUNNING} or {@link JobStatus#QUEUED}
     */
    private boolean isTerminal(JobStatus status) {
        return status != JobStatus.RUNNING && status != JobStatus.QUEUED;
    }

    /**
     * Serializes {@code jobLog} and sends it to {@code emitter} as a {@value #LOG_EVENT} event. If the send fails — most likely because the client
     * has already disconnected — the failure is logged and swallowed rather than propagated, since one dead client shouldn't affect any other.
     *
     * @param emitter the emitter to send to
     * @param jobLog the log line to send
     */
    private void send(SseEmitter emitter, JobLog jobLog) {
        try {
            String json = objectMapper.writeValueAsString(new JobLogModel(jobLog));
            emitter.send(SseEmitter.event().name(LOG_EVENT).data(json, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            log.debug("Dropping log stream client that failed to receive a line: {}", e.getMessage());
        }
    }

    /**
     * Sends {@code emitter} a final {@value #COMPLETE_EVENT} event and closes it. Used both when a client subscribes to an already-finished job
     * (closing immediately after the backlog replay) and when a still-open stream's job reaches a terminal status.
     *
     * @param emitter the emitter to complete
     */
    private void complete(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name(COMPLETE_EVENT));
        } catch (IOException | IllegalStateException e) {
            log.debug("Log stream client already gone before completion could be sent: {}", e.getMessage());
        }

        emitter.complete();
    }
}
