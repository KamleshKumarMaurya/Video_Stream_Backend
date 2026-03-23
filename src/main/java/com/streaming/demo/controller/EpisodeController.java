package com.streaming.demo.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.streaming.demo.entity.Episode;
import com.streaming.demo.repository.EpisodeRepository;

@RestController
@RequestMapping("/api/episodes")
public class EpisodeController {

	@Autowired
	private EpisodeRepository episodeRepository;

	@Value("${app.uploadDir}")
	private String uploadDir;

	@GetMapping("/downlaod/{id}")
	public ResponseEntity<?> downloadEpisode(@PathVariable Long id) throws Exception {

		Episode episode = episodeRepository.findById(id).orElseThrow(() -> new RuntimeException("Episode not found"));

		// 🔥 Example: /videos/story_1/episode_1/master.m3u8
		String hlsUrl = episode.getVideoUrl();

		// 👉 Convert to relative path
		String relativePath = hlsUrl.replace("/videos/", "");
		// story_1/episode_1/master.m3u8

		// 👉 Full system path
		String fullHlsPath = uploadDir + File.separator + relativePath;

		File hlsFile = new File(fullHlsPath);
		if (!hlsFile.exists()) {
			throw new RuntimeException("HLS file not found: " + fullHlsPath);
		}

		String folderPath = hlsFile.getParent();

		// 👉 Output MP4
		String mp4Path = folderPath + File.separator + "temp.mp4";
		File mp4File = new File(mp4Path);

		// 🔥 Generate MP4 if not exists
		if (!mp4File.exists()) {

			ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", fullHlsPath, "-c", "copy", "-bsf:a", "aac_adtstoasc",
					mp4Path);

			pb.redirectErrorStream(true);

			Process process = pb.start();

			// consume stream
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

				while (reader.readLine() != null) {
					// just consume
				}
			}

			int exitCode = process.waitFor();
			if (exitCode != 0) {
				throw new RuntimeException("FFmpeg conversion failed");
			}
		}

		// 👉 Return public URL
		String downloadUrl = "/videos/" + relativePath.replace("master.m3u8", "temp.mp4");

		return ResponseEntity.ok(Map.of("url", downloadUrl));
	}

}
