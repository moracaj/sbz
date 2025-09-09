package com.example.sbz.web.dto;

public record UserSummaryDto(
    Long id, String firstName, String lastName, String email, boolean friend, boolean blocked
) {}
