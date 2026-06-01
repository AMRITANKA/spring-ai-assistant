package dev.amritanka.assistant.domain;

public record ChatTurn(Role role, String content) {
    public enum Role { USER, ASSISTANT, SYSTEM }
}
