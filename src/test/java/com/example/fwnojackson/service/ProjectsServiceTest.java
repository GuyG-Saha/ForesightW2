package com.example.fwnojackson.service;

import com.example.fwnojackson.Inputs;
import com.example.fwnojackson.dto.ProjectsDto;
import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.model.ProjectComposite;
import com.example.fwnojackson.model.ProjectType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectsServiceTest {
    private ProjectsService service;
    private ObjectMapper mapper;

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

    private void printTree(ProjectComponent node, String indent) {
        System.out.println(indent + "- [" + node.getType() + "] " + node.getName()
                + " (" + node.getUid() + ") start=" + node.getStartDate() + " end=" + node.getEndDate());
        for (ProjectComponent child : node.getChildren()) {
            printTree(child, indent + "  ");
        }
    }
    @Test
    void loadAllProjectEntities_rejectsEmptyPayload() {
        // TODO: call service.loadAllProjectEntities(new ProjectsDto()) (or null),
        // assert the "Bad Request" branch — no tree/root should be created.
    }

    @Test
    void addNewEntity_addsTaskUnderExistingSubproject() throws Exception {
        // TODO: loadFixture() + loadAllProjectEntities() first to get a populated tree,
        // pick a known uid from the fixture as parentUid, build a new Task,
        // call addNewEntity, assert entitiesCount == 1 and the parent's
        // getChildren() now includes it.
    }

    @Test
    void addNewEntity_rejectsSubprojectWithNoChildren() {
        // TODO: construct a bare ProjectComposite (no children attached),
        // assert addNewEntity rejects it with the "must be added with at
        // least one related Task" message.
    }

    @Test
    void removeEntity_rejectsRemovingTheLastRemainingChild() {
        // TODO: find (or construct) a parent with exactly one child,
        // assert removeEntity rejects it rather than orphaning the parent.
    }

    @Test
    void removeEntity_rejectsWhenParentUidDoesNotMatchTarget() {
        // TODO: this is the "caller lied about parentUid" case we discussed —
        // pass a parentUid that's valid but isn't target's real parent,
        // assert removeEntity rejects it rather than silently detaching nothing.
    }
}