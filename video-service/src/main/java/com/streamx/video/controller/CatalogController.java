package com.streamx.video.controller;

import com.streamx.video.dto.*;
import com.streamx.video.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping("/titles")
    @ResponseStatus(HttpStatus.CREATED)
    public TitleResponse createTitle(@RequestBody CreateTitleRequest request) {
        return catalogService.createTitle(request);
    }

    @PostMapping("/titles/{titleId}/episodes")
    @ResponseStatus(HttpStatus.CREATED)
    public EpisodeResponse createEpisode(@PathVariable UUID titleId, @RequestBody CreateEpisodeRequest request) {
        return catalogService.createEpisode(titleId, request);
    }

    @GetMapping("/titles")
    public Page<TitleResponse> listTitles(Pageable pageable) {
        return catalogService.listTitles(pageable);
    }

    @GetMapping("/titles/{titleId}")
    public TitleDetailResponse getTitleDetail(@PathVariable UUID titleId) {
        return catalogService.getTitleDetail(titleId);
    }
}
