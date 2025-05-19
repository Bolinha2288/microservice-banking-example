package com.example.users.unit.service;

import com.example.users.domain.model.User;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.events.UserManagerProducer;
import com.example.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.KafkaException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTests {
    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserManagerProducer userManagerProducer;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(modelMapper, userRepository, userManagerProducer);
    }

    @Test
    void shouldCreateUserSuccessfully() {

        UserDTO userDTO = createUserDTO();

        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        doNothing().when(userManagerProducer).sendMessage(userDTO);

        ResponseDTO response = userService.createUser(userDTO);

        assertNotNull(response);
        assertEquals("User created", response.message());
        assertEquals(1, response.data().size());
        assertEquals(userDTO, response.data().get(0));

        verify(userRepository, times(1)).save(user);
        verify(userManagerProducer, times(1)).sendMessage(userDTO);
    }

    @Test
    void shouldThrowExceptionWhenDatabaseFails() {

        UserDTO userDTO = createUserDTO();
        User user = new User();

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        doThrow(new DataAccessException("Database error") {}).when(userRepository).save(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(userDTO));
        assertEquals("Failed to save user to the database", exception.getMessage());

        verify(userRepository, times(1)).save(user);
        verify(userManagerProducer, never()).sendMessage(any());
    }

    @Test
    void shouldThrowExceptionWhenKafkaFails() {
        UserDTO userDTO = createUserDTO();
        User user = new User();

        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        doThrow(new KafkaException("Kafka error")).when(userManagerProducer).sendMessage(userDTO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(userDTO));
        assertEquals("Failed to send message to Kafka", exception.getMessage());

        verify(userRepository, times(1)).save(user);
        verify(userManagerProducer, times(1)).sendMessage(userDTO);
    }

    private UserDTO createUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com.br");
        return userDTO;
    }
}
