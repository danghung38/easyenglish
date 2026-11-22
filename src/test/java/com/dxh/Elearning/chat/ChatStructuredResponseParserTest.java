package com.dxh.Elearning.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatStructuredResponseParserTest {
    private final ChatStructuredResponseParser parser =
            new ChatStructuredResponseParser(new ObjectMapper());

    @Test
    void parsesPlainJson() {
        String response = """
                {"answer":"Hello","intent":"GREETING","confidence":0.99,
                "learningCandidate":null,"quiz":null}
                """;
        ChatAssistantResult result = parser.parse(response).orElseThrow();
        assertEquals("Hello", result.getAnswer());
        assertEquals("GREETING", result.getIntent());
    }

    @Test
    void parsesJsonWrappedInMarkdownFence() {
        String response = "```json\n{\"answer\":\"Hi\",\"intent\":\"GREETING\",\"confidence\":1}\n```";
        assertEquals("Hi", parser.parse(response).orElseThrow().getAnswer());
    }

    @Test
    void rejectsBlankAndMalformedResponses() {
        assertTrue(parser.parse(" ").isEmpty());
        assertTrue(parser.parse("not json").isEmpty());
        assertTrue(parser.parse("{broken}").isEmpty());
    }
}
