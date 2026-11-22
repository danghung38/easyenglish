package com.dxh.Elearning.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps the local AI service dotenv values to chatbot-specific properties.
 *
 * <p>The separate namespace prevents an unrelated machine-level OPENAI_API_KEY
 * from silently overriding the 9Router key used by this project. Production can
 * override every value with CHATBOT_PROVIDER_* environment variables.</p>
 */
public class ChatbotProviderEnvironmentPostProcessor implements EnvironmentPostProcessor {
    static final String DEFAULT_ENV_PATH = "../ai-service/.env.backend";
    static final String ENV_PATH_PROPERTY = "chatbot.provider.env-file";
    static final String PROPERTY_SOURCE_NAME = "chatbotProviderDotenv";

    private static final Map<String, String> PROPERTY_MAPPING = Map.of(
            "OPENAI_API_KEY", "chatbot.provider.api-key",
            "OPENAI_BASE_URL", "chatbot.provider.base-url",
            "OPENAI_MODEL", "chatbot.provider.model"
    );

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application
    ) {
        Path envPath = resolveEnvPath(
                environment.getProperty(ENV_PATH_PROPERTY, DEFAULT_ENV_PATH)
        );
        Map<String, Object> properties = loadMappedProperties(envPath);
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, properties)
            );
        }
    }

    Path resolveEnvPath(String configuredPath) {
        Path path = Path.of(configuredPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        Path directPath = path.toAbsolutePath().normalize();
        if (Files.isRegularFile(directPath)) {
            return directPath;
        }

        try {
            // Maven may start through an NTFS junction to avoid Unicode path issues.
            // Resolving the real working directory keeps sibling paths such as
            // ../ai-service/.env.backend valid in both normal and junction-based launches.
            Path realWorkingDirectory = Path.of(".").toRealPath();
            Path realPath = realWorkingDirectory.resolve(path).normalize();
            if (Files.isRegularFile(realPath)) {
                return realPath;
            }
        } catch (IOException ignored) {
            // The direct path below remains the predictable fallback.
        }
        return directPath;
    }

    Map<String, Object> loadMappedProperties(Path envPath) {
        if (!Files.isRegularFile(envPath)) {
            return Map.of();
        }

        Map<String, Object> mapped = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String candidate = line.trim();
                if (candidate.isBlank() || candidate.startsWith("#")) {
                    continue;
                }
                if (candidate.startsWith("export ")) {
                    candidate = candidate.substring("export ".length()).trim();
                }

                int separator = candidate.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String sourceKey = candidate.substring(0, separator).trim();
                String targetKey = PROPERTY_MAPPING.get(sourceKey);
                if (targetKey == null) {
                    continue;
                }

                String value = unquote(candidate.substring(separator + 1).trim());
                if (!value.isBlank()) {
                    mapped.put(targetKey, value);
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }
        return mapped;
    }

    private String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
