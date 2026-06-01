package dev.amritanka.assistant.repo;

import dev.amritanka.assistant.domain.Conversation;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository placeholder. Swap with a JPA-backed implementation
 * (table: conversations + messages) for production.
 */
@Repository
public class ConversationRepository {

    private final Map<UUID, Conversation> store = new ConcurrentHashMap<>();

    public Conversation loadOrCreate(UUID id, String userId) {
        if (id != null) {
            Conversation existing = store.get(id);
            if (existing != null) return existing;
        }
        Conversation c = Conversation.create(userId, "default");
        store.put(c.getId(), c);
        return c;
    }

    public void save(Conversation conversation) {
        store.put(conversation.getId(), conversation);
    }
}
