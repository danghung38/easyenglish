package com.dxh.Elearning.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChatbotProviderEnvironmentPostProcessorTest {
    @TempDir
    private Path tempDirectory;

    @Test
    void mapsOnlyRequiredOpenAiCompatibleSettings() throws IOException {
        Path envFile = tempDirectory.resolve(".env.backend");
        Files.writeString(envFile, """
                # local provider
                OPENAI_API_KEY="secret-value"
                OPENAI_BASE_URL=http://localhost:20128/v1
                OPENAI_MODEL=cx/gpt-5.6-luna
                JWT_KEY=must-not-be-imported
                """);

        Map<String, Object> properties = new ChatbotProviderEnvironmentPostProcessor()
                .loadMappedProperties(envFile);

        assertEquals("secret-value", properties.get("chatbot.provider.api-key"));
        assertEquals(
                "http://localhost:20128/v1",
                properties.get("chatbot.provider.base-url")
        );
        assertEquals("cx/gpt-5.6-luna", properties.get("chatbot.provider.model"));
        assertFalse(properties.containsKey("JWT_KEY"));
    }

    @Test
    void returnsEmptyPropertiesWhenFileDoesNotExist() {
        Map<String, Object> properties = new ChatbotProviderEnvironmentPostProcessor()
                .loadMappedProperties(tempDirectory.resolve("missing.env"));

        assertEquals(Map.of(), properties);
    }
}
