package com.example.fwnojackson.repository;

import com.example.fwnojackson.model.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectEntityRepository extends JpaRepository<ProjectEntity, String> {
}
