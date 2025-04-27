package com.example.fwnojackson.service;

import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectEntity;
import com.example.fwnojackson.repository.ProjectEntityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ProjectsService {
    private final ProjectEntityRepository repository;
    private final Map<String, ProjectEntity> projects;
    private final Map<String, ProjectEntity> subprojects;
    private final Map<String, ProjectEntity> tasks;
    private final Map<String, List<String>> Uids;

    @Autowired
    public ProjectsService(ProjectEntityRepository repository) {
        this.repository = repository;
        projects = new HashMap<>();
        subprojects = new HashMap<>();
        tasks = new HashMap<>();
        Uids = new HashMap<>();
    }
    public ResponseDto<?> loadAllProjectEntities(ProjectsDto dto) {
        if (Objects.nonNull(dto)) {
            for (ProjectEntity entity : dto.getItems()) {
                if (entity.getType().equalsIgnoreCase("PROJECT")
                    && Objects.isNull(entity.getParentUid())) {
                    projects.put(entity.getUid(), entity);
                } else if (entity.getType().equalsIgnoreCase("PROJECT")
                        && Objects.nonNull(entity.getParentUid())) {
                    subprojects.put(entity.getUid(), entity);
                    attachEntityToParent(entity);
                } else if (entity.getType().equalsIgnoreCase("TASK")) {
                    tasks.put(entity.getUid(), entity);
                    attachEntityToParent(entity);
                } else {
                    return new ResponseDto<>("UNKNOWN ENTITY", 0);
                }
                repository.save(entity);
            }
            int count = projects.size() + subprojects.size() + tasks.size();
            return new ResponseDto<>("CREATED", count);
        } else
            return new ResponseDto<>("Bad Request", 0);
    }
    private void attachEntityToParent(ProjectEntity entity) {
        switch (entity.getType().toUpperCase()) {
            case "PROJECT":
                if (projects.containsKey(entity.getParentUid()))
                    addChildEntityToList(entity, projects);
                else
                    addChildEntityToList(entity, subprojects);
                break;
            case "TASK":
                addChildEntityToList(entity, subprojects);
                break;
        }
    }
    private void addChildEntityToList(ProjectEntity entity, Map<String, ProjectEntity> parentsMap) {
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
            ProjectEntity project = projects.containsKey(projectUid) ? projects.get(projectUid) : subprojects.get(projectUid);
            project.setStartDate(earliest);
            project.setEndDate(latest);
            repository.save(project);
            return new ResponseDto<ProjectEntity>("Updated start and end date", project, 1);
        } else return new ResponseDto<String>("Invalid or unknown Uid entered", null, 0);
    }

    public ResponseDto<ProjectEntity> addNewEntity(ProjectEntity entity) {
        if (Objects.nonNull(entity.getParentUid())) {
            if (entity.getType().equalsIgnoreCase("PROJECT")) {
                // check that Subproject has a related Task
                for (String taskId : tasks.keySet()) {
                    if (tasks.get(taskId).getParentUid().equalsIgnoreCase(entity.getUid())) {
                        if (projects.containsKey(entity.getParentUid()))
                            addChildEntityToList(entity, projects);
                        else
                            addChildEntityToList(entity, subprojects);
                        subprojects.put(entity.getUid(), entity);
                        return new ResponseDto<>("New Subproject added", entity, 1);
                    }
                }
                return new ResponseDto<>("Not found related task", 0);
            } else if (entity.getType().equalsIgnoreCase("TASK")) {
                if (subprojects.containsKey(entity.getParentUid())) {
                    addChildEntityToList(entity, subprojects);
                } else {
                    // I don't enforce creation of a new Subproject - avoid null values in the map
                    return new ResponseDto<>("Unknown parent Uid provided", 0);
                }
                tasks.put(entity.getUid(), entity);
                return new ResponseDto<>("New Task added", entity, 1);
            }
        }
        return new ResponseDto<>("Invalid entity type", 0);
    }

    public List<Map<String, Object>> serializeProjectStructure() throws JsonProcessingException {
        List<Map<String, Object>> projectList = new ArrayList<>();
        for (Map.Entry<String, ProjectEntity> entry : projects.entrySet()) {
            ProjectEntity project = entry.getValue();
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
    private List<Map<String, Object>> serializeChildren(String parentUid, Map<String, ProjectEntity> subprojects, Map<String, ProjectEntity> tasks) throws JsonProcessingException {
        List<Map<String, Object>> childrenList = new ArrayList<>();
        List<String> childUids = Uids.get(parentUid);
        if (Objects.isNull(childUids) || childUids.isEmpty()) {
            return childrenList;
        }
        for (String childUid : childUids) {
            ProjectEntity childProject = subprojects.get(childUid);
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
            ProjectEntity task = tasks.get(childUid);
            if (task != null) {
                Map<String, Object> taskMap = new LinkedHashMap<>();
                taskMap.put("task", task.serializeProject());
                // No further recursion for tasks (they have no children) so just add them
                childrenList.add(taskMap);
            }
        }
        return childrenList;
    }


    public Map<String, ProjectEntity> getProjects() {
        return projects;
    }

    public Map<String, ProjectEntity> getSubprojects() {
        return subprojects;
    }

    public Map<String, ProjectEntity> getTasks() {
        return tasks;
    }

    public Map<String, List<String>> getUids() {
        return Uids;
    }
}
