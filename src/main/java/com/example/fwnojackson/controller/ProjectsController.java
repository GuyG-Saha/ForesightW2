package com.example.fwnojackson.controller;

import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectEntity;
import com.example.fwnojackson.service.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectsController {
    @Autowired
    ProjectsService projectsService;

    @GetMapping("/projects")
    public Map<String, ProjectEntity> getAllProjects() {
        return projectsService.getProjects();
    }
    @GetMapping("/subprojects")
    public Map<String, ProjectEntity> getAllSubProjects() {
        return projectsService.getSubprojects();
    }
    @GetMapping("/tasks")
    public Map<String, ProjectEntity> getAllTasks() {
        return projectsService.getTasks();
    }
    @GetMapping("/relations")
    public Map<String, List<String>> getAllProjectsRelations() {
        return projectsService.getUids();
    }
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<?> uploadFile(@RequestBody ProjectsDto input) {
        return projectsService.loadAllProjectEntities(input);
    }
    @PatchMapping("/setStartEndDates/{Uid}")
    public ResponseEntity<ResponseDto<?>> setStartEndDatesToProject(@PathVariable String Uid) {
        return ResponseEntity.ok(projectsService.setProjectsStartAndEndDates(Uid));
    }

}
