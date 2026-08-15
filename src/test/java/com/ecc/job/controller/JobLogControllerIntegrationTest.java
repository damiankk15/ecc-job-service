package com.ecc.job.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.model.JobType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class JobLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getJobLogs_afterJobCompletes_returnsChronologicalEntries() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-logs"));

        pollForTerminalStatus(id);

        mockMvc
            .perform(get("/api/jobs/{jobId}/logs", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data._embedded.items.length()").value(3))
            .andExpect(jsonPath("$.data._embedded.items[0].message").value("Job started"))
            .andExpect(jsonPath("$.data._embedded.items[1].message").value("Fetching data..."))
            .andExpect(jsonPath("$.data._embedded.items[2].message").value("Job completed"))
            .andExpect(jsonPath("$.data._embedded.items[0].level").value("INFO"));
    }

    @Test
    void getJobLogs_forNonExistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/logs", 999_999)).andExpect(status().isNotFound());
    }

    @Test
    void streamJobLogs_forCompletedJob_replaysBacklogThenCompletes() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-stream"));

        pollForTerminalStatus(id);

        MvcResult result = mockMvc.perform(get("/api/jobs/{jobId}/logs/stream", id)).andExpect(request().asyncStarted()).andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
            .andExpect(content().string(containsString("event:log")))
            .andExpect(content().string(containsString("Job completed")))
            .andExpect(content().string(containsString("event:complete")));
    }

    @Test
    void streamJobLogs_forNonExistentJob_returns404() throws Exception {
        mockMvc.perform(get("/api/jobs/{jobId}/logs/stream", 999_999)).andExpect(status().isNotFound());
    }

    private void pollForTerminalStatus(long id) throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            String jobStatus = fetchStatus(id);

            if (!jobStatus.equals("QUEUED") && !jobStatus.equals("RUNNING")) {
                return;
            }

            Thread.sleep(50);
        }

        throw new AssertionError("Job " + id + " did not reach a terminal status in time");
    }

    private String fetchStatus(long id) throws Exception {
        String response = mockMvc.perform(get("/api/jobs/{id}", id)).andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("jobStatus").asString();
    }

    private long createJob(JobType jobType, List<String> scope) throws Exception {
        CreateJobRequest request = new CreateJobRequest(jobType, scope);

        String response = mockMvc
            .perform(post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
