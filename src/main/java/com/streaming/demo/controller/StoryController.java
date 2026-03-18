package com.streaming.demo.controller;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.entity.Story;
import com.streaming.demo.service.EpisodeService;
import com.streaming.demo.service.StoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private EpisodeService episodeService;

    // Admin API
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Story> createStory(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("thumbnail") MultipartFile thumbnailFile) throws IOException {
        return ResponseEntity.ok(storyService.createStory(title, description, thumbnailFile));
    }

    @PostMapping("/upload-episode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Episode> uploadEpisode(
            @RequestParam("storyId") Long storyId,
            @RequestParam("episodeNumber") int episodeNumber,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam("thumbnail") MultipartFile thumbnail) throws Exception {
        return ResponseEntity.ok(episodeService.uploadEpisode(storyId, episodeNumber, title, file, thumbnail));
    }

    // Customer API
    @GetMapping
    public ResponseEntity<Page<Story>> getAllStories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(storyService.getAllStories(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Story> getStoryById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(storyService.getStoryById(id));
    }

    @GetMapping("/{id}/episodes")
    public ResponseEntity<List<Episode>> getEpisodesByStory(@PathVariable("id") Long id) {
        return ResponseEntity.ok(episodeService.getEpisodesByStory(id));
    }
}
