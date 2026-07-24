package com.example.fwnojackson.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class ProjectComposite extends ProjectComponent {
    private final List<ProjectComponent> children = new ArrayList<>();

    public ProjectComposite(String uid, String name) {
        super(uid, name);
    }

    @Override
    public String getUid() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public LocalDate getStartDate() {
        return children.stream()
                .map(ProjectComponent::getStartDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDate getEndDate() {
        return children.stream()
                .map(ProjectComponent::getStartDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    @Override
    public ProjectType getType() {
        return null;
    }

    @Override
    public void addChild(ProjectComponent newComponent) {
        children.add(newComponent);
    }

    @Override
    public void removeChild(ProjectComponent component) {
        children.remove(component);
    }

    @Override
    public List<ProjectComponent> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void printStructure(String indent) {

    }
}
