package com.example.fwnojackson.service;

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

@Service
public class ProjectsService {
    private final Map<String, ProjectComponent> byUid;
    private ProjectComponent root;

    @Autowired
    public ProjectsService(ProjectEntityRepository repository) {
        byUid = new HashMap<>();

    }
    public ResponseDto<?> loadAllProjectEntities(ProjectsDto dto) {
        if (Objects.nonNull(dto)) {
            for (ProjectComponent entity : dto.getItems()) {
                if (entity.getType() == ProjectType.SUBPROJECT
                    && Objects.isNull(entity.getParentUid())) {
                    byUid.put(entity.getUid(), entity);
                } else if (entity.getType() == ProjectType.PROJECT
                        && Objects.nonNull(entity.getParentUid())) {
                    subprojects.put(entity.getUid(), entity);
                    attachEntityToParent(entity);
                } else if (entity.getType() == ProjectType.TASK) {
                    tasks.put(entity.getUid(), entity);
                    attachEntityToParent(entity);
                } else {
                    return new ResponseDto<>("UNKNOWN ENTITY", 0);
                }
            }
            int count = byUid.size() + subprojects.size() + tasks.size();
            return new ResponseDto<>("CREATED", count);
        } else
            return new ResponseDto<>("Bad Request", 0);
    }
    private void attachEntityToParent(ProjectComponent entity) {
        switch (entity.getType().name().toUpperCase()) {
            case "PROJECT":
                if (byUid.containsKey(entity.getParentUid()))
                    addChildEntityToList(entity, byUid);
                else
                    addChildEntityToList(entity, subprojects);
                break;
            case "TASK":
                addChildEntityToList(entity, subprojects);
                break;
        }
    }
    private void addChildEntityToList(ProjectComponent entity, Map<String, ProjectComponent> parentsMap) {
        if (parentsMap.containsKey(entity.getParentUid())
                && Objects.nonNull(Uids.get(entity.getParentUid()))) {
            Uids.get(entity.getParentUid()).add(entity.getUid());
        } else if (parentsMap.containsKey(entity.getParentUid())
                && Objects.isNull(Uids.get(entity.getParentUid()))) {
            ArrayList<String> childrenUids = new ArrayList<>();
            childrenUids.add(entity.getUid());
            Uids.put(entity.getParentUid(), childrenUids);
        }
    }

    public ResponseDto<?> setProjectsStartAndEndDates(String projectUid) {
        LocalDate earliest = LocalDate.MAX;
        LocalDate latest = LocalDate.MIN;
        int index = 0;
        if (Uids.containsKey(projectUid)) {
            List<String> uidsToIterate = Uids.get(projectUid);
            while (index < uidsToIterate.size()) {
                String subUid = uidsToIterate.get(index);
                if (tasks.containsKey(subUid)) {
                    if (tasks.get(subUid).getStartDate().isBefore(earliest))
                        earliest = tasks.get(subUid).getStartDate();
                    if (tasks.get(subUid).getEndDate().isAfter(latest))
                        latest = tasks.get(subUid).getEndDate();
                } else if (subprojects.containsKey(subUid)) {
                    System.out.println(subUid + " is not a Task. Checking through Subprojects...");
                    uidsToIterate.addAll(Uids.get(subUid));
                }
                index++;
            }
            ProjectComponent project = byUids.containsKey(projectUid) ? byUids.get(projectUid) : subprojects.get(projectUid);
            project.setStartDate(earliest);
            project.setEndDate(latest);
            return new ResponseDto<ProjectComponent>("Updated start and end date", project, 1);
        } else return new ResponseDto<String>("Invalid or unknown Uid entered", null, 0);
    }

    public ResponseDto<ProjectComponent> addNewEntity(String parentUid, ProjectComponent entity) {
        ProjectComponent parent = byUid.get(parentUid);
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
        registerRecursively(entity);
        return new ResponseDto<>("Added", entity, 1);
    }

    /** Registers this node and any children it already arrived with (e.g. a
     Subproject submitted together with a pre-attached Task) into byUid,
     so later uid-based lookups (add/delete under this node) can find them **/
    private void registerRecursively(ProjectComponent entity) {
        byUid.put(entity.getUid(), entity);
        for (ProjectComponent child : entity.getChildren()) {
            registerRecursively(child);
        }
    }
    public List<Map<String, Object>> serializeProjectStructure() throws JsonProcessingException {
        List<Map<String, Object>> projectList = new ArrayList<>();
        for (Map.Entry<String, ProjectComponent> entry : byUid.entrySet()) {
            ProjectComponent project = entry.getValue();
            // Use LinkedHashMap to ensure field order
            Map<String, Object> projectMap = new LinkedHashMap<>();
            projectMap.put("project", project.serializeProject());
            // Serialize both subprojects and tasks for the current project
            List<Map<String, Object>> children = serializeChildren(project.getUid(), subprojects, tasks);
            if (!children.isEmpty()) {
                projectMap.put("children", children);
            }
            projectList.add(projectMap);
        }
        return projectList;
    }
    private List<Map<String, Object>> serializeChildren(String parentUid, Map<String, ProjectComponent> subprojects, Map<String, ProjectComponent> tasks) throws JsonProcessingException {
        List<Map<String, Object>> childrenList = new ArrayList<>();
        List<String> childUids = Uids.get(parentUid);
        if (Objects.isNull(childUids) || childUids.isEmpty()) {
            return childrenList;
        }
        for (String childUid : childUids) {
            ProjectComponent childProject = subprojects.get(childUid);
            if (childProject != null) {
                // Create a LinkedHashMap to maintain field order for subprojects
                Map<String, Object> childMap = new LinkedHashMap<>();
                childMap.put("subproject", childProject.serializeProject());
                // Recursively serialize grandchildren (both subprojects and tasks)
                List<Map<String, Object>> grandChildren = serializeChildren(childUid, subprojects, tasks);
                if (!grandChildren.isEmpty()) {
                    childMap.put("children", grandChildren); // Add grandchildren if present
                }
                childrenList.add(childMap);
            }
            // Handle tasks
            ProjectComponent task = tasks.get(childUid);
            if (task != null) {
                Map<String, Object> taskMap = new LinkedHashMap<>();
                taskMap.put("task", task.serializeProject());
                // No further recursion for tasks (they have no children) so just add them
                childrenList.add(taskMap);
            }
        }
        return childrenList;
    }


    public Map<String, ProjectComponent> getProjects() {
        return byUid;
    }

}
