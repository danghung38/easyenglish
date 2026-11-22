package com.dxh.Elearning.dto.response;

import com.dxh.Elearning.enums.StoryLevel;

public record StorySummaryResponse(
        Long id,
        String title,
        String titleVi,
        String summary,
        String imageUrl,
        StoryLevel level
) {}
