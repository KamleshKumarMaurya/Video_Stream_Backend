package com.streaming.demo.controller;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.entity.Story;
import com.streaming.demo.entity.UploadJob;
import com.streaming.demo.repository.EpisodeRepository;
import com.streaming.demo.repository.UploadJobRepository;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

	@Autowired
	private StoryService storyService;

	@Autowired
	private EpisodeService episodeService;
	@Autowired
	private EpisodeRepository episodeRepository;
	@Autowired
	private UploadJobRepository uploadJobRepository;

	// Admin API
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Story> createStory(@RequestParam("title") String title,
			@RequestParam("description") String description, @RequestParam(defaultValue = "false") boolean latest_story,
			@RequestParam("thumbnail") MultipartFile thumbnailFile) throws IOException {
		return ResponseEntity.ok(storyService.createStory(title, description, thumbnailFile, latest_story));
	}

	@PostMapping("/upload-episode")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> uploadEpisode(@RequestParam("storyId") Long storyId,
			@RequestParam("episodeNumber") int episodeNumber, @RequestParam("title") String title,
			@RequestParam("file") MultipartFile file, @RequestParam("thumbnail") MultipartFile thumbnail)
			throws Exception {
		boolean exists = episodeRepository.existsByStoryIdAndEpisodeNumber(storyId, episodeNumber);
		if (exists) {
			throw new RuntimeException("Episode already exists");
		}

		UploadJob job = UploadJob.builder().storyId(storyId).episodeNumber(episodeNumber).title(title)
				.status("UPLOADING").progress(0).createdAt(LocalDateTime.now()).build();

		uploadJobRepository.save(job);

		File videoTemp = File.createTempFile("video_", file.getOriginalFilename());
		file.transferTo(videoTemp);

		File thumbTemp = File.createTempFile("thumb_", thumbnail.getOriginalFilename());
		thumbnail.transferTo(thumbTemp);

		episodeService.uploadEpisode(job.getId(), storyId, episodeNumber, title, videoTemp, thumbTemp);
		return ResponseEntity.ok(Map.of("jobId", job.getId()));
	}

	// Customer API
	@GetMapping
	public ResponseEntity<Page<Story>> getAllStories(@RequestParam(name = "page", defaultValue = "0") int page,
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

	// 🔥 Get single job status
	@GetMapping("/upload-status/{jobId}")
	public ResponseEntity<UploadJob> getStatus(@PathVariable Long jobId) {
		return ResponseEntity.ok(uploadJobRepository.findById(jobId).orElseThrow());
	}

	// 🔥 Get all active uploads
	@GetMapping("/upload-status/active")
	public ResponseEntity<List<UploadJob>> getActiveUploads() {
		return ResponseEntity.ok(uploadJobRepository.findByStatusIn(List.of("UPLOADING", "PROCESSING")));
	}
}
