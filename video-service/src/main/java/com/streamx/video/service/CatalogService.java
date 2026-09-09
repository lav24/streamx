package com.streamx.video.service;

import com.streamx.video.dto.*;
import com.streamx.video.entity.Episode;
import com.streamx.video.entity.Title;
import com.streamx.video.repository.EpisodeRepository;
import com.streamx.video.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final TitleRepository titleRepository;
    private final EpisodeRepository episodeRepository;

    @Transactional
    public TitleResponse createTitle(CreateTitleRequest request) {
        Title title = Title.builder()
                .name(request.name())
                .description(request.description())
                .genres(request.genres() == null ? new String[0] : request.genres().toArray(new String[0]))
                .releaseYear(request.releaseYear())
                .type(request.type())
                .build();
        titleRepository.save(title);
        return TitleResponse.from(title);
    }

    @Transactional
    public EpisodeResponse createEpisode(UUID titleId, CreateEpisodeRequest request) {
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Title not found: " + titleId));

        Episode episode = Episode.builder()
                .title(title)
                .season(request.season())
                .episodeNumber(request.episodeNumber())
                .durationSeconds(request.durationSeconds())
                .build();
        episodeRepository.save(episode);
        return EpisodeResponse.from(episode);
    }

    @Transactional(readOnly = true)
    public Page<TitleResponse> listTitles(Pageable pageable) {
        return titleRepository.findAll(pageable).map(TitleResponse::from);
    }

    @Transactional(readOnly = true)
    public TitleDetailResponse getTitleDetail(UUID titleId) {
        Title title = titleRepository.findById(titleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Title not found: " + titleId));

        List<EpisodeResponse> episodes = episodeRepository.findByTitleIdWithTitle(titleId).stream()
                .map(EpisodeResponse::from)
                .toList();

        return new TitleDetailResponse(TitleResponse.from(title), episodes);
    }
}
