package com.example.account.dto;

import java.util.List;

public record ResponseDTO(String message, List<Object> data) {
}
