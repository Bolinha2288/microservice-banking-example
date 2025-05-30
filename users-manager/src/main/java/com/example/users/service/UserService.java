package com.example.users.service;

import com.example.users.domain.model.User;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.events.UserManagerProducer;
import com.example.users.external.AccountServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AccountServiceClient accountServiceClient;

    public UserService(ModelMapper modelMapper, UserRepository userRepository, AccountServiceClient accountServiceClient) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.accountServiceClient = accountServiceClient;
    }

    @Transactional
    public ResponseDTO createUser(UserDTO userDTO) {
        log.info("Starting user create: {}", userDTO);

        try {
            userDTO.setIdReference(UUID.randomUUID());
            userDTO.setDateCreated(LocalDateTime.now());
            User user = modelMapper.map(userDTO, User.class);
            userRepository.save(user);
            accountServiceClient.sendDataAccountService(userDTO);
        } catch (DataAccessException e) {
            log.error("Error saving user to the database: {}", e.getMessage());
            throw new RuntimeException("Failed to save user to the database");
        }

        log.info("Successfully user create: {}", userDTO);
        return new ResponseDTO("User created", List.of(userDTO));
    }
}