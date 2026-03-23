package com.streaming.demo.common;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TempFileCleaner {

	@Value("${app.uploadDir}")
	private String uploadDir;

	@Scheduled(fixedRate = 30 * 60 * 1000) // every 30 min
	public void cleanTempFiles() {
		File root = new File(uploadDir);
		deleteTempFiles(root);
	}

	private void deleteTempFiles(File dir) {

		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File file : files) {

			if (file.isDirectory()) {
				deleteTempFiles(file);
			} else if (file.getName().equals("temp.mp4")) {

				long diff = System.currentTimeMillis() - file.lastModified();

				// delete after 30 min
				if (diff > 30 * 60 * 1000) {
					file.delete();
					System.out.println("Deleted: " + file.getAbsolutePath());
				}
			}
		}
	}
}
