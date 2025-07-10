package com.example.ec2.dao;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ImageMetadata {

    private String name;

    private String fileExtension;

    private LocalDateTime updateTime;

    private Long size;

}
