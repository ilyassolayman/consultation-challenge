package com.medexpress.consultation;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ConsultationControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // --- GET /consultations/{consultationId} ---

    @Test
    void getConsultation_returnsConsultationAfterSubmission() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "customerId": "user-999",
                  "answers": [
                    { "questionId": "q1", "value": false },
                    { "questionId": "q2", "value": false },
                    { "questionId": "q3", "value": false }
                  ]
                }
                """;

        String responseBody = mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String consultationId = JsonPath.read(responseBody, "$.consultationId");

        mockMvc.perform(get("/api/v1/consultations/" + consultationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consultationId").value(consultationId))
                .andExpect(jsonPath("$.productId").value("pear-allergy-med"));
    }

    @Test
    void getConsultation_returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/consultations/unknown-id"))
                .andExpect(status().isNotFound());
    }

    // --- GET /products/{productId}/questions ---

    @Test
    void getQuestions_returnsOkWithThreeQuestions() throws Exception {
        mockMvc.perform(get("/api/v1/products/pear-allergy-med/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value("q1"));
    }

    @Test
    void getQuestions_returnsNotFoundForUnknownProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/unknown-product/questions"))
                .andExpect(status().isNotFound());
    }

    // --- POST /consultations: happy paths ---

    @Test
    void submitConsultation_withNoContraindications_returnsEligibleTrue() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "q1", "value": false },
                    { "questionId": "q2", "value": false },
                    { "questionId": "q3", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.productId").value("pear-allergy-med"))
                .andExpect(jsonPath("$.consultationId").isNotEmpty())
                .andExpect(jsonPath("$.clinicalNotes").doesNotExist());
    }

    @Test
    void submitConsultation_withContraindication_returnsEligibleFalse() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "customerId": "user-456",
                  "answers": [
                    { "questionId": "q1", "value": true },
                    { "questionId": "q2", "value": false },
                    { "questionId": "q3", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.clinicalNotes[0]").isNotEmpty());
    }

    // --- POST /consultations: Bean Validation ---

    @Test
    void submitConsultation_withMissingProductId_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "q1", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitConsultation_withBlankProductId_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "productId": "",
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "q1", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitConsultation_withMissingCustomerId_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "answers": [
                    { "questionId": "q1", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitConsultation_withEmptyAnswersList_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "customerId": "user-123",
                  "answers": []
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitConsultation_withBlankAnswerQuestionId_returnsBadRequest() throws Exception {
        String payload = """
                {
                  "productId": "pear-allergy-med",
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    // --- POST /consultations: validation error response body ---

    @Test
    void submitConsultation_withMissingProductId_returnsValidationFailedMessage() throws Exception {
        String payload = """
                {
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "q1", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // --- POST /consultations: unknown product ---

    @Test
    void submitConsultation_withUnknownProductId_returnsNotFound() throws Exception {
        String payload = """
                {
                  "productId": "unknown-product",
                  "customerId": "user-123",
                  "answers": [
                    { "questionId": "q1", "value": false }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: unknown-product"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
