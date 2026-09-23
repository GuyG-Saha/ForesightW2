package com.example.fwnojackson.dto;

import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.model.ProjectComposite;
import com.example.fwnojackson.model.ProjectType;
import com.example.fwnojackson.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTreeBuilder {

    public static ProjectComponent buildTree(String jsonFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        List<ProjectNodeDTO> nodes = List.of(mapper.readValue(
                new File(jsonFilePath), ProjectNodeDTO[].class
        ));
        return buildTree(nodes);
    }
    public static ProjectComponent buildTree(List<ProjectNodeDTO> nodes) {
        Map<String, ProjectComponent> uidToComponent = new HashMap<>();
        ProjectComponent root = null;

        // First pass: create instances
        for (ProjectNodeDTO dto : nodes) {
            boolean hasParent = dto.parentUid != null && !dto.parentUid.isEmpty();
            ProjectComponent component;

            if ("TASK".equalsIgnoreCase(dto.type)) {
                component = new Task(
                        dto.uid,
                        dto.name,
                        dto.startDate != null ? LocalDate.parse(dto.startDate) : null,
                        dto.endDate != null ? LocalDate.parse(dto.endDate) : null
                );
            } else if ("PROJECT".equalsIgnoreCase(dto.type)) {
                ProjectType type = hasParent ? ProjectType.SUBPROJECT : ProjectType.PROJECT;
                component = new ProjectComposite(dto.uid, dto.name, type);
            } else {
                throw new IllegalArgumentException("Unknown type: " + dto.type);
            }

            uidToComponent.put(dto.uid, component);
        }
        // Second pass: wire up hierarchy
        for (ProjectNodeDTO dto : nodes) {
            if (dto.parentUid == null || dto.parentUid.isEmpty()) {
                if (root != null) {
                    throw new IllegalStateException(
                            "Multiple root nodes found: " + root.getUid() + " and " + dto.uid
                    );
                }
                root = uidToComponent.get(dto.uid);
            } else {
                ProjectComponent parent = uidToComponent.get(dto.parentUid);
                if (parent instanceof ProjectComposite composite) {
                    composite.addChild(uidToComponent.get(dto.uid));
                } else {
                    throw new IllegalStateException("Parent is not a composite: " + dto.parentUid);
                }
            }
        }
        if (root == null) {
            throw new IllegalStateException("No root node found (node without parentUid)");
        }
        return root;
    }
}

