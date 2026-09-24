package com.example.fwnojackson.service;

import com.example.fwnojackson.dto.ProjectTreeBuilder;
import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.model.ProjectComposite;
import com.example.fwnojackson.model.ProjectType;
import com.example.fwnojackson.repository.ProjectEntityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectsService {
    private final Map<String, ProjectComponent> allProjects;
    private ProjectComponent root;

    @Autowired
    public ProjectsService() {
        allProjects = new HashMap<>();

    }
    public ResponseDto<?> loadAllProjectEntities(ProjectsDto dto) {
        if (Objects.isNull(dto) || dto.getItems() == null || dto.getItems().isEmpty()) {
            return new ResponseDto<>("Bad Request", 0);
        }
        root = ProjectTreeBuilder.buildTree(dto.getItems());
        int count = registerRecursively(root);
        return new ResponseDto<>("CREATED", count);
    }

    public ResponseDto<ProjectComponent> addNewEntity(String parentUid, ProjectComponent entity) {
        ProjectComponent parent = allProjects.get(parentUid);
        if (Objects.isNull(parent)) {
            return new ResponseDto<>("Parent uid not found", 0);
        }
        if (!(parent instanceof ProjectComposite parentComposite)) {
            return new ResponseDto<>("Cannot add children to a Task", 0);
        }
        if (entity.getType() == ProjectType.SUBPROJECT && entity.getChildren().isEmpty()) {
            return new ResponseDto<>("Subproject must be added with at least one related Task", 0);
        }

        parentComposite.addChild(entity);
        int entitiesCount = registerRecursively(entity);
        return new ResponseDto<>("Added", entity, entitiesCount);
    }
    public ResponseDto<ProjectComponent> removeEntity(String parentUid, String uid) {
        ProjectComponent target = allProjects.get(uid);
        if (Objects.isNull(target)) {
            return new ResponseDto<>("Unknown uid provided", 0);
        }
        // Deleting the root itself is a special case: no parent to guard against,
        // and the whole tree goes with it.
        if (Objects.equals(uid, root.getUid())) {
            int entitiesCount = removeRecursively(target);
            root = null;
            return new ResponseDto<>("Removed root project", target, entitiesCount);
        }

        ProjectComponent parent = allProjects.get(parentUid);
        if (Objects.isNull(parent)) {
            return new ResponseDto<>("Parent uid not found", 0);
        }
        if (!(parent instanceof ProjectComposite parentComposite)) {
            return new ResponseDto<>("Parent cannot have children", 0);
        }
        if (parentComposite.getChildren().size() == 1) {
            return new ResponseDto<>("Cannot remove the last remaining child", 0);
        }
        if (!parentComposite.removeChild(target)) {
            return new ResponseDto<>("Given uid is not a child of the specified parent", 0);
        }
        int entitiesCount = removeRecursively(target);
        return new ResponseDto<>("Removed entity", target, entitiesCount);
    }

    /** Registers this node and any children it already arrived with (e.g. a
     Subproject submitted together with a pre-attached Task) into allProjects,
     so later uid-based lookups (add/delete under this node) can find them **/
    private int registerRecursively(ProjectComponent entity) {
        allProjects.put(entity.getUid(), entity);
        int count = 1;
        for (ProjectComponent child : entity.getChildren()) {
            count += registerRecursively(child);
        }
        return count;
    }
    private int removeRecursively(ProjectComponent entity) {
        allProjects.remove(entity.getUid());
        int count = 1;
        for (ProjectComponent child : entity.getChildren()) {
            count += removeRecursively(child);
        }
        return count;
    }
    public Map<String, Object> serializeProjectStructure() {
        if (Objects.nonNull(root)) {
            Map<String, Object> rootSerialized = serializeNode(root, null);
            return rootSerialized;
        } else {
            throw new IllegalArgumentException("Cannot serialize projects tree structure when the root project is null");
        }
    }
    private Map<String, Object> serializeNode(ProjectComponent node, String parentUid) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uid", node.getUid());
        map.put("name", node.getName());
        map.put("type", node.getType());
        map.put("startDate", node.getStartDate());
        map.put("endDate", node.getEndDate());
        map.put("parentUid", parentUid);

        if (node instanceof ProjectComposite composite) {
            map.put("children", composite.getChildren().stream()
                    .map(child -> serializeNode(child, node.getUid()))
                    .collect(Collectors.toList()));
        }
        return map;
    }
    private List<Map<String, Object>> serializeChildren(String parentUid, Map<String, ProjectComponent> subprojects, Map<String, ProjectComponent> tasks) throws JsonProcessingException {
        List<Map<String, Object>> childrenList = new ArrayList<>();

        return childrenList;
    }


    public Map<String, ProjectComponent> getProjects() {
        return allProjects;
    }

}
