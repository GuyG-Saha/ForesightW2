package com.example.fwnojackson.dto;

import com.example.fwnojackson.model.ProjectEntity;

import java.util.ArrayList;
import java.util.List;

public class ProjectsDto {
    private List<ProjectEntity> items;

    public ProjectsDto() {
        items = new ArrayList<ProjectEntity>();
    }
    public ProjectsDto(List<ProjectEntity> items) {
        this.items = items;
    }

    public List<ProjectEntity> getItems() {
        return items;
    }

    public void setItems(List<ProjectEntity> items) {
        this.items = items;
    }
}
