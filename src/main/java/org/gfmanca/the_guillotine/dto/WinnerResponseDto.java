package org.gfmanca.the_guillotine.dto;

import java.time.LocalDateTime;

public record WinnerResponseDto(

        Long quizId,
        String correctAnswer,

        Long winnerUserId,
        String winnerUsername,

        Long submissionId,
        LocalDateTime submittedAt

) {}
