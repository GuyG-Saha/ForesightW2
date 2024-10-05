package com.example.fwnojackson.controller;

import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectEntity;
import com.example.fwnojackson.service.ProjectsService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    public Map<String, ProjectEntity> getAllProjects(@RequestParam String type) {
        switch (type.toUpperCase()) {
            case "PROJECT", "PROJECTS" -> {
                return projectsService.getProjects();
            }
            case "SUBPROJECT", "SUBPROJECTS" -> {
                return projectsService.getSubprojects();
            }
            case "TASK", "TASKS" -> {
                return projectsService.getTasks();
            }
        }
        return null;
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
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDto<ProjectEntity>> saveNewEntity(@RequestBody ProjectEntity entity) {
        return ResponseEntity.ok(projectsService.addNewEntity(entity));
    }
    @GetMapping("/hierarchy")
    public ResponseEntity<String> getProjectHierarchyAsJson() throws JsonProcessingException {
        return ResponseEntity.ok(projectsService.serializeProjectStructure());
    }
}
