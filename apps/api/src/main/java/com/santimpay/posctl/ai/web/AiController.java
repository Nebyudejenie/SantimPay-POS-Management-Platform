package com.santimpay.posctl.ai.web;

import com.santimpay.posctl.ai.application.AiScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiScoreService service;

    public record ScoreResponse(String subjectType, UUID subjectId, String scoreType,
                                BigDecimal value, String band, String modelVersion, Instant computedAt) {}

    @Operation(summary = "Latest AI scores for a subject (merchant/device)")
    @GetMapping("/scores")
    public List<ScoreResponse> scores(@RequestParam String subjectType, @RequestParam UUID id) {
        return service.latestForSubject(subjectType, id).stream()
                .map(s -> new ScoreResponse(s.getSubjectType(), s.getSubjectId(), s.getScoreType(),
                        s.getValue(), s.getBand(), s.getModelVersion(), s.getComputedAt()))
                .toList();
    }
}
