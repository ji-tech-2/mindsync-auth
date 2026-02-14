package com.jitech.mindsync.controller;

import com.jitech.mindsync.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloWorldController.class)
@AutoConfigureMockMvc(addFilters = false)
class HelloWorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    void testHelloWorld() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello World"));
    }
}
