package dev.amritanka.assistant.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(
        UUID conversationId,
        @NotBlank @Size(max = 8000) String message,
        boolean useRag
) {}
