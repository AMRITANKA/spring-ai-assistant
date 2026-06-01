package dev.amritanka.assistant.service;

import dev.amritanka.assistant.domain.Conversation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenMeter {

    // Rough cost (USD) per 1K tokens for GPT-4o (illustrative).
    private static final double COST_PER_1K_INPUT  = 0.005;
    private static final double COST_PER_1K_OUTPUT = 0.015;

    private final Counter tokensCounter;
    private final Counter costCounter;

    public TokenMeter(MeterRegistry registry) {
        this.tokensCounter = Counter.builder("assistant.tokens.total")
                .description("Total tokens consumed")
                .register(registry);
        this.costCounter = Counter.builder("assistant.cost.usd.total")
                .description("Total estimated cost (USD)")
                .register(registry);
    }

    /**
     * Approximate token count from characters (~4 chars per token for English).
     */
    public void record(String userId, Conversation conversation, int assistantChars) {
        int inputChars = conversation.totalUserChars();
        int inTokens  = inputChars / 4;
        int outTokens = assistantChars / 4;
        double cost = (inTokens / 1000.0) * COST_PER_1K_INPUT
                    + (outTokens / 1000.0) * COST_PER_1K_OUTPUT;

        tokensCounter.increment(inTokens + outTokens);
        costCounter.increment(cost);

        log.info("token meter user={} in={} out={} cost=${}",
                userId, inTokens, outTokens, String.format("%.5f", cost));
    }
}
