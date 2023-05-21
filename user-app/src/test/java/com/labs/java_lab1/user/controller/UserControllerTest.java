package com.labs.java_lab1.user.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.labs.java_lab1.common.response.AuthenticationResponse;
import com.labs.java_lab1.common.security.JwtUserData;
import com.labs.java_lab1.user.dto.AuthDto;
import com.labs.java_lab1.user.dto.CreateUserDto;
import com.labs.java_lab1.user.dto.PaginationDto;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    public void shouldSaveAndGet() {
        assertNotNull(mockMvc);
        // сохранение пользователя
        String register = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(userJson()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readValue(register, AuthenticationResponse.class).getToken();

        // аутентификация
//        String logIn = mockMvc.perform(post("/api/users/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .characterEncoding(StandardCharsets.UTF_8)
//                        .content(loginJson()))
//                .andExpect(status().isOk())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        String token = objectMapper.readValue(logIn, AuthenticationResponse.class).getToken();

        UUID userId = UUID.fromString("46e202ad-b624-4dd4-8406-ca5b30c76e7c");

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = mock(Authentication.class);
        JwtUserData jwtUserData = new JwtUserData(userId,"gqwrhrehr", "Иванов Иван Иваныч");
        when(authentication.getPrincipal()).thenReturn(jwtUserData);
        context.setAuthentication(authentication);

        // получение пользователя
        mockMvc.perform((get("/api/users/profile/gqwrhrehr"))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("gqwrhrehr"))
                .andExpect(jsonPath("$.email").value("ivan@tsu.ru"))
                .andExpect(jsonPath("$.fullName").value("Иванов Иван Иваныч"))
                .andExpect(jsonPath("$.phoneNumber").value("592386923"))
                .andExpect(jsonPath("$.city").value("city"))
                .andExpect(jsonPath("$.avatar").value("08f1baa5-7710-4c4f-babb-351bb2519d02"));

        // проверка, что на левого пользователя вернётся 404
        mockMvc.perform((get("/api/users/incorrectLogin")))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    public void shouldAccessUserList() {
        assertNotNull(mockMvc);
        // список пользователей
        mockMvc.perform((post("/api/users/list"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(filtersJson()))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    private String userJson() {
        CreateUserDto dto = CreateUserDto.of(
                "gqwrhrehr",
                "ivan@tsu.ru",
                "1",
                "Иванов Иван Иваныч",
                new Date(1684236437),
                "592386923",
                "city",
                "08f1baa5-7710-4c4f-babb-351bb2519d02");
        return objectMapper.writeValueAsString(dto);
    }

    @SneakyThrows
    private String loginJson() {
        AuthDto dto = new AuthDto(
                "gqwrhrehr",
                "1");
        return objectMapper.writeValueAsString(dto);
    }

    @SneakyThrows
    private String filtersJson() {
        Map<String, String> filters = new HashMap<>();
        filters.put("fullName", "h");
        Map<String, String> sorting = new HashMap<>();
        sorting.put("fullName", "asc");

        PaginationDto dto = PaginationDto.of(
                1,
                5,
                filters,
                sorting
        );
        return objectMapper.writeValueAsString(dto);
    }

}
