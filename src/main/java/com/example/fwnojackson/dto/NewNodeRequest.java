package com.example.fwnojackson.dto;

import java.time.LocalDate;
import java.util.List;

public record NewNodeRequest(String uid, String name, String type,
                             LocalDate startDate, LocalDate endDate,
                             List<NewNodeRequest> children) {}
