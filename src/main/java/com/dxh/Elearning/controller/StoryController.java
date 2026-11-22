package com.dxh.Elearning.controller;

import com.dxh.Elearning.dto.request.StoryRequest;
import com.dxh.Elearning.dto.response.ApiResponse;
import com.dxh.Elearning.dto.response.StoryResponse;
import com.dxh.Elearning.dto.response.StorySummaryResponse;
import com.dxh.Elearning.enums.StoryLevel;
import com.dxh.Elearning.service.impl.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
public class StoryController {
    private final StoryService storyService;

    @GetMapping
    public ApiResponse<List<StorySummaryResponse>> list(@RequestParam(required = false) StoryLevel level) {
        return ApiResponse.<List<StorySummaryResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(storyService.list(level))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<StoryResponse> get(@PathVariable Long id) {
        return ok(storyService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StoryResponse> create(@RequestBody @Valid StoryRequest request) {
        return ApiResponse.<StoryResponse>builder()
                .code(HttpStatus.CREATED.value())
                .result(storyService.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiResponse<StoryResponse> update(@PathVariable Long id, @RequestBody @Valid StoryRequest request) {
        return ok(storyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        storyService.delete(id);
    }

    private ApiResponse<StoryResponse> ok(StoryResponse result) {
        return ApiResponse.<StoryResponse>builder()
                .code(HttpStatus.OK.value())
                .result(result)
                .build();
    }
}
