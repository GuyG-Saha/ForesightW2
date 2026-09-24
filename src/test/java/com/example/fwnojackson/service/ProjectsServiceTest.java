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
    void loadAllProjectEntities_acceptsRootWithNoChildrenYet() {
        ResponseDto<?> response = service.loadAllProjectEntities(rootOnlyTree());
        assertThat(response.getMessage()).isEqualTo("CREATED");
        assertThat(response.getEntitiesCount()).isEqualTo(1);
        assertThat(service.getProjects()).containsOnlyKeys("p1");
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