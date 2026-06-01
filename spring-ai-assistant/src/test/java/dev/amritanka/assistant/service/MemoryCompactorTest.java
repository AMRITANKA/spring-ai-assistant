package dev.amritanka.assistant.service;

import dev.amritanka.assistant.domain.ChatTurn;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MemoryCompactorTest {

    @Test
    void keepsRecentTurnsWhenWithinWindow() {
        MemoryCompactor compactor = new MemoryCompactor(mock(ChatClient.class), 5);

        var turns = List.of(
                new ChatTurn(ChatTurn.Role.USER, "hi"),
                new ChatTurn(ChatTurn.Role.ASSISTANT, "hello"),
                new ChatTurn(ChatTurn.Role.USER, "weather?")
        );

        var compacted = compactor.compact(turns);

        assertThat(compacted).hasSize(3);
    }

    @Test
    void emptyHistoryReturnsEmpty() {
        MemoryCompactor compactor = new MemoryCompactor(mock(ChatClient.class), 5);
        assertThat(compactor.compact(List.of())).isEmpty();
    }
}
