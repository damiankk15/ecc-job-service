package com.ecc.job.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class InternalJobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeJob_reportingFailure_transitionsJobToFailed() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-internal-complete"));

        mockMvc
            .perform(post("/internal/jobs/{jobId}/complete", id).contentType(MediaType.APPLICATION_JSON).content("{\"success\": false}"))
            .andExpect(status().isAccepted());

        mockMvc.perform(get("/api/jobs/{id}", id)).andExpect(jsonPath("$.data.jobStatus").value("FAILED"));
    }

    @Test
    void completeJob_forNonExistentJob_isAcceptedAsNoOp() throws Exception {
        mockMvc
            .perform(post("/internal/jobs/{jobId}/complete", 999_999).contentType(MediaType.APPLICATION_JSON).content("{\"success\": true}"))
            .andExpect(status().isAccepted());
    }

    @Test
    void completeJob_withMissingSuccessField_returnsValidationError() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW-internal-complete-validation"));

        mockMvc
            .perform(post("/internal/jobs/{jobId}/complete", id).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("success"));
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
