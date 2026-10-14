package com.example.fwnojackson.controller;

import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.service.ProjectsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ProjectsController {
    @Autowired
    ProjectsService projectsService;
    public record UpdateDatesRequest(LocalDate startDate, LocalDate endDate) {}

    @GetMapping("/projects")
    public ResponseEntity<Map<String, ?>> getAllProjects(@RequestParam(required = false) String type) {
        if (type == null || type.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Type parameter is required"));
        switch (type.toUpperCase()) {
            case "PROJECT", "PROJECTS", "TASK", "TASKS", "SUBPROJECT", "SUBPROJECTS" -> {
                return ResponseEntity.ok(projectsService.getProjects());
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid type parameter. Allowed values: PROJECT, SUBPROJECT, TASK"));
            }
        }
    }
    @GetMapping("/projects/hierarchy")
    public ResponseEntity<Map<String, Object>> getProjectHierarchyAsJson() {
        return ResponseEntity.ok(projectsService.serializeProjectStructure());
    }
    @GetMapping("/relations")
    public Map<String, ProjectComponent> getAllProjectsRelations() {
        return projectsService.getProjects();
    }
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<?> uploadFile(@RequestBody ProjectsDto input) {
        return projectsService.loadAllProjectEntities(input);
    }
    @PatchMapping("/projects/{uid}/dates")
    public ResponseEntity<ResponseDto<ProjectComponent>> updateTaskDates(
            @PathVariable String uid, @RequestBody UpdateDatesRequest request) {
        return ResponseEntity.ok(projectsService.updateTaskDates(uid, request.startDate(), request.endDate()));
    }
    @GetMapping("/projects/{uid}/completion")
    public ResponseEntity<Double> getCompletionStatus(
            @PathVariable String uid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        LocalDate effectiveDate = Objects.requireNonNullElseGet(date, LocalDate::now);
        return ResponseEntity.ok(projectsService.getCompletionStatus(uid, effectiveDate));
    }
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ResponseDto<ProjectComponent>> saveNewEntity(@RequestBody ProjectComponent entity) {
        return ResponseEntity.ok(projectsService.addNewEntity(entity.getUid(), entity));
    }
}
