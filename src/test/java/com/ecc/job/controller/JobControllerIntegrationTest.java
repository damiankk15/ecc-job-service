package com.ecc.job.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createJob_persistsAndReturnsRunningJob() throws Exception {
        CreateJobRequest request = new CreateJobRequest(JobType.COMPANY_LIST_UPDATE, List.of("GPW"));

        mockMvc.perform(
            post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.status").value("RUNNING"))
            .andExpect(jsonPath("$.data.jobType").value("COMPANY_LIST_UPDATE"));
    }

    @Test
    void createJob_withoutScope_returnsValidationError() throws Exception {
        CreateJobRequest request = new CreateJobRequest(JobType.COMPANY_LIST_UPDATE, List.of());

        mockMvc.perform(
            post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.errors[0].field").value("scope"));
    }

    @Test
    void getJob_returnsCreatedJob() throws Exception {
        int id = (int) createJob(JobType.COMPANY_DETAILS_UPDATE, List.of("AMAZON"));

        mockMvc.perform(get("/api/jobs/{id}", id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));
    }

    @Test
    void getJobs_filtersById() throws Exception {
        int id = (int) createJob(JobType.COMPANY_LIST_UPDATE, List.of("GPW"));

        mockMvc.perform(get("/api/jobs").param("id", String.valueOf(id)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data._embedded.items.length()").value(1))
            .andExpect(jsonPath("$.data._embedded.items[0].id").value(id));
    }

    @Test
    void getJobs_filtersByStartedAt() throws Exception {
        String response = mockMvc.perform(
            post("/api/jobs").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateJobRequest(JobType.COMPANY_LIST_UPDATE, List.of("GPW"))))
        )
            .andReturn()
            .getResponse()
            .getContentAsString();

        var created = objectMapper.readTree(response).path("data");
        int id = created.path("id").asInt();
        String startedAt = created.path("startedAt").asString();

        mockMvc.perform(get("/api/jobs").param("startedAt", startedAt))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data._embedded.items.length()").value(1))
            .andExpect(jsonPath("$.data._embedded.items[0].id").value(id));
    }

    @Test
    void getJobs_withMalformedStartedAt_returnsInvalidRequest() throws Exception {
        mockMvc.perform(get("/api/jobs").param("startedAt", "not-a-date"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("INVALID_REQUEST"));
    }

    @Test
    void cancelJob_onNonRunningJob_returnsInvalidState() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("NewConnect"));

        mockMvc.perform(post("/api/jobs/{id}/cancel", id)).andExpect(status().isOk());

        mockMvc.perform(post("/api/jobs/{id}/cancel", id))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].code").value("INVALID_STATE"));
    }

    @Test
    void deleteJob_removesJob() throws Exception {
        long id = createJob(JobType.COMPANY_LIST_UPDATE, List.of("CDPROJEKT"));

        mockMvc.perform(delete("/api/jobs/{id}", id)).andExpect(status().isNoContent());

        mockMvc.perform(get("/api/jobs/{id}", id)).andExpect(status().isNotFound());
    }

    private long createJob(JobType jobType, List<String> scope) throws Exception {
        CreateJobRequest request = new CreateJobRequest(jobType, scope);

        String response = mockMvc.perform(
            post("/api/jobs").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
        )
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong();
    }
}
