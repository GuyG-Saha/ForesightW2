package com.example.fwnojackson.service;

import com.example.fwnojackson.Inputs;
import com.example.fwnojackson.dto.ProjectNodeDTO;
import com.example.fwnojackson.dto.ProjectTreeBuilder;
import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.model.ProjectComposite;
import com.example.fwnojackson.model.ProjectType;
import com.example.fwnojackson.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ProjectsServiceTest {
    private ProjectsService service;
    private ObjectMapper mapper;
    private final String BARE_SUBPROJECT_MSG = "Subproject must be added with at least one related Task";
    private final String CANNOT_REMOVE_CHILD_MSG = "Cannot remove the last remaining child";
    private final String WRONG_CHILD_MSG = "Given uid is not a child of the specified parent";

    @BeforeEach
    void setUp() {
        service = new ProjectsService();
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    private ProjectsDto loadFixture() throws Exception {
        return mapper.readValue(Inputs.JSON_INPUT, ProjectsDto.class);
    }

    @Test
    void loadAllProjectEntities_registersEveryNodeFromTheFixture() throws Exception {
        ProjectsDto dto = loadFixture();
        int expectedNodeCount = dto.getItems().size();
        ResponseDto<?> response = service.loadAllProjectEntities(dto);

        System.out.println("--- loadAllProjectEntities result ---");
        System.out.println("message: " + response.getMessage());
        System.out.println("entitiesCount: " + response.getEntitiesCount());
        System.out.println("expectedNodeCount (from fixture): " + expectedNodeCount);
        System.out.println("allProjects size: " + service.getProjects().size());
        System.out.println();
        System.out.println("--- tree shape ---");
        printTree(service.getProjects().get(service.getProjects().keySet().stream()
                .filter(uid -> service.getProjects().get(uid) instanceof ProjectComposite pc
                        && pc.getType() == ProjectType.PROJECT)
                .findFirst()
                .orElseThrow()), "");

        assertThat(response.getMessage()).isEqualTo("CREATED");
        assertThat(response.getEntitiesCount()).isEqualTo(expectedNodeCount);
        assertThat(service.getProjects()).hasSize(expectedNodeCount);
    }

    @Test
    void loadAllProjectEntities_rejectsEmptyPayload() {
        // assert the "Bad Request" branch — no tree/root should be created.
        ProjectsDto dto = new ProjectsDto();
        ResponseDto<?> response = service.loadAllProjectEntities(dto);
        assertThat(response.getMessage()).isEqualTo("Bad Request");
        assertThat(response.getEntitiesCount()).isEqualTo(0);
        assertThat(service.getProjects()).hasSize(0);
    }

    @Test
    void addNewEntity_addsTaskUnderExistingSubproject() throws Exception {
        // call addNewEntity, assert entitiesCount == 1 and the parent's
        // getChildren() now includes it.
        ProjectsDto minimalTree = rootOnlyTree();
        service.loadAllProjectEntities(minimalTree);
        Task newTask = new Task("tt1", "New Leaf Task",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));
        ResponseDto<ProjectComponent> response = service.addNewEntity("p1", newTask);
        assertThat(response.getEntitiesCount()).isEqualTo(1);
        assertThat(service.getProjects().size()).isEqualTo(2);
        assertThat(service.getProjects().get("p1").getChildren()).contains(newTask);
    }

    @Test
    void addNewEntity_rejectsSubprojectWithNoChildren() {
        // assert addNewEntity rejects it with the "must be added with at
        // least one related Task" message.
        ProjectsDto minimalTree = rootOnlyTree();
        service.loadAllProjectEntities(minimalTree);
        ProjectComposite subproject = new ProjectComposite("sp1", "New Subproject with no Task",
                ProjectType.SUBPROJECT);
        ResponseDto<ProjectComponent> response = service.addNewEntity("p1", subproject);
        assertThat(response.getMessage()).isEqualTo(BARE_SUBPROJECT_MSG);
    }

    @Test
    void removeEntity_rejectsRemovingTheLastRemainingChild() {
        // assert removeEntity rejects it rather than orphaning the parent.
        ProjectsDto minimalTree = rootOnlyTree();
        service.loadAllProjectEntities(minimalTree);
        Task newTask = new Task("tt1", "New Leaf Task",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));
        service.addNewEntity("p1", newTask);
        ResponseDto<ProjectComponent> response = service.removeEntity("p1", "tt1");
        assertThat(response.getMessage()).isEqualTo(CANNOT_REMOVE_CHILD_MSG);

    }

    @Test
    void removeEntity_rejectsWhenParentUidDoesNotMatchTarget() throws Exception {
        // pass a parentUid that's valid but isn't target's real parent,
        // assert removeEntity rejects it rather than silently detaching nothing.
        ProjectsDto dto = loadFixture();
        service.loadAllProjectEntities(dto);
        String validParentUid = "690ajgop520";
        String wrongChildUid = "270tmqyb719";
        ResponseDto<ProjectComponent> response = service.removeEntity(validParentUid, wrongChildUid);
        assertThat(response.getMessage()).isEqualTo(WRONG_CHILD_MSG);
    }
    @Test
    void removeEntity_updatesParentDatesWithoutRecalculation() {
        service.loadAllProjectEntities(new ProjectsDto(List.of(
                node("p1", "Root", "PROJECT", null, null, null),
                node("t1", "Early task", "TASK", "p1", "2026-01-01", "2026-01-10"),
                node("t2", "Late task", "TASK", "p1", "2026-02-01", "2026-02-10"))));

        ProjectComponent root = service.getProjects().get("p1");
        assertThat(root.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(root.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 10));
        ResponseDto<ProjectComponent> response = service.removeEntity("p1", "t1");
        assertThat(response.getEntitiesCount()).isEqualTo(1);
        assertThat(service.getProjects()).doesNotContainKey("t1");
        // the same root object now reports the remaining task's dates
        assertThat(root.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(root.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 10));
    }

    @Test
    void removeEntity_removingSubprojectPurgesItsSubtreeAndUpdatesDates() {
        // p1
        //  ├── sp1
        //  │    └── t1  2026-01-01 .. 2026-01-10
        //  └── t2       2026-02-01 .. 2026-02-10
        service.loadAllProjectEntities(new ProjectsDto(List.of(
                node("p1", "Root", "PROJECT", null, null, null),
                node("sp1", "Sub", "PROJECT", "p1", null, null),
                node("t1", "Task 1", "TASK", "sp1", "2026-01-01", "2026-01-10"),
                node("t2", "Task 2", "TASK", "p1", "2026-02-01", "2026-02-10"))));

        ResponseDto<ProjectComponent> response = service.removeEntity("p1", "sp1");

        assertThat(response.getEntitiesCount()).isEqualTo(2);    // sp1 + t1
        assertThat(service.getProjects()).containsOnlyKeys("p1", "t2"); // no zombie entries
        ProjectComponent root = service.getProjects().get("p1");
        assertThat(root.getChildren()).extracting(ProjectComponent::getUid).containsExactly("t2");
        assertThat(root.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(root.getEndDate()).isEqualTo(LocalDate.of(2026, 2, 10));
    }
    @Test
    void loadAllProjectEntities_acceptsRootWithNoChildrenYet() {
        ResponseDto<?> response = service.loadAllProjectEntities(rootOnlyTree());
        assertThat(response.getMessage()).isEqualTo("CREATED");
        assertThat(response.getEntitiesCount()).isEqualTo(1);
        assertThat(service.getProjects()).containsOnlyKeys("p1");
    }
    @Test
    void serializeProjectStructure_throwsWhenNothingLoaded() {
        assertThatThrownBy(() -> service.serializeProjectStructure())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("root project is null");
    }
    @Test
    @SuppressWarnings("unchecked")
    void serializeProjectStructure_buildNestedHierarchyCheckSize() {
        // p1 (root)
        //  ├── sp1 (subproject)
        //       └── t1  2026-01-01 .. 2026-01-10
        //       └── t2  2026-01-14 .. 2026-01-17
        service.loadAllProjectEntities(new ProjectsDto(List.of(
                node("p1", "Root", "PROJECT", null, null, null),
                node("sp1", "Sub", "PROJECT", "p1", null, null),
                node("t1", "Task 1", "TASK", "sp1", "2026-01-01", "2026-01-10"),
                node("t2", "Task 2", "TASK", "sp1", "2026-01-14", "2026-01-17"))));

        Map<String, Object> root = service.serializeProjectStructure();
        assertThat(root.get("uid")).isEqualTo("p1");
        assertThat(root.get("type")).isEqualTo(ProjectType.PROJECT);
        assertThat(root.get("parentUid")).isNull();

        int allNodesCount = countNodes(root);
        assertThat(allNodesCount).isEqualTo(4);
    }
    @Test
    void serializeProjectStructure_includesEveryFixtureNode() throws Exception {
        ProjectsDto dto = loadFixture();
        service.loadAllProjectEntities(dto);
        int fixtureNodesCount = countNodes(service.serializeProjectStructure());
        assertThat(fixtureNodesCount).isEqualTo(dto.getItems().size());
    }
    @Test
    @SuppressWarnings("unchecked")
    void serializeProjectStructure_buildsNestedHierarchyWithDerivedParentUid() {
        // p1 (root)
        //  ├── sp1 (subproject)
        //  │    └── t1  2026-01-01 .. 2026-01-10
        //  └── t2       2026-02-01 .. 2026-02-10
        service.loadAllProjectEntities(new ProjectsDto(List.of(
                node("p1", "Root", "PROJECT", null, null, null),
                node("sp1", "Sub", "PROJECT", "p1", null, null),
                node("t1", "Task 1", "TASK", "sp1", "2026-01-01", "2026-01-10"),
                node("t2", "Task 2", "TASK", "p1", "2026-02-01", "2026-02-10"))));

        Map<String, Object> root = service.serializeProjectStructure();
        assertThat(root.get("uid")).isEqualTo("p1");
        assertThat(root.get("type")).isEqualTo(ProjectType.PROJECT);
        assertThat(root.get("parentUid")).isNull();
        assertThat(root.get("startDate")).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(root.get("endDate")).isEqualTo(LocalDate.of(2026, 2, 10));

        // children appear in the order they were attached
        List<Map<String, Object>> rootChildren = (List<Map<String, Object>>) root.get("children");
        assertThat(rootChildren).extracting(c -> c.get("uid")).containsExactly("sp1", "t2");

        // subproject: type derived from position, parentUid threaded from above
        Map<String, Object> sp1 = rootChildren.get(0);
        assertThat(sp1.get("type")).isEqualTo(ProjectType.SUBPROJECT);
        assertThat(sp1.get("parentUid")).isEqualTo("p1");
        assertThat(sp1.get("startDate")).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(sp1.get("endDate")).isEqualTo(LocalDate.of(2026, 1, 10));

        // leaf: correct parentUid, and no "children" key at all
        Map<String, Object> t1 = ((List<Map<String, Object>>) sp1.get("children")).get(0);
        assertThat(t1.get("parentUid")).isEqualTo("sp1");
        assertThat(t1).doesNotContainKey("children");
    }

    @Test
    void serializeProjectStructure_reflectsLaterMutationsWithoutRecalculation() {
        service.loadAllProjectEntities(new ProjectsDto(List.of(
                node("p1", "Root", "PROJECT", null, null, null),
                node("t1", "Task 1", "TASK", "p1", "2026-03-01", "2026-03-10"))));
        assertThat(service.serializeProjectStructure().get("startDate"))
                .isEqualTo(LocalDate.of(2026, 3, 1));

        service.addNewEntity("p1", new Task("t2", "Earlier task",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)));

        // nothing "recomputes" anywhere; the root's dates are derived live
        Map<String, Object> root = service.serializeProjectStructure();
        assertThat(root.get("startDate")).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(root.get("endDate")).isEqualTo(LocalDate.of(2026, 3, 10));
    }
    @SuppressWarnings("unchecked")
    private int countNodes(Map<String, Object> node) {
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children == null) return 1;
        return 1 + children.stream().mapToInt(this::countNodes).sum();
    }

    private ProjectNodeDTO node(String uid, String name, String type,
                                String parentUid, String start, String end) {
        ProjectNodeDTO dto = new ProjectNodeDTO();
        dto.uid = uid;
        dto.name = name;
        dto.type = type;
        dto.parentUid = parentUid;
        dto.startDate = start;
        dto.endDate = end;
        return dto;
    }
    private void printTree(ProjectComponent node, String indent) {
        System.out.println(indent + "- [" + node.getType() + "] " + node.getName()
                + " (" + node.getUid() + ") start=" + node.getStartDate() + " end=" + node.getEndDate());
        for (ProjectComponent child : node.getChildren()) {
            printTree(child, indent + "  ");
        }
    }
    private ProjectsDto rootOnlyTree() {
        ProjectNodeDTO root = new ProjectNodeDTO();
        root.uid = "p1";
        root.name = "Root Project";
        root.type = "PROJECT";
        return new ProjectsDto(List.of(root));
    }
}