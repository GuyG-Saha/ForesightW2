package com.example.fwnojackson.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProjectTreeBuilderTest {

    @Test
    void buildTree_rejectsMultipleRoots() {
        ProjectNodeDTO root1 = new ProjectNodeDTO();
        root1.uid = "p1"; root1.name = "Root 1"; root1.type = "PROJECT";
        ProjectNodeDTO root2 = new ProjectNodeDTO();
        root2.uid = "p2"; root2.name = "Root 2"; root2.type = "PROJECT";

        assertThatThrownBy(() -> ProjectTreeBuilder.buildTree(List.of(root1, root2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Multiple root nodes");
    }

}