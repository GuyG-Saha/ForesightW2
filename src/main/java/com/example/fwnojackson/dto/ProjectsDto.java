package com.example.fwnojackson.dto;

import com.example.fwnojackson.model.ProjectComponent;

import java.util.ArrayList;
import java.util.List;

public class ProjectsDto {
    private List<ProjectNodeDTO> items;

    public ProjectsDto() {
        items = new ArrayList<ProjectNodeDTO>();
    }
    public ProjectsDto(List<ProjectNodeDTO> items) {
        this.items = items;
    }

    public List<ProjectNodeDTO> getItems() {
        return items;
    }

    public void setItems(List<ProjectNodeDTO> items) {
        this.items = items;
    }
}
