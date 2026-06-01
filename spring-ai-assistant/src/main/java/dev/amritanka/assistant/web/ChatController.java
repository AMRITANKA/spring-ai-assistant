package dev.amritanka.assistant.web;

import dev.amritanka.assistant.service.ChatService;
import dev.amritanka.assistant.web.dto.ChatChunk;
import dev.amritanka.assistant.web.dto.ChatRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Stream chat tokens via Server-Sent Events.
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatChunk>> stream(
            @AuthenticationPrincipal Jwt principal,
            @Valid @RequestBody ChatRequest request) {

        String userId = principal != null ? principal.getSubject() : "anonymous";
        log.info("stream chat user={} convo={}", userId, request.conversationId());

        return chatService.streamReply(userId, request)
                .map(chunk -> ServerSentEvent.<ChatChunk>builder()
                        .id(chunk.id())
                        .event("token")
                        .data(chunk)
                        .build())
                .doOnError(e -> log.error("stream failed", e));
    }
}
