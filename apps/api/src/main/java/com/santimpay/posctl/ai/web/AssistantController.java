package com.santimpay.posctl.ai.web;

import com.santimpay.posctl.ai.application.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI assistant endpoint (RAG). Grounded, cited answers over indexed operational knowledge. Returns
 * citations so the user can verify; {@code grounded=false} means the assistant declined to answer
 * (no/low-confidence context) rather than guessing.
 */
@Tag(name = "AI")
@RestController
@RequestMapping("/api/v1/ai/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final RagService rag;

    public record AskRequest(@NotBlank @Size(max = 2000) String question) {}

    public record Citation(UUID id, String sourceType, UUID sourceId, double similarity) {}

    public record AskResponse(String answer, boolean grounded, List<Citation> citations) {}

    @Operation(summary = "Ask the RAG assistant a grounded, cited question")
    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest req) {
        var a = rag.ask(req.question());
        var citations = a.citations().stream()
                .map(c -> new Citation(c.id(), c.sourceType(), c.sourceId(),
                        Math.round(c.similarity() * 1000.0) / 1000.0))
                .toList();
        return new AskResponse(a.text(), a.grounded(), citations);
    }
}
