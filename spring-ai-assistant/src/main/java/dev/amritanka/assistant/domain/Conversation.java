package dev.amritanka.assistant.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Conversation {

    private UUID id;
    private String userId;
    private String tenantId;
    private final List<ChatTurn> messages = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public static Conversation create(String userId, String tenantId) {
        Conversation c = new Conversation();
        c.id = UUID.randomUUID();
        c.userId = userId;
        c.tenantId = tenantId;
        return c;
    }

    public void appendUserMessage(String content) {
        messages.add(new ChatTurn(ChatTurn.Role.USER, content));
        updatedAt = Instant.now();
    }

    public void appendAssistantMessage(String content) {
        messages.add(new ChatTurn(ChatTurn.Role.ASSISTANT, content));
        updatedAt = Instant.now();
    }

    public int totalUserChars() {
        return messages.stream()
                .filter(m -> m.role() == ChatTurn.Role.USER)
                .mapToInt(m -> m.content().length())
                .sum();
    }
}
