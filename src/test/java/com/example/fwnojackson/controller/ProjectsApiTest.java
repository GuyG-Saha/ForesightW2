package com.example.fwnojackson.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectsApiTest {

    @Autowired
    private MockMvc mvc;

    private static final String TREE = """
            {"items":[
              {"uid":"p1","name":"Root","type":"PROJECT"},
              {"uid":"sp1","name":"Sub","type":"PROJECT","parentUid":"p1"},
              {"uid":"t1","name":"Task 1","type":"TASK","startDate":"2026-01-01","endDate":"2026-01-10","parentUid":"sp1"},
              {"uid":"t2","name":"Task 2","type":"TASK","startDate":"2026-02-01","endDate":"2026-02-10","parentUid":"p1"}
            ]}""";

    @Test
    void contextLoads() {
    }

    @Test
    void uploadReadUpdateAddRemove() throws Exception {
        mvc.perform(post("/api/upload").contentType(MediaType.APPLICATION_JSON).content(TREE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entitiesCount").value(4));

        mvc.perform(get("/api/projects/hierarchy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("p1"))
                .andExpect(jsonPath("$.type").value("PROJECT"))
                .andExpect(jsonPath("$.startDate").value("2026-01-01"))
                .andExpect(jsonPath("$.endDate").value("2026-02-10"))
                .andExpect(jsonPath("$.children[0].type").value("SUBPROJECT"))
                .andExpect(jsonPath("$.children[0].parentUid").value("p1"))
                .andExpect(jsonPath("$.children[0].children[0].uid").value("t1"));

        // moving t1 later: the root's dates follow with no recalculation step
        mvc.perform(patch("/api/projects/t1/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-03-01\",\"endDate\":\"2026-03-20\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/projects/hierarchy"))
                .andExpect(jsonPath("$.startDate").value("2026-02-01"))
                .andExpect(jsonPath("$.endDate").value("2026-03-20"));

        // root span is Feb 1..Mar 20 = 48 days; on Feb 25, 24 days have elapsed -> 50%
        mvc.perform(get("/api/projects/p1/completion").param("date", "2026-02-25"))
                .andExpect(status().isOk())
                .andExpect(content().string("50.0"));

        // add a subproject that arrives with its task (nested body)
        mvc.perform(post("/api/projects/p1/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"uid":"sp2","name":"New Sub","type":"PROJECT","children":[
                                  {"uid":"t3","name":"New Task","type":"TASK","startDate":"2026-05-01","endDate":"2026-05-09"}]}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entitiesCount").value(2));

        // sp1's only child can't be removed
        mvc.perform(delete("/api/projects/sp1/children/t1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot remove the last remaining child"));

        // removing a subproject returns its subtree; each of the 2 nodes carries exactly one "type"
        String body = mvc.perform(delete("/api/projects/p1/children/sp2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entitiesCount").value(2))
                .andReturn().getResponse().getContentAsString();
        assertThat(body.split("\"type\"", -1).length - 1).isEqualTo(2);
    }
}