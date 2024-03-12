package com.example.fwnojackson.dto;

public class ResponseDto<T> {
    private String message;
    private T entity;
    private int entitiesCount;

    public ResponseDto(String message, int entitiesCount) {
        this.message = message;
        this.entitiesCount = entitiesCount;
    }
    public ResponseDto(String message, T entity, int entitiesCount) {
        this.message = message;
        this.entity = entity;
        this.entitiesCount = entitiesCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getEntitiesCount() {
        return entitiesCount;
    }

    public void setEntitiesCount(int entitiesCount) {
        this.entitiesCount = entitiesCount;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}
