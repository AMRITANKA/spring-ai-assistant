package dev.amritanka.assistant.service;

import dev.amritanka.assistant.domain.Conversation;
import dev.amritanka.assistant.repo.ConversationRepository;
import dev.amritanka.assistant.web.dto.ChatChunk;
import dev.amritanka.assistant.web.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are a helpful, accurate enterprise assistant.
            Use the supplied CONTEXT when answering knowledge questions and cite
            source ids inline as [doc:<id>]. If the answer is not in the context,
            say so honestly. Be concise.
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ConversationRepository conversationRepo;
    private final MemoryCompactor memoryCompactor;
    private final TokenMeter tokenMeter;

    public Flux<ChatChunk> streamReply(String userId, ChatRequest req) {
        Conversation conversation = conversationRepo.loadOrCreate(req.conversationId(), userId);

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));

        if (req.useRag()) {
            List<Document> docs = vectorStore.similaritySearch(
                    SearchRequest.query(req.message())
                            .withTopK(6)
                            .withSimilarityThreshold(0.7));
            if (!docs.isEmpty()) {
                String ctx = buildContextBlock(docs);
                messages.add(new SystemMessage("CONTEXT:\n" + ctx));
            }
        }

        messages.addAll(memoryCompactor.compact(conversation.getMessages()));
        messages.add(new UserMessage(req.message()));

        Prompt prompt = new Prompt(messages);
        StringBuilder assistantBuf = new StringBuilder();

        return chatClient.prompt(prompt)
                .stream()
                .content()
                .map(token -> {
                    assistantBuf.append(token);
                    return new ChatChunk(UUID.randomUUID().toString(), token);
                })
                .doOnComplete(() -> {
                    conversation.appendUserMessage(req.message());
                    conversation.appendAssistantMessage(assistantBuf.toString());
                    conversationRepo.save(conversation);
                    tokenMeter.record(userId, conversation, assistantBuf.length());
                });
    }

    private String buildContextBlock(List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (Document d : docs) {
            sb.append("[doc:").append(d.getId()).append("] ")
              .append(d.getContent()).append("\n---\n");
        }
        return sb.toString();
    }
}
