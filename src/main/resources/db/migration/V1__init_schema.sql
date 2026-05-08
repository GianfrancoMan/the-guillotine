CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quizzes (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      correct_answer VARCHAR(100),
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE submissions (
                            id BIGSERIAL PRIMARY KEY,
                            quiz_id BIGINT NOT NULL,
                            user_id BIGINT NOT NULL,
                            answer VARCHAR(100) NOT NULL,
                            submitted_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_submission_quiz
                                FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
                            CONSTRAINT fk_submission_user
                                FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT uq_user_quiz
                                UNIQUE (quiz_id, user_id)
);

CREATE INDEX idx_submission_quiz_time
    ON submissions (quiz_id, submitted_at);

CREATE INDEX idx_submission_quiz_answer_time
    ON submissions (quiz_id, answer, submitted_at);




