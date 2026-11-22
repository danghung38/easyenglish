package com.dxh.Elearning.dto.request;

import com.dxh.Elearning.enums.StoryLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoryRequest(
        @NotBlank(message = "INVALID_BLANK") @Size(max = 160, message = "INVALID_REQUEST") String title,
        @NotBlank(message = "INVALID_BLANK") @Size(max = 160, message = "INVALID_REQUEST") String titleVi,
        @Size(max = 500, message = "INVALID_REQUEST") String summary,
        @NotBlank(message = "INVALID_BLANK") @Size(max = 24_000, message = "INVALID_REQUEST") String content,
        @NotBlank(message = "INVALID_BLANK") @Size(max = 24_000, message = "INVALID_REQUEST") String translation,
        @Size(max = 2048, message = "INVALID_REQUEST")
        @Pattern(regexp = "^(?:https?://[^\\s]+)?$", message = "INVALID_STORY_IMAGE") String imageUrl,
        @NotNull(message = "INVALID_NULL") StoryLevel level
) {}
