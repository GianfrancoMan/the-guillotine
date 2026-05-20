package org.gfmanca.the_guillotine.integration;

import org.gfmanca.the_guillotine.domain.entity.Quiz;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;
import org.gfmanca.the_guillotine.dto.AuthenticationRequestDto;
import org.gfmanca.the_guillotine.dto.SubmissionRequestDto;
import org.gfmanca.the_guillotine.dto.UserResponseDto;
import org.gfmanca.the_guillotine.repository.QuizRepository;
import org.gfmanca.the_guillotine.repository.SubmissionRepository;
import org.gfmanca.the_guillotine.repository.UserRepository;
import org.gfmanca.the_guillotine.service.AuthenticationService;
import org.gfmanca.the_guillotine.service.SubmissionService;
import org.gfmanca.the_guillotine.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Ensure these match the exact keys expected by your JwtService/SecurityConfig
                "security.jwt.secret=Z2ZtYW5jYS10aGUtZ3VpbGxvdGluZS1qd3Qtc2VjcmV0LWtleS0yMDI2",
                "security.jwt.expiration=3600000"
        }
)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SubmissionConcurrencyIntegrationTest {

    private static final String PASSWORD = "Password123!";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    private Quiz quiz;

    @BeforeEach
    void setup() {
        submissionRepository.deleteAll();
        userRepository.deleteAll();
        quizRepository.deleteAll();

        quiz = new Quiz();
        quiz.setName("Concurrency Quiz");
        quiz.setStatus(QuizStatus.OPEN);
        quiz = quizRepository.save(quiz);
    }

    @Test
    void shouldHandleConcurrentSubmissionsCorrectly() throws Exception {
        int totalUsers = 100;
        List<AuthenticatedUser> users = new ArrayList<>();

        for (int i = 0; i < totalUsers; i++) {
            users.add(createAuthenticatedUser("user_" + i));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (AuthenticatedUser user : users) {
            futures.add(executorService.submit(() -> {
                latch.await();
                submitAnswer(user.token(), "rome");
                return null;
            }));
        }

        latch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        long submissionCount = submissionRepository.count();
        assertThat(submissionCount).isEqualTo(totalUsers);
    }

    @Test
    void shouldAllowOnlyOneSubmissionPerUserUnderConcurrency() throws Exception {
        @SuppressWarnings("unused")
        AuthenticatedUser user = createAuthenticatedUser("duplicate_test_user");

        int concurrentRequests = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < concurrentRequests; i++) {
            futures.add(executorService.submit(() -> {
                latch.await();
                try {
                    submitAnswer(user.token(), "rome");
                } catch (Exception ignored) {
                    // Expected for duplicate concurrent submissions.
                }
                return null;
            }));
        }

        latch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        long submissionCount = submissionRepository.count();
        assertThat(submissionCount).isEqualTo(1);
    }

    @Test
    void shouldReturnEarliestCorrectSubmissionUnderConcurrency() throws Exception {
        List<AuthenticatedUser> users = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            users.add(createAuthenticatedUser("winner_user_" + i));
        }

        submitAnswer(users.get(0).token(), "milan");
        submitAnswer(users.get(1).token(), "paris");

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        List<AuthenticatedUser> correctUsers = users.subList(2, 10);

        for (AuthenticatedUser user : correctUsers) {
            futures.add(executorService.submit(() -> {
                latch.await();
                submitAnswer(user.token(), "rome");
                return null;
            }));
        }

        latch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        quiz.setCorrectAnswer("rome");
        quizRepository.saveAndFlush(quiz);

        var winner = submissionService.findWinner(quiz.getId());
        var expectedWinnerSubmission =
                submissionRepository.findFirstByQuizIdAndAnswerOrderBySubmittedAtAscIdAsc(quiz.getId(), "rome")
                        .orElseThrow();

        assertThat(winner.winnerUserId()).isEqualTo(expectedWinnerSubmission.getUser().getId());
    }

    private AuthenticatedUser createAuthenticatedUser(String username) {
        UserResponseDto user = userService.createUser(username, PASSWORD);
        String token = authenticationService
                .authenticate(new AuthenticationRequestDto(username, PASSWORD))
                .token();
        return new AuthenticatedUser(user.id(), user.username(), token);
    }

    private void submitAnswer(String token, String answer) throws Exception {
        SubmissionRequestDto request = new SubmissionRequestDto(quiz.getId(), answer);

        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))); // Changed to objectMapper
    }

    private record AuthenticatedUser(Long id, String username, String token) {
    }
}
