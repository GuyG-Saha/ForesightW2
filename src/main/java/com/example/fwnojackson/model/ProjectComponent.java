package com.example.fwnojackson.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Task.class, name = "TASK"),
        @JsonSubTypes.Type(value = ProjectComposite.class, name = "PROJECT")
})
public abstract class ProjectComponent {
    protected String uid;
    protected String name;
    protected LocalDate startDate;
    protected LocalDate endDate;
    protected ProjectType type;

    public ProjectComponent(String uid, String name, LocalDate startDate, LocalDate endDate, ProjectType type) {
        this.uid = uid;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.type = type;
    }

    public ProjectComponent(String uid, String name, ProjectType type) {
        this(uid, name, null, null, type);
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }
    public ProjectType getType() { return type; }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Composite-related methods
    public abstract void addChild(ProjectComponent child);
    public abstract void removeChild(ProjectComponent child);
    public abstract List<ProjectComponent> getChildren();

    // Utility method (can be overridden or used recursively)
    public abstract void printStructure(String indent);
}
