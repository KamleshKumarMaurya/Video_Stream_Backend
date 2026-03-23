package com.streaming.demo.repository;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    List<Episode> findByStoryOrderByEpisodeNumberAsc(Story story);
    Optional<Episode> findByStoryAndEpisodeNumber(Story story, int episodeNumber);
	boolean existsByStoryIdAndEpisodeNumber(Long storyId, int episodeNumber);
}
