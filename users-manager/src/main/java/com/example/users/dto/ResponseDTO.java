package com.example.users.dto;

import java.util.List;
import java.util.Objects;

public record ResponseDTO(String message, List<Object> data) {
}
