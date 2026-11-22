package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.StoryRequest;
import com.dxh.Elearning.entity.Story;
import com.dxh.Elearning.repo.StoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.stories", name = "seed-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class StorySeedRunner implements ApplicationRunner {
    private final ObjectMapper objectMapper;
    private final StoryRepository storyRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        var existingKeys = new HashSet<>(storyRepository.findSeedKeys());
        var pending = new ArrayList<Story>();
        for (String level : List.of("beginner", "intermediate", "advanced")) {
            var resource = new ClassPathResource("stories/" + level + ".json");
            try (var stream = resource.getInputStream()) {
                List<SeedStory> seeds = objectMapper.readValue(stream, new TypeReference<>() {});
                for (SeedStory seed : seeds) {
                    if (!existingKeys.add(seed.key())) {
                        continue;
                    }
                    Story story = new Story();
                    StoryService.applyRequest(story, seed.story());
                    story.setSeedKey(seed.key());
                    pending.add(story);
                }
            }
        }
        if (!pending.isEmpty()) {
            storyRepository.saveAll(pending);
            log.info("Added {} sample bilingual stories", pending.size());
        }
    }

    private record SeedStory(String key, StoryRequest story) {}
}
