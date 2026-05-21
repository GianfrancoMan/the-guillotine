package org.gfmanca.the_guillotine.integration;

import org.gfmanca.the_guillotine.dto.AuthenticationRequestDto;
import org.gfmanca.the_guillotine.dto.CreateUserRequestDto;
import org.gfmanca.the_guillotine.dto.SubmissionRequestDto;
import org.gfmanca.the_guillotine.rate_limit.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    private RateLimitService rateLimitService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        CreateUserRequestDto request = new CreateUserRequestDto("rate_limit_user", "password123");
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)));
        rateLimitService.clearBuckets();
    }

    @Test
    void shouldRateLimitLoginRequests() throws Exception {

        AuthenticationRequestDto request = new AuthenticationRequestDto("rate_limit_user", "password123");

        /* First 5 requests allowed */
        for (int i = 0; i < 5; i++) {
            mockMvc.perform( post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isOk());
        }

        /* 6th request rejected */
        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldRateLimitSubmissionRequests() throws Exception {
        /* Authenticate user */
        AuthenticationRequestDto authRequest = new AuthenticationRequestDto( "rate_limit_user", "password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(authRequest))
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =  objectMapper.readTree(loginResponse).get("token").asText();

        /* Create 4 quizzes */
        for (long quizId = 1; quizId <= 4; quizId++) {
            SubmissionRequestDto submission = new SubmissionRequestDto(quizId, "rome");

            if (quizId <= 3) {
                mockMvc.perform(post("/api/submissions")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(submission))
                        ) ;
            } else {
                mockMvc.perform(post("/api/submissions")
                                        .header("Authorization", "Bearer " + token)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(submission))
                        )
                        .andExpect(status().isTooManyRequests());
            }
        }
    }

    @Test
    void shouldIsolateBucketsPerUser() throws Exception {
        /* Create second user */
        CreateUserRequestDto secondUser = new CreateUserRequestDto("second_user", "password123" );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUser))
        );
        /* Authenticate first user */
        String firstToken =  authenticateAndGetToken("rate_limit_user", "password123" );
        /* Exhaust first user's limit */
        SubmissionRequestDto submission = new SubmissionRequestDto( 999L, "rome" );

        for (int i = 0; i < 3; i++)
            mockMvc.perform(post("/api/submissions")
                            .header("Authorization", "Bearer " + firstToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submission)));

        /* Fourth request -> 429 */
        mockMvc.perform(post("/api/submissions")
                                .header("Authorization", "Bearer " + firstToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isTooManyRequests());

        /* Authenticate second user */
        String secondToken =  authenticateAndGetToken("second_user", "password123");

        /* Second user MUST NOT be rate limited */
        mockMvc.perform(post("/api/submissions")
                                .header( "Authorization", "Bearer " + secondToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRefillTokensOverTime() throws Exception {
        String token = authenticateAndGetToken("rate_limit_user", "password123");
        SubmissionRequestDto submission = new SubmissionRequestDto(999L, "rome");
        /* Consume all tokens */
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/submissions")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(submission)));
        }
        /* Bucket exhausted */
        mockMvc.perform(post("/api/submissions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isTooManyRequests());

        /* Wait for refill */
        Thread.sleep(1500);
        /* Request should be allowed again */
        mockMvc.perform(post("/api/submissions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isNotFound());
    }

    //Authentication helper
    private String authenticateAndGetToken(String username, String password) throws Exception {
        AuthenticationRequestDto request =  new AuthenticationRequestDto(username, password);

        String response = mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request))
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper
                .readTree(response)
                .get("token")
                .asText();
    }
}
