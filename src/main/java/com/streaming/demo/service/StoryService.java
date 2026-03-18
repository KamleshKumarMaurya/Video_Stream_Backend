package com.streaming.demo.service;

import com.streaming.demo.entity.Story;
import com.streaming.demo.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Value("${app.uploadDir}")
    private String uploadDir;

    public Story createStory(String title, String description, MultipartFile thumbnailFile) throws IOException {
        Story story = Story.builder()
                .title(title)
                .description(description)
                .build();
        story = storyRepository.save(story);

        String relativePath = String.format("story_%d/", story.getId());
        Path targetDir = Paths.get(uploadDir, relativePath);
        Files.createDirectories(targetDir);

        String thumbnailName = "thumb_" + thumbnailFile.getOriginalFilename();
        Path thumbnailPath = targetDir.resolve(thumbnailName);
        Files.copy(thumbnailFile.getInputStream(), thumbnailPath);

        story.setThumbnail("/videos/" + relativePath + thumbnailName);
        return storyRepository.save(story);
    }

    public Page<Story> getAllStories(Pageable pageable) {
        return storyRepository.findAll(pageable);
    }

    public Story getStoryById(Long id) {
        return storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
    }
}
