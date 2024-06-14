package de.kolbenik.rgbpixlNasaWebhook.config;

public record WebhookConfig(String webhookUrl, String username, String avatarUrl, long threadId, boolean tts) {
}
