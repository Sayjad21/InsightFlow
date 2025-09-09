package com.insightflow;

import com.insightflow.controllers.AuthController;
import com.insightflow.security.JwtUtil;
import com.insightflow.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(JwtUtil.class)
@AutoConfigureMockMvc(addFilters = false) // ← This disables Spring Security for the test
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testSignup() throws Exception {
        when(userService.signup(anyString(), anyString())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/signup")
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"password\":\"testpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    public void testLogin() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/login")
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"password\":\"testpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }
}