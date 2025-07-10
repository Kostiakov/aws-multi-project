package com.example.ec2.repository;

import com.example.ec2.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    ImageEntity findByName(String name);

    void deleteByName(String name);

}
