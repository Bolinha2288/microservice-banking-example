package com.example.users.unit.controller;

import com.example.users.controller.UserController;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private  ObjectMapper objectMapper;

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUserTest(){
        UserDTO userDTO = createUserDTO();
        when(userService.createUser(userDTO)).thenReturn(createResponse());
        UserController userController = new UserController(userService);
        ResponseEntity<ResponseDTO> response = userController.criarUsuario(userDTO);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    private UserDTO createUserDTO(){
        UserDTO userDTO = new UserDTO();
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com.br");
        return userDTO;
    }

    private ResponseDTO createResponse(){
        return new ResponseDTO("User created", List.of(createUserDTO()));
    }
}
