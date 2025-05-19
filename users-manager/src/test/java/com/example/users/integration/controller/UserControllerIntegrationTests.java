package com.example.users.integration.controller;

import com.example.users.controller.UserController;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(UserController.class)
class UserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return 201 when user created successfully")
    void shouldReturn201WhenUserIsValid() throws Exception {

        UserDTO userDTO = createUserDTO();
        ResponseDTO responseDTO = new ResponseDTO("User created", List.of(userDTO));

        when(userService.createUser(any(UserDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User created"))
                .andExpect(jsonPath("$.data[0].name").value("edu"))
                .andExpect(jsonPath("$.data[0].email").value("edu@teste.com.br"));
    }

    @Test
    @DisplayName("Should return 400 when invalid input")
    void shouldReturn400ForInvalidInput() throws Exception {
        UserDTO invalidUserDTO = new UserDTO();
        invalidUserDTO.setName("");
        invalidUserDTO.setEmail("invalidemail");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Errors validation data: "))
                .andExpect(jsonPath("$.data[0].name").value("The name must be between 3 and 50 characters."))
                .andExpect(jsonPath("$.data[0].email").value("The email must be valid."));
    }

    @Test
    @DisplayName("Should return 500 occurs generic exception")
    void shouldReturn500WhenGenericExceptionOccursInService() throws Exception {
        UserDTO userDTO = createUserDTO();
        String genericErrorMessage = "Simulated generic error";
        NullPointerException serviceException = new NullPointerException(genericErrorMessage);

        when(userService.createUser(any(UserDTO.class))).thenThrow(serviceException);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred: " + serviceException.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());
    }


    private UserDTO createUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com.br");
        return userDTO;
    }
}