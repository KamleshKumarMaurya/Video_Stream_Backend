package com.streaming.demo.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class FFprobeService {
    private static final Logger logger = LoggerFactory.getLogger(FFprobeService.class);

    @Value("${app.ffprobePath}")
    private String ffprobePath;

    public int getVideoDuration(String videoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath
            );

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            process.waitFor();

            if (line != null) {
                return (int) Double.parseDouble(line);
            }
        } catch (Exception e) {
            logger.error("Error extracting video duration: {}", e.getMessage());
        }
        return 0;
    }
}
