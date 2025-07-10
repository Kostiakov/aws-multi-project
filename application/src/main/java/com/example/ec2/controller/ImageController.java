package com.example.ec2.controller;

import com.example.ec2.dao.ImageMetadata;
import com.example.ec2.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping()
    public String test() {
        return "Hello world";
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam String name) {
        InputStream s3Stream = imageService.getImageByName(name);
        String encodedFileName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(s3Stream));
    }

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) {
        return imageService.uploadImage(file) ? "SUCCESS" : "FAIL";
    }

    @DeleteMapping("/delete")
    public String delete(@RequestParam String name) {
        return imageService.deleteImageByName(name) ? "SUCCESS" : "FAIL";
    }

    @GetMapping("/imageMetadata")
    public ImageMetadata getImageMetadataByName(@RequestParam String name) {
        return imageService.getImageMetadataByName(name);
    }

    @GetMapping("/imageMetadataRandom")
    public ImageMetadata getRandomImageMetadata() {
        return imageService.getRandomImageMetadata();
    }

    @GetMapping("/checkConsistentState")
    public String checkConsistentState() {
        return imageService.checkConsistentState();
    }

}
