package com.streaming.demo.service;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.entity.Story;
import com.streaming.demo.entity.UploadJob;
import com.streaming.demo.ffmpeg.FFmpegService;
import com.streaming.demo.ffmpeg.FFprobeService;
import com.streaming.demo.repository.EpisodeRepository;
import com.streaming.demo.repository.StoryRepository;
import com.streaming.demo.repository.UploadJobRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class EpisodeService {

	@Autowired
	private EpisodeRepository episodeRepository;

	@Autowired
	private StoryRepository storyRepository;

	@Autowired
	private FFmpegService ffmpegService;

	@Autowired
	private FFprobeService ffprobeService;
	@Autowired
	private UploadJobRepository uploadJobRepository;

	@Value("${app.uploadDir}")
	private String uploadDir;

	@Async("taskExecutor")
	public void uploadEpisode(Long jobId, Long storyId, int episodeNumber, String title, File videoFile,
			File thumbnailFile) {
		Path rawVideoPath = null;
		UploadJob job = uploadJobRepository.findById(jobId).orElseThrow();
		try {
			job.setStatus("PROCESSING");
			job.setProgress(10);
			uploadJobRepository.save(job);
			Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));

			String relativePath = String.format("story_%d/episode_%d/", storyId, episodeNumber);
			Path targetDir = Paths.get(uploadDir, relativePath);
			Files.createDirectories(targetDir);

			// Save original video
			String rawVideoName = "raw_" + videoFile.getName();
			rawVideoPath = targetDir.resolve(rawVideoName);
			Files.copy(videoFile.toPath(), rawVideoPath, StandardCopyOption.REPLACE_EXISTING);

			// Save thumbnail
			String thumbnailName = "thumb_" + thumbnailFile.getName();
			Path thumbnailPath = targetDir.resolve(thumbnailName);
			Files.copy(thumbnailFile.toPath(), thumbnailPath, StandardCopyOption.REPLACE_EXISTING);
			job.setProgress(30);
			uploadJobRepository.save(job);
			// Process with FFmpeg
			ffmpegService.generateHls(rawVideoPath.toString(), targetDir.toString());
			job.setProgress(80);
			uploadJobRepository.save(job);
			// Get duration
			int duration = ffprobeService.getVideoDuration(rawVideoPath.toString());

			Episode episode = Episode.builder().story(story).episodeNumber(episodeNumber).title(title)
					.videoUrl("/videos/" + relativePath + "master.m3u8")
					.thumbnail("/videos/" + relativePath + thumbnailName).duration(duration).build();

			episodeRepository.save(episode);
			job.setProgress(100);
			job.setStatus("COMPLETED");
			uploadJobRepository.save(job);

		} catch (Exception e) {
			job.setStatus("FAILED");
			job.setMessage(e.getMessage());
			uploadJobRepository.save(job);
			System.err.println("Error while uploading episode: " + e.getMessage());
			e.printStackTrace();

		} finally {
			// ✅ Cleanup always runs
			try {
				if (rawVideoPath != null) {
					Files.deleteIfExists(rawVideoPath);
				}
			} catch (Exception ex) {
				System.err.println("Failed to delete raw video: " + ex.getMessage());
			}

			if (videoFile != null && videoFile.exists()) {
				videoFile.delete();
			}

			if (thumbnailFile != null && thumbnailFile.exists()) {
				thumbnailFile.delete();
			}
		}
	}

	public List<Episode> getEpisodesByStory(Long storyId) {
		Story story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
		return episodeRepository.findByStoryOrderByEpisodeNumberAsc(story);
	}
}
