package com.streaming.demo.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

@Service
public class FFmpegService {
    private static final Logger logger = LoggerFactory.getLogger(FFmpegService.class);

    @Value("${app.ffmpegPath}")
    private String ffmpegPath;

    public void generateHls(String inputVideoPath, String outputDir) throws Exception {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        File inputVideoFile = new File(inputVideoPath);
        File episodeDirFile = new File(outputDir);

        ProcessBuilder builder = new ProcessBuilder(ffmpegPath, "-i", inputVideoFile.getAbsolutePath(),
                "-filter_complex",
                "[0:v]split=3[v1][v2][v3];" + "[v1]scale=640:360[v1out];" + "[v2]scale=1280:720[v2out];"
                        + "[v3]scale=1920:1080[v3out]",
                "-map", "[v1out]", "-map", "0:a?", "-map", "[v2out]", "-map", "0:a?", "-map", "[v3out]", "-map", "0:a?",
                "-c:v", "libx264", "-c:a", "aac", "-b:v:0", "800k", "-b:v:1", "1500k", "-b:v:2", "3000k", "-b:a:0",
                "128k", "-b:a:1", "128k", "-b:a:2", "128k", "-preset", "veryfast", "-g", "48", "-sc_threshold", "0",
                "-f", "hls", "-hls_time", "10", "-hls_playlist_type", "vod", "-hls_segment_filename",
                new File(episodeDirFile, "segment_%v_%03d.ts").getAbsolutePath(), "-master_pl_name", "master.m3u8",
                "-var_stream_map", "v:0,a:0 v:1,a:1 v:2,a:2",
                new File(episodeDirFile, "playlist_%v.m3u8").getAbsolutePath());

        builder.redirectErrorStream(true);
        logger.info("Executing FFmpeg command: {}", String.join(" ", builder.command()));

        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info("[FFMPEG] {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed. Exit code: " + exitCode);
        }

        logger.info("HLS generation completed successfully for {}", inputVideoPath);
    }
}
