package com.example.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserDTO{

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @Schema(hidden = true)
        private Long id;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID idReference = UUID.randomUUID();

        @Size(min = 3, max = 50, message = "The name must be between 3 and 50 characters.")
        private String name;

        @Email(message = "The email must be valid.")
        private String email;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private LocalDateTime dateCreated = LocalDateTime.now();
}
