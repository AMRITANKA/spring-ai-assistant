package dev.amritanka.assistant.service;

import dev.amritanka.assistant.domain.ChatTurn;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sliding window + rolling summary memory compactor.
 * Keeps the last N turns verbatim and compresses older turns into a system summary.
 */
@Component
public class MemoryCompactor {

    private final ChatClient summarizer;
    private final int windowSize;

    public MemoryCompactor(ChatClient summarizer,
                           @Value("${assistant.memory.window-size:12}") int windowSize) {
        this.summarizer = summarizer;
        this.windowSize = windowSize;
    }

    public List<Message> compact(List<ChatTurn> turns) {
        if (turns == null || turns.isEmpty()) return List.of();
        List<Message> out = new ArrayList<>();

        int fromIdx = Math.max(0, turns.size() - windowSize);
        if (fromIdx > 0) {
            String older = serialize(turns.subList(0, fromIdx));
            String summary = summarize(older);
            out.add(new SystemMessage("CONVERSATION SUMMARY SO FAR:\n" + summary));
        }
        for (ChatTurn t : turns.subList(fromIdx, turns.size())) {
            out.add(t.role() == ChatTurn.Role.USER
                    ? new UserMessage(t.content())
                    : new AssistantMessage(t.content()));
        }
        return out;
    }

    private String serialize(List<ChatTurn> turns) {
        StringBuilder sb = new StringBuilder();
        for (ChatTurn t : turns) {
            sb.append(t.role()).append(": ").append(t.content()).append('\n');
        }
        return sb.toString();
    }

    private String summarize(String text) {
        try {
            return summarizer.prompt()
                    .system("Summarize the following chat history into 5-8 bullet points capturing facts, decisions, and open questions.")
                    .user(text)
                    .call()
                    .content();
        } catch (Exception e) {
            // Fallback: truncate
            return text.length() > 2000 ? text.substring(0, 2000) + "..." : text;
        }
    }
}
