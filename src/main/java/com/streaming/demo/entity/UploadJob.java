package com.streaming.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "upload_job")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class UploadJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storyId;

    private Integer episodeNumber;

    private String title;

    private String status; // UPLOADING, PROCESSING, COMPLETED, FAILED

    private Integer progress; // 0–100

    private String message;

    private LocalDateTime createdAt;
}