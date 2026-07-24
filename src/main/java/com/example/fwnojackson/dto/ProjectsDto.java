package com.example.fwnojackson.dto;

import com.example.fwnojackson.model.ProjectComponent;

import java.util.ArrayList;
import java.util.List;

public class ProjectsDto {
    private List<ProjectComponent> items;

    public ProjectsDto() {
        items = new ArrayList<ProjectComponent>();
    }
    public ProjectsDto(List<ProjectComponent> items) {
        this.items = items;
    }

    public List<ProjectComponent> getItems() {
        return items;
    }

    public void setItems(List<ProjectComponent> items) {
        this.items = items;
    }
}
