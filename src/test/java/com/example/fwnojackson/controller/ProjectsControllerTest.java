package com.example.fwnojackson.controller;

import com.example.fwnojackson.dto.ResponseDto;
import com.example.fwnojackson.model.ProjectComponent;
import com.example.fwnojackson.model.ProjectType;
import com.example.fwnojackson.service.ProjectsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
@WebMvcTest(ProjectsController.class)
class ProjectsControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ProjectsService service;
    @Test
    void inBandFailureBecomes400() throws Exception {
        when(service.updateTaskDates(eq("t1"), any(), any()))
                .thenReturn(new ResponseDto<>("Unknown uid provided", 0));

        mvc.perform(patch("/api/projects/t1/dates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-02-01\",\"endDate\":\"2026-02-10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown uid provided"));
    }

    @Test
    void serviceExceptionIsMappedTo400ByTheControllerAdvice() throws Exception {
        when(service.serializeProjectStructure()).thenThrow(new IllegalArgumentException("nothing loaded"));

        mvc.perform(get("/api/projects/hierarchy"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("nothing loaded"));
    }

    @Test
    void nestedAddRequestIsBoundAndBuiltIntoASubprojectWithItsTask() throws Exception {
        when(service.addNewEntity(eq("p1"), any(ProjectComponent.class)))
                .thenReturn(new ResponseDto<>("Added", 2));

        mvc.perform(post("/api/projects/p1/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"uid":"sp2","name":"Sub","type":"PROJECT","children":[
                                  {"uid":"t3","name":"Task","type":"TASK","startDate":"2026-05-01","endDate":"2026-05-09"}]}"""))
                .andExpect(status().isCreated());

        ArgumentCaptor<ProjectComponent> captor = ArgumentCaptor.forClass(ProjectComponent.class);
        verify(service).addNewEntity(eq("p1"), captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ProjectType.SUBPROJECT);
        assertThat(captor.getValue().getChildren()).extracting(ProjectComponent::getUid).containsExactly("t3");
    }

    @Test
    void unknownNodeTypeInAddRequestIs400() throws Exception {
        mvc.perform(post("/api/projects/p1/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uid\":\"x\",\"name\":\"X\",\"type\":\"MILESTONE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void completionDateParameterIsBound() throws Exception {
        when(service.getCompletionStatus("t1", LocalDate.of(2021, 10, 3)))
                .thenReturn(10.0);

        mvc.perform(get("/api/projects/t1/completion").param("date", "2021-10-03"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }
}
