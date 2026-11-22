package com.dxh.Elearning.service.impl;

import com.dxh.Elearning.dto.request.StoryRequest;
import com.dxh.Elearning.dto.response.StoryResponse;
import com.dxh.Elearning.dto.response.StorySummaryResponse;
import com.dxh.Elearning.entity.Story;
import com.dxh.Elearning.enums.StoryLevel;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import com.dxh.Elearning.repo.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {
    private final StoryRepository storyRepository;

    public List<StorySummaryResponse> list(StoryLevel level) {
        return storyRepository.findSummaries(level);
    }

    public StoryResponse get(Long id) {
        return toResponse(findStory(id));
    }

    @Transactional
    public StoryResponse create(StoryRequest request) {
        Story story = new Story();
        applyRequest(story, request);
        return toResponse(storyRepository.save(story));
    }

    @Transactional
    public StoryResponse update(Long id, StoryRequest request) {
        Story story = findStory(id);
        applyRequest(story, request);
        return toResponse(story);
    }

    @Transactional
    public void delete(Long id) {
        findStory(id).setDeletedAt(LocalDateTime.now());
    }

    private Story findStory(Long id) {
        return storyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.STORY_NOT_FOUND));
    }

    static void applyRequest(Story story, StoryRequest request) {
        story.setTitle(request.title().strip());
        story.setTitleVi(request.titleVi().strip());
        story.setSummary(request.summary());
        story.setContent(request.content().strip());
        story.setTranslation(request.translation().strip());
        story.setImageUrl(request.imageUrl() == null || request.imageUrl().isBlank()
                ? null : request.imageUrl());
        story.setLevel(request.level());
    }

    private StoryResponse toResponse(Story story) {
        return new StoryResponse(
                new StorySummaryResponse(story.getId(), story.getTitle(), story.getTitleVi(),
                        story.getSummary(), story.getImageUrl(), story.getLevel()),
                story.getContent(),
                story.getTranslation()
        );
    }
}
