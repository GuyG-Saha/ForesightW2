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
    public ResponseEntity<Map<String, ?>> getAllProjects(@RequestParam(required = false) String type) {
        if (type == null || type.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Type parameter is required"));
        switch (type.toUpperCase()) {
            case "PROJECT", "PROJECTS" -> {
                return ResponseEntity.ok(projectsService.getProjects());
            }
            case "SUBPROJECT", "SUBPROJECTS" -> {
                return ResponseEntity.ok(projectsService.getSubprojects());
            }
            case "TASK", "TASKS" -> {
                return ResponseEntity.ok(projectsService.getTasks());
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid type parameter. Allowed values: PROJECT, SUBPROJECT, TASK"));
            }
        }
    }
    @GetMapping("/projects/hierarchy")
    public ResponseEntity<List<Map<String, Object>>> getProjectHierarchyAsJson() throws JsonProcessingException {
        return ResponseEntity.ok(projectsService.serializeProjectStructure());
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
}
