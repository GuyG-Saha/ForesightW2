package com.example.fwnojackson.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class Task extends ProjectComponent {

    public Task(String uid, String name, LocalDate startDate, LocalDate endDate) {
        super(uid, name, startDate, endDate, ProjectType.TASK);
    }

    @Override
    public void addChild(ProjectComponent child) {
        throw new UnsupportedOperationException("Cannot add child to a Task (Leaf).");
    }

    @Override
    public boolean removeChild(ProjectComponent child) {
        throw new UnsupportedOperationException("Cannot remove child from a Task (Leaf).");
    }

    @Override
    public List<ProjectComponent> getChildren() {
        return Collections.emptyList(); // Tasks have no children
    }

    @Override
    public void printStructure(String indent) {
        System.out.println(indent + "Task: " + name + " (" + uid + ")");
    }
}
