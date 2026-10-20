package com.example.fwnojackson.controller;

import com.example.fwnojackson.dto.NewNodeRequest;
import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.dto.ProjectTreeBuilder;
import com.example.fwnojackson.service.ProjectsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
@RestController
@RequestMapping("/api")
public class ProjectsController {

    private final ProjectsService projectsService;

    public ProjectsController(ProjectsService projectsService) {
        this.projectsService = projectsService;
    }

    public record UpdateDatesRequest(LocalDate startDate, LocalDate endDate) {}

    @PostMapping("/upload")
    public ResponseEntity<ResponseDto<?>> uploadFile(@RequestBody ProjectsDto input) {
        return respond(projectsService.loadAllProjectEntities(input), HttpStatus.CREATED);
    }

    @GetMapping("/projects/hierarchy")
    public ResponseEntity<Map<String, Object>> getProjectHierarchyAsJson() {
        return ResponseEntity.ok(projectsService.serializeProjectStructure());
    }

    @PostMapping("/projects/{parentUid}/children")
    public ResponseEntity<ResponseDto<?>> addNode(@PathVariable String parentUid,
                                                  @RequestBody NewNodeRequest request) {
        return respond(projectsService.addNewEntity(parentUid, ProjectTreeBuilder.buildNode(request)),
                HttpStatus.CREATED);
    }

    @DeleteMapping("/projects/{parentUid}/children/{uid}")
    public ResponseEntity<ResponseDto<?>> removeNode(@PathVariable String parentUid,
                                                     @PathVariable String uid) {
        return respond(projectsService.removeEntity(parentUid, uid), HttpStatus.OK);
    }

    @PatchMapping("/projects/{uid}/dates")
    public ResponseEntity<ResponseDto<?>> updateTaskDates(@PathVariable String uid,
                                                          @RequestBody UpdateDatesRequest request) {
        return respond(projectsService.updateTaskDates(uid, request.startDate(), request.endDate()),
                HttpStatus.OK);
    }

    @GetMapping("/projects/{uid}/completion")
    public ResponseEntity<Double> getCompletionStatus(
            @PathVariable String uid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate effectiveDate = Objects.requireNonNullElseGet(date, LocalDate::now);
        return ResponseEntity.ok(projectsService.getCompletionStatus(uid, effectiveDate));
    }

    // Every service method reports failure in-band with entitiesCount == 0, so that decides the status.
    private static ResponseEntity<ResponseDto<?>> respond(ResponseDto<?> dto, HttpStatus onSuccess) {
        if (dto.getEntitiesCount() > 0) {
            return ResponseEntity.status(onSuccess).body(dto);
        }
        return ResponseEntity.badRequest().body(dto);
    }
}