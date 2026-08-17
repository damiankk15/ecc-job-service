package com.ecc.job.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ecc.job.dto.AppendJobLogRequest;
import com.ecc.job.dto.CreateJobRequest;
import com.ecc.job.model.JobType;
import com.ecc.job.model.LogLevel;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class InternalJobLogControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void appendJobLog_forExistingJob_persistsAndReturnsLogLine() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-internal-append"));
        AppendJobLogRequest request = new AppendJobLogRequest(LogLevel.WARN, "Rate limited, retrying");

        mockMvc
            .perform(
                post("/internal/jobs/{jobId}/logs", id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.level").value("WARN"))
            .andExpect(jsonPath("$.data.message").value("Rate limited, retrying"));

        mockMvc.perform(get("/api/jobs/{jobId}/logs", id).param("size", "50")).andExpect(content().string(containsString("Rate limited, retrying")));
    }

    @Test
    void appendJobLog_forNonExistentJob_returns404() throws Exception {
        AppendJobLogRequest request = new AppendJobLogRequest(LogLevel.INFO, "irrelevant");

        mockMvc
            .perform(
                post("/internal/jobs/{jobId}/logs", 999_999).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void appendJobLog_withBlankMessage_returnsValidationError() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-internal-validation"));
        AppendJobLogRequest request = new AppendJobLogRequest(LogLevel.INFO, " ");

        mockMvc
            .perform(
                post("/internal/jobs/{jobId}/logs", id).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("message"));
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
