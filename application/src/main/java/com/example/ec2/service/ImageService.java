package com.example.ec2.service;

import com.example.ec2.dao.ImageMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ImageService {

    InputStream getImageByName(String name);

    boolean uploadImage(MultipartFile multipartFile);

    boolean deleteImageByName(String name);

    ImageMetadata getImageMetadataByName(String name);

    ImageMetadata getRandomImageMetadata();

    String checkConsistentState();

}
