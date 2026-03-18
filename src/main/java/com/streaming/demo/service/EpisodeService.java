package com.streaming.demo.service;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.entity.Story;
import com.streaming.demo.ffmpeg.FFmpegService;
import com.streaming.demo.ffmpeg.FFprobeService;
import com.streaming.demo.repository.EpisodeRepository;
import com.streaming.demo.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${app.uploadDir}")
    private String uploadDir;

    public Episode uploadEpisode(Long storyId, int episodeNumber, String title, MultipartFile videoFile, MultipartFile thumbnailFile) throws Exception {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        String relativePath = String.format("story_%d/episode_%d/", storyId, episodeNumber);
        Path targetDir = Paths.get(uploadDir, relativePath);
        Files.createDirectories(targetDir);

        // Save original video
        String rawVideoName = "raw_" + videoFile.getOriginalFilename();
        Path rawVideoPath = targetDir.resolve(rawVideoName);
        Files.copy(videoFile.getInputStream(), rawVideoPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Save thumbnail
        String thumbnailName = "thumb_" + thumbnailFile.getOriginalFilename();
        Path thumbnailPath = targetDir.resolve(thumbnailName);
        Files.copy(thumbnailFile.getInputStream(), thumbnailPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Process with FFmpeg
        ffmpegService.generateHls(rawVideoPath.toString(), targetDir.toString());

        // Get duration
        int duration = ffprobeService.getVideoDuration(rawVideoPath.toString());

        Episode episode = Episode.builder()
                .story(story)
                .episodeNumber(episodeNumber)
                .title(title)
                .videoUrl("/videos/" + relativePath + "master.m3u8")
                .thumbnail("/videos/" + relativePath + thumbnailName)
                .duration(duration)
                .build();

        Episode savedEpisode = episodeRepository.save(episode);
        Files.deleteIfExists(rawVideoPath); // Original file removed as per user practice
        return savedEpisode;
    }

    public List<Episode> getEpisodesByStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));
        return episodeRepository.findByStoryOrderByEpisodeNumberAsc(story);
    }
}
