package com.example.users.integration.service;

import com.example.users.domain.model.User;
import com.example.users.domain.repository.UserRepository;
import com.example.users.dto.ResponseDTO;
import com.example.users.dto.UserDTO;
import com.example.users.dto.UserEventDTO;
import com.example.users.events.UserManagerProducer;
import com.example.users.service.UserService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(topics = "${kafka.topic.user-topic}", partitions = 1)
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoSpyBean
    private UserManagerProducer userManagerProducer;

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

    }

    @Test
    void shouldCreateUserAndSendKafkaMessage() {

        UserDTO userDTO = createUserDTO();

        ResponseDTO response = userService.createUser(userDTO);

        assertDB();

        assertKafkaEvent(response, userDTO);
    }

    private void assertDB() {
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("edu", users.get(0).getName());
    }

    private void assertKafkaEvent(ResponseDTO response, UserDTO userDTO) {

        verify(userManagerProducer, times(1)).sendMessage(userDTO);

        Consumer<String, UserEventDTO> consumer = createKafkaConsumer();
        ConsumerRecords<String, UserEventDTO> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.isEmpty()).isFalse();

        UserEventDTO received = records.iterator().next().value();
        assertThat(received.getEventType()).isEqualTo("USER_CREATED");
        assertThat(received.getUserDTO()).isNotNull();
        assertThat(received.getUserDTO().getName()).isEqualTo("edu");
        assertThat(received.getUserDTO().getEmail()).isEqualTo("edu@teste.com.br");

        assertEquals("User created", response.message());
        assertEquals(userDTO.getEmail(), ((UserDTO) response.data().get(0)).getEmail());
    }


    private Consumer<String, UserEventDTO> createKafkaConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Consumer<String, UserEventDTO> consumer = new DefaultKafkaConsumerFactory<>(consumerProps,
                new StringDeserializer(),
                new JsonDeserializer<>(UserEventDTO.class, false)
        ).createConsumer();
        consumer.subscribe(Collections.singletonList("users_managers_topic"));
        return consumer;
    }

    private UserDTO createUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName("edu");
        userDTO.setEmail("edu@teste.com.br");
        return userDTO;
    }
}