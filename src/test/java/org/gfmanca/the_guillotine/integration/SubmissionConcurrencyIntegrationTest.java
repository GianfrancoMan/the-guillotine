package org.gfmanca.the_guillotine.integration;

import org.gfmanca.the_guillotine.domain.entity.Quiz;
import org.gfmanca.the_guillotine.domain.entity.User;
import org.gfmanca.the_guillotine.domain.enums.QuizStatus;
import org.gfmanca.the_guillotine.repository.QuizRepository;
import org.gfmanca.the_guillotine.repository.SubmissionRepository;
import org.gfmanca.the_guillotine.repository.UserRepository;
import org.gfmanca.the_guillotine.service.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SubmissionConcurrencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);

        registry.add("spring.datasource.username", postgres::getUsername);

        registry.add("spring.datasource.password", postgres::getPassword);
    }

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
        List<User> users = new ArrayList<>();

        for (int i = 0; i < totalUsers; i++) {
            User user = new User();
            user.setUsername("user_" + i);
            users.add(userRepository.save(user));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        CountDownLatch latch = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (User user : users) {
            futures.add(executorService.submit(() -> {
                        latch.await();
                        submissionService.submitAnswer(quiz.getId(), user.getId(), "rome");
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
        User user = new User();
        user.setUsername("duplicate_test_user");
        user = userRepository.save(user);

        int concurrentRequests = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentRequests; i++) {
            User finalUser = user;
            futures.add(executorService.submit(() -> {
                        latch.await();
                        try {
                            submissionService.submitAnswer(quiz.getId(), finalUser.getId(), "rome");
                        } catch (Exception ignored) { }

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
        List<User> users = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            User user = new User();
            user.setUsername("winner_user_" + i);
            users.add(userRepository.save(user));
        }

        /* First send some incorrect answers sequentially*/
        submissionService.submitAnswer(quiz.getId(), users.get(0).getId(), "milan");
        submissionService.submitAnswer(quiz.getId(), users.get(1).getId(), "paris");

        /* Concurrent correct answers*/
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        List<User> correctUsers = users.subList(2, 10);

        for (User user : correctUsers) {
            futures.add(executorService.submit(() -> {
                        latch.await();
                        submissionService.submitAnswer(quiz.getId(), user.getId(), "rome");
                        return null;
            }));
        }

        latch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }

        executorService.shutdown();

        /* Set correct answer */
        quiz.setCorrectAnswer("rome");
        quizRepository.saveAndFlush(quiz);

        /* Resolve winner*/

        var winner = submissionService.findWinner(quiz.getId());

        /* Verify winner is truly the earliest correct stored submission*/

        var expectedWinnerSubmission =
                submissionRepository.findFirstByQuizIdAndAnswerOrderBySubmittedAtAscIdAsc(quiz.getId(), "rome").orElseThrow();

        assertThat(winner.winnerUserId()).isEqualTo(expectedWinnerSubmission.getUser().getId());
    }
}